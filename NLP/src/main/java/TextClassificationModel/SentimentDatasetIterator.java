package TextClassificationModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SentimentDatasetIterator implements DataSetIterator {

    private final int batchSize;
    private final int vectorSize;

    private final File[] positiveFiles;
    private final File[] negativeFiles;
    private final File[] neutralFiles;

    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;
    private final WordVectors wordVectors;
    private final int truncateLength;

    public SentimentDatasetIterator(String dataDirectory, WordVectors wordVectors, int batchSize, int truncateLength, boolean train) throws IOException {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;


        File positive = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/pos/") + "/");
        File negative = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/neg/") + "/");
        File neutral = new File(FilenameUtils.concat(dataDirectory, "dataset/" + (train ? "train" : "test") + "/neu/") + "/");
        positiveFiles = positive.listFiles();
        negativeFiles = negative.listFiles();
        neutralFiles = neutral.listFiles();

        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }

    @Override
    public DataSet next(int num) {
        if (cursor >= positiveFiles.length + negativeFiles.length) throw new NoSuchElementException();
        try {
            return nextDataSet(num);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int num) throws IOException {
        List<String> texts = new ArrayList<>(num);
        int[] sentiment = new int[num];




        for( int i=0; i<num && cursor<totalDataset(); i++ ){
            if(cursor % 2 == 0){
                //Load positive review
                int posReviewNumber = cursor/ 2;
                String tweetText = FileUtils.readFileToString(positiveFiles[posReviewNumber], (Charset)null);
                texts.add(tweetText);
                sentiment[i] = 0;
            } else {
                //Load negative review
                int negReviewNumber = cursor/ 2;
                String tweetText = FileUtils.readFileToString(negativeFiles[negReviewNumber], (Charset)null);
                texts.add(tweetText);
                sentiment[i] = 1;
            }
            cursor++;
        }
        return null;
    }


    public int totalDataset() {
        return positiveFiles.length + negativeFiles.length + neutralFiles.length;
    }

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
        return null;
    }

    @Override
    public void remove() {
//        DataSetIterator.super.remove();
    }
}
