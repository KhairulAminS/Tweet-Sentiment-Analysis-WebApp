package Inference;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.application.views.MainLayout.model;
import static com.example.application.views.MainLayout.word2Vec;
import static com.example.application.views.sentimentanalysis.cleanText.cleanTweetText;
import static org.nd4j.linalg.factory.Nd4j.argMax;

public class getSentiment {

    static String text;
    static TokenizerFactory tokenizerFactory;


    // For testing purposes
    public static void main(String[] args) throws IOException {
        String prediction = getTweetsSentiment("aku boleh berpura-pura tersenyum tapi aku tak reti nk berpura-pura bahagia", model, word2Vec);
        System.out.println(prediction);
    }

    public static String getTweetsSentiment(String tweet, MultiLayerNetwork model, Word2Vec word2Vec) throws IOException {
        text = tweet;
        String sentiment;


        String data = cleanTweetText(text);

//        System.out.println(data);

        INDArray features = loadFeaturesFromString(data, word2Vec);


//        System.out.println(model.summary());
//
//        System.out.println(features);

        INDArray output = model.output(features);

        long timeSeriesLength = output.size(2);
        INDArray probabilitiesAtLastWord = output.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));

        System.out.println("p(positive): " + probabilitiesAtLastWord.getDouble(0));
        System.out.println("p(negative): " + probabilitiesAtLastWord.getDouble(1));
        System.out.println("p(neutral): " + probabilitiesAtLastWord.getDouble(2));

        int label = Integer.parseInt(String.valueOf(argMax(probabilitiesAtLastWord)));

        if(label == 0){
            sentiment = "Positive";
        } else if(label == 1){
            sentiment = "Negative";
        } else{
            sentiment = "Neutral";
        }

        return sentiment;
    }

    private static INDArray loadFeaturesFromString(String reviewContents, Word2Vec word2Vec){
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        List<String> tokens = tokenizerFactory.create(reviewContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : tokens ){
            if(word2Vec.hasWord(t)) tokensFiltered.add(t);
        }
        int outputLength = Math.min(256,tokensFiltered.size());

        int vectorSize = word2Vec.getWordVector(word2Vec.vocab().wordAtIndex(0)).length;

        INDArray features = Nd4j.create(1, vectorSize, outputLength);

        int count = 0;
        for(int j = 0; j<tokensFiltered.size() && count< 256; j++ ){
            String token = tokensFiltered.get(j);
            INDArray vector = word2Vec.getWordVectorMatrix(token);
            if(vector == null){
                continue;   //Word not in word vectors
            }
            features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);
            count++;
        }

        return features;
    }
}
