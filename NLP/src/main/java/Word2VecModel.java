import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.File;
import java.util.Collection;


public class Word2VecModel {


    /**
     * Created by agibsonccc on 10/9/14.
     *
     * Neural net that processes text into wordvectors. See below url for an in-depth explanation.
     * https://deeplearning4j.org/word2vec.html
     */

    static String word = "hari";

    public static void main(String[] args) throws Exception {

        // Gets Path to Text file
        File filePath = new File("C:/Users/Khairul Amin/IdeaProjects/NLP/src/main/resources/TestVocab/ms-wiki.txt");

        System.out.println("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new UimaSentenceIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
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

        System.out.println("Fitting Word2Vec model....");
        vec.fit();

        System.out.println("Saving model....");

        WordVectorSerializer.writeWord2VecModel(vec, "word2vec_ms_wiki.vector");

        System.out.println("Writing word vectors to text file....");

        // Prints out the closest 10 words to "day". An example on what to do with these Word Vectors.
        System.out.println("Closest Words:");
        Collection<String> list = vec.wordsNearestSum(word, 10);
        System.out.println("\"Closest words to \" + word + \" on 2nd run: \" + list");
    }
}
