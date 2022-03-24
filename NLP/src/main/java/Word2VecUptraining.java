import org.bytedeco.opencv.presets.opencv_core;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Collection;

public class Word2VecUptraining {

    private static Logger log = LoggerFactory.getLogger(Word2VecUptraining.class);

    static String word = "malam";

    public static void main(String[] args) throws FileNotFoundException {

        String filePath = "C:/Users/Khairul Amin/IdeaProjects/NLP/src/main/resources/TestVocab/ms-wiki.txt";

        Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel("word2vec_twitter.txt");

        SentenceIterator iterator = new BasicLineIterator(filePath);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        word2Vec.setTokenizerFactory(tokenizerFactory);
        word2Vec.setSentenceIterator(iterator);

        log.info("Word2vec uptraining...");

        word2Vec.fit();

        Collection<String> list = word2Vec.wordsNearestSum(word, 10);
        log.info("Closest words to " + word + " on 2nd run: " + list);
    }

}
