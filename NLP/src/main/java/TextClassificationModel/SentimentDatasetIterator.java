package TextClassificationModel;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static TextClassificationModel.sentimentClassification.tweets;

public class SentimentDatasetIterator implements DataSetIterator {

    private final int batchSize;
    private final int vectorSize;
    private int size = tweets.size();

//    private final File[] positiveFiles;
//    private final File[] negativeFiles;
//    private final File[] neutralFiles;
//
//    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;
    private final WordVectors wordVectors;
    private final int truncateLength;

    public SentimentDatasetIterator(String dataDirectory, WordVectors wordVectors, int batchSize, int truncateLength) throws IOException {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(1)).length;


//        File positive = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/pos/") + "/");
//        File negative = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/neg/") + "/");
//        File neutral = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/neu/") + "/");
//        positiveFiles = positive.listFiles();
//        negativeFiles = negative.listFiles();
//        neutralFiles = neutral.listFiles();

        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    @Override
    public DataSet next(int num) {
        try {
            return nextDataSet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet() throws IOException {

        List<String> tweetText = new ArrayList<>(size);

        for(int i = 0; i< size; i++){
            tweetText.add(tweets.get(i).getText());
        }

        List<List<String>> allTokens = new ArrayList<>(tweetText.size());
        int maxLength = 0;
        for (String s : tweetText) {
            List<String> tokens = tokenizerFactory.create(s).getTokens();
            List<String> tokensFiltered = new ArrayList<>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t)) tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength, tokensFiltered.size());
        }

        if(maxLength > truncateLength) maxLength = truncateLength;

        INDArray features = Nd4j.create(new int[]{tweetText.size(), vectorSize, maxLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{tweetText.size(), 3, maxLength}, 'f');
        INDArray featuresMask = Nd4j.zeros(size, maxLength);
        INDArray labelsMask = Nd4j.zeros(size, maxLength);

        for( int i=1; i<size; i++ ){
            List<String> tokens = allTokens.get(i);
            String sentiment = tweets.get(i).getSentiment();

            // Get the truncated sequence length of document (i)
            int seqLength = Math.min(tokens.size(), maxLength);

//            // Get all wordvectors for the current document and transpose them to fit the 2nd and 3rd feature shape
//            final INDArray vectors = wordVectors.getWordVectors(tokens.subList(0, seqLength)).transpose();

            // Put wordvectors into features array at the following indices:
            // 1) Document (i)
            // 2) All vector elements which is equal to NDArrayIndex.interval(0, vectorSize)
            // 3) All elements between 0 and the length of the current sequence
            for( int j=0; j<tokens.size() && j<maxLength; ++j){
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);

                // Assign "1" to each position where a feature is present, that is, in the interval of [0, seqLength)
                featuresMask.get(new INDArrayIndex[] {NDArrayIndex.point(i), NDArrayIndex.interval(0, seqLength)}).assign(1);
            }

            int idx = 0;
            if (sentiment == "Negative") idx = 1;
            if (sentiment == "Neutral") idx = 2;
            int lastIdx = Math.min(tokens.size(),maxLength);
            labels.putScalar(new int[]{i,idx,lastIdx-1},1.0);   //Set label: [0,1] for negative, [1,0] for positive
            labelsMask.putScalar(new int[]{i,lastIdx-1},1.0);   //Specify that an output exists at the final time step for this example
        }

        return new DataSet(features,labels,featuresMask,labelsMask);
    }


//    public int totalDataset() {
//        return positiveFiles.length + negativeFiles.length + neutralFiles.length;
//    }

    @Override
    public int inputColumns() {
        return 0;
    }

    @Override
    public int totalOutcomes() {
        return 0;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public int batch() {
        return 0;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {

    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        return null;
    }

    @Override
    public List<String> getLabels() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {
//        DataSetIterator.super.remove();
    }
}
