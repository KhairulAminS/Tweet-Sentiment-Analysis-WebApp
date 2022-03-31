package Word2Vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.common.io.ClassPathResource;

import java.util.Collection;


public class Word2VecModel {


    static String word = "hari";

    public static void main(String[] args) throws Exception {

        String filePath = new ClassPathResource("Dataset\\cleanedDataset.txt").getFile().getPath();

        System.out.println("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        t.setTokenPreProcessor(new CommonPreprocessor());

        System.out.println("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(10)
                .layerSize(300)
                .seed(1234)
                .batchSize(2000)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        System.out.println("Fitting model....");
        vec.fit();

        System.out.println("Saving model....");

        WordVectorSerializer.writeWord2VecModel(vec, "word2vec.vector");

        System.out.println("Writing word vectors to text file....");

        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        System.out.println("Closest Words:");
        Collection<String> list = vec.wordsNearestSum(word, 10);
        System.out.println("Closest words to " + word + " on 2nd run: " + list);
    }
}
