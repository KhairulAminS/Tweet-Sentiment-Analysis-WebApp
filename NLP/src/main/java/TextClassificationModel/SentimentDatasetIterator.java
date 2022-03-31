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
import java.util.Arrays;
import java.util.List;

import static TextClassificationModel.FileReader.tweets;


public class SentimentDatasetIterator implements DataSetIterator {

    private final int batchSize;
    private final int vectorSize;

    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;
    private final WordVectors wordVectors;
    private final int truncateLength;

    List<String> tweetText = new ArrayList<>(tweets.size());
    int[] labelArr = new int[tweets.size()];

    public SentimentDatasetIterator(WordVectors wordVectors, int batchSize, int truncateLength) throws IOException {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;

        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    @Override
    public DataSet next(int num) {
        try {
            return nextDataSet(num);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int num) throws IOException {

        for(int i = 0; i<num; i++){
            tweetText.add(tweets.get(i).getText());
            labelArr[i] = tweets.get(i).getSentiment();
            cursor++;
        }

        List<List<String>> allTokens = new ArrayList<>();
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

        INDArray features = Nd4j.create(tweetText.size(), vectorSize, maxLength);
        INDArray labels = Nd4j.create(tweetText.size(), 3, maxLength);
        INDArray featuresMask = Nd4j.zeros(tweetText.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(tweetText.size(), maxLength);

        for( int i=0; i< tweetText.size(); i++ ){
            List<String> tokens = allTokens.get(i);

            // Get the truncated sequence length of document (i)
            int seqLength = Math.min(tokens.size(), maxLength);

            // Put wordvectors into features array at the following indices:
            // 1) Document (i)
            // 2) All vector elements which is equal to NDArrayIndex.interval(0, vectorSize)
            // 3) All elements between 0 and the length of the current sequence
            for( int j=0; j<tokens.size() && j<maxLength; ++j){
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);

                // Assign "1" to each position where a feature is present, that is, in the interval of [0, seqLength)
                featuresMask.get(NDArrayIndex.point(i), NDArrayIndex.interval(0, seqLength)).assign(1);
            }


            int lastIdx = Math.min(tokens.size(),maxLength);
            labels.putScalar(new int[]{i,labelArr[i],lastIdx-1},1.0);   //Set label: [0,1] for negative, [1,0] for positive
            labelsMask.putScalar(new int[]{i,lastIdx-1},1.0);   //Specify that an output exists at the final time step for this example
        }

        return new DataSet(features,labels,featuresMask,labelsMask);
    }


    public int totalDataset() {
        return tweets.size();
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return 3;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public void reset() {
        cursor = 0;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getLabels() {
        return Arrays.asList("positive","negative","neutral");
    }

    @Override
    public boolean hasNext() {
        return cursor < totalDataset();
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
