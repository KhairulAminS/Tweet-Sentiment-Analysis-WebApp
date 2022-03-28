package TextClassificationModel;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.ViewIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class sentimentClassification {

    static List<Tweet> tweets = new ArrayList<>();
    static String wordVectorsPath = new File("word2vec_ms_wiki.vector").getAbsolutePath();

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new ClassPathResource("data.csv").getFile();
        FileReader.csvToString(file.getPath());

        int batchSize = 64;     //Number of examples in each minibatch
        int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
        int nEpochs = 1;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
        final int seed = 0;     //Seed for reproducibility

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(5e-3))
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .list()
                .layer(new LSTM.Builder().nIn(vectorSize).nOut(256)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(wordVectorsPath));
        SentimentDatasetIterator allData = new SentimentDatasetIterator(file.getPath(), wordVectors, batchSize, truncateReviewsToLength);

        DataSet fullDataset = allData.next();

        SplitTestAndTrain testAndTrain = fullDataset.splitTestAndTrain(0.8);

        DataSet trainDataset = testAndTrain.getTrain();
        DataSet testDataset = testAndTrain.getTest();

        System.out.println("Starting training");
        model.setListeners(new ScoreIterationListener(1), new EvaluativeListener(testDataset, 1, InvocationType.EPOCH_END));

        for(int i=0; i < nEpochs; i++) {
            model.fit(trainDataset);
        }

//        Evaluation trainEval = model.evaluate(new ViewIterator(trainDataset);
////        Evaluation testEval = model.evaluate((DataSetIterator) testDataset);
//
//        System.out.println(trainEval);
////        System.out.println(testEval);
    }
}