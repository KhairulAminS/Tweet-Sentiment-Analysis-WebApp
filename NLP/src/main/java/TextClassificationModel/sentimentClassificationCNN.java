package TextClassificationModel;

import org.apache.commons.io.FilenameUtils;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator;
import org.deeplearning4j.iterator.LabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.CollectionLabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.graph.ComputationGraph;
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
import java.util.*;

import static TextClassificationModel.FileReader.tweets;

public class sentimentClassificationCNN {
    static String wordVectorsPath = new File("word2vec_ms_wiki.vector").getAbsolutePath();

    static int batchSize = 64;     //Number of examples in each minibatch
    static int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
    static int nEpochs = 500;        //Number of epochs (full passes of training data) to train on
    static int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
    static final int seed = 123;     //Seed for reproducibility

    static int cnnLayerFeatureMaps = 256;
    static PoolingType globalPoolingType = PoolingType.MAX;
    static Random rng = new Random(seed);

    public static void main(String[] args) throws IOException {
        File file = new ClassPathResource("data.csv").getFile();
        FileReader.csvToString(file.getPath());

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(wordVectorsPath));
        DataSetIterator allData = getDataSetIterator(wordVectors, batchSize, truncateReviewsToLength, rng);

        DataSet fullDataset = allData.next();

        SplitTestAndTrain testAndTrain = fullDataset.splitTestAndTrain(0.8);

        DataSet trainDataset = testAndTrain.getTrain();
        DataSet testDataset = testAndTrain.getTest();

        DataSetIterator trainIterator = new ViewIterator(trainDataset, trainDataset.numExamples());
        DataSetIterator testIterator = new ViewIterator(testDataset, testDataset.numExamples());


        ComputationGraph model = new ComputationGraph(config());
        model.init();

        StatsStorage statsStorage = new InMemoryStatsStorage();
        UIServer server = UIServer.getInstance();
        server.attach(statsStorage);
        model.setListeners(
                new StatsListener( statsStorage),
                new ScoreIterationListener(10));

        System.out.println("......Start training......");

        for(int i=0; i < nEpochs; i++) {
            model.fit(trainIterator);
        }

        Evaluation trainEval = model.evaluate(trainIterator);
        Evaluation testEval = model.evaluate(testIterator);

        System.out.println(trainEval);
        System.out.println(testEval);


        System.out.println(".....Saving model.....");
        ModelSerializer.writeModel(model, "CNN model.zip", true);
        System.out.println(".....Model saved.....");
    }

    private static ComputationGraphConfiguration config(){
        return new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.RELU)
                .activation(Activation.LEAKYRELU)
                .updater(new Adam(1e-3))
                .convolutionMode(ConvolutionMode.Same)      //This is important so we can 'stack' the results later
                .l2(0.05)
                .graphBuilder()
                .addInputs("input")
                .addLayer("cnn3", new ConvolutionLayer.Builder()
                        .kernelSize(3, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                .addLayer("cnn4", new ConvolutionLayer.Builder()
                        .kernelSize(4, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                //MergeVertex performs depth concatenation on activations: 3x[minibatch,100,length,300] to 1x[minibatch,300,length,300]
                .addVertex("merge", new MergeVertex(), "cnn3", "cnn4")
                //Global pooling: pool over x/y locations (dimensions 2 and 3): Activations [minibatch,300,length,300] to [minibatch, 300]
                .addLayer("globalPool", new GlobalPoolingLayer.Builder()
                        .poolingType(globalPoolingType)
                        .dropOut(0.5)
                        .build(), "merge")
                .addLayer("out", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nOut(3)    //2 classes: positive or negative
                        .build(), "globalPool")
                .setOutputs("out")
                //Input has shape [minibatch, channels=1, length=1 to 256, 300]
                .setInputTypes(InputType.convolutional(truncateReviewsToLength, vectorSize, 1))
                .build();
    }

    private static DataSetIterator getDataSetIterator(WordVectors wordVectors, int minibatchSize, int maxSentenceLength, Random rng) {

        List<String> tweetText = new ArrayList<>();
        List<String> label = new ArrayList<>();

        for(int i=0; i< tweets.size(); i++){
            tweetText.add(tweets.get(i).getText());
            label.add(String.valueOf(tweets.get(i).getSentiment()));
        }

        LabeledSentenceProvider sentenceProvider = new CollectionLabeledSentenceProvider(tweetText, label, rng);

        return new CnnSentenceDataSetIterator.Builder(CnnSentenceDataSetIterator.Format.CNN2D)
                .sentenceProvider(sentenceProvider)
                .wordVectors(wordVectors)
                .minibatchSize(minibatchSize)
                .maxSentenceLength(maxSentenceLength)
                .useNormalizedWordVectors(false)
                .build();
    }
}
