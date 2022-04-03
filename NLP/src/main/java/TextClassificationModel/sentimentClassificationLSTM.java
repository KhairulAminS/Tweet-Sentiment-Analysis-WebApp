package TextClassificationModel;

import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
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


public class sentimentClassificationLSTM {

    static String wordVectorsPath = new File("word2vec.vector").getAbsolutePath();

    static int batchSize = 64;     //Number of examples in each minibatch
    static int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
//    static int nEpochs = 500;        //Number of epochs (full passes of training data) to train on
    static int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
    static final int seed = 0;     //Seed for reproducibility

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new ClassPathResource("Dataset/data.csv").getFile();
        FileReader.csvToString(file.getPath());

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(wordVectorsPath));
        SentimentDatasetIterator allData = new SentimentDatasetIterator(wordVectors, batchSize, truncateReviewsToLength);

        DataSet fullDataset = allData.next();

        SplitTestAndTrain testAndTrain = fullDataset.splitTestAndTrain(0.8);

        DataSet trainDataset = testAndTrain.getTrain();
        DataSet testDataset = testAndTrain.getTest();

        DataSetIterator trainIterator = new ViewIterator(trainDataset, trainDataset.numExamples());
        DataSetIterator testIterator = new ViewIterator(testDataset, testDataset.numExamples());


        MultiLayerNetwork model = new MultiLayerNetwork(config());
        model.init();

        StatsStorage statsStorage = new InMemoryStatsStorage();
        UIServer server = UIServer.getInstance();
        server.attach(statsStorage);
        model.setListeners(
                new StatsListener( statsStorage),
                new ScoreIterationListener(10));

        EarlyStoppingConfiguration esConfig = new EarlyStoppingConfiguration.Builder()
                .epochTerminationConditions(new ScoreImprovementEpochTerminationCondition(10))
                .evaluateEveryNEpochs(1)
                .scoreCalculator(new DataSetLossCalculator(testIterator, true)) //Calculate validation set score/loss
                .build();

        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConfig, model, trainIterator);  // Input Early Stopping Configuration, model, trainIterator
        EarlyStoppingResult result = trainer.fit();

        System.out.println("......Start training......");

        for(int i=0; i < result.getBestModelEpoch(); i++) {
            model.fit(trainIterator);
        }

        Evaluation trainEval = model.evaluate(trainIterator);
        Evaluation testEval = model.evaluate(testIterator);

        System.out.println(trainEval);
        System.out.println(testEval);


        System.out.println(".....Saving model.....");
        ModelSerializer.writeModel(model, "LSTM model.zip", true);
        System.out.println(".....Model saved.....");


    }

    private static MultiLayerConfiguration config(){
        return new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(1e-3))
                .l2(0.1)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new LSTM.Builder()
                        .nIn(vectorSize)
                        .nOut(150)
                        .dropOut(0.3)
                        .activation(Activation.TANH)
                        .build())
                .layer(new LSTM.Builder()
                        .nOut(50)
                        .activation(Activation.TANH)
                        .build())
                .layer(new RnnOutputLayer.Builder().
                        activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .nOut(3)
                        .build())
                .build();
    }
}