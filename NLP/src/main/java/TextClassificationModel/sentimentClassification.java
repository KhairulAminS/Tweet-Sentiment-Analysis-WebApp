package TextClassificationModel;

import org.bytedeco.opencv.opencv_dnn.RNNLayer;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DropoutLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
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


public class sentimentClassification {

    static String wordVectorsPath = new File("word2vec_ms_wiki.vector").getAbsolutePath();

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new ClassPathResource("data.csv").getFile();
        FileReader.csvToString(file.getPath());

        int batchSize = 64;     //Number of examples in each minibatch
        int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
        int nEpochs = 500;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
        final int seed = 0;     //Seed for reproducibility

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(1e-3))
                .l2(0.05)
                .weightInit(WeightInit.XAVIER)
//                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .list()
                .layer(new LSTM.Builder()
                        .nIn(vectorSize)
                        .nOut(150)
                        .activation(Activation.TANH)
                        .build())
                .layer(new RnnOutputLayer.Builder().
                        activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .nOut(3)
                        .build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(wordVectorsPath));
        SentimentDatasetIterator allData = new SentimentDatasetIterator(file.getPath(), wordVectors, batchSize, truncateReviewsToLength);

        DataSet fullDataset = allData.next();

        SplitTestAndTrain testAndTrain = fullDataset.splitTestAndTrain(0.8);

        DataSet trainDataset = testAndTrain.getTrain();
        DataSet testDataset = testAndTrain.getTest();

        DataSetIterator trainIterator = new ViewIterator(trainDataset, trainDataset.numExamples());
        DataSetIterator testIterator = new ViewIterator(testDataset, testDataset.numExamples());

        StatsStorage statsStorage = new InMemoryStatsStorage();
        UIServer server = UIServer.getInstance();
        server.attach(statsStorage);
        model.setListeners(
                new StatsListener( statsStorage),
                new ScoreIterationListener(10)
        );

        System.out.println("......Start training......");

        for(int i=0; i < nEpochs; i++) {
            model.fit(trainIterator);
        }

        Evaluation trainEval = model.evaluate(trainIterator);
        Evaluation testEval = model.evaluate(testIterator);

        System.out.println(trainEval);
        System.out.println(testEval);
    }
}