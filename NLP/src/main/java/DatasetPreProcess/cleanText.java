package DatasetPreProcess;

import org.nd4j.common.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class cleanText {

    static List<String> stopWord  = new ArrayList<>();
    static List<String> dataset = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String filePath1 = new ClassPathResource("Dataset/cleaned-filtered-dumping-wiki.txt").getFile().getAbsolutePath();
        String filePath2 = new ClassPathResource("Dataset/dumping-instagram.txt").getFile().getAbsolutePath();
        String stopWordPath = new ClassPathResource("Dataset/StopWord.txt").getFile().getAbsolutePath();
        FileWriter cleanedDataset = new FileWriter("cleanedDataset.txt");

        String line;

        BufferedReader br1 = Files.newBufferedReader(Paths.get(filePath1), StandardCharsets.US_ASCII);
        BufferedReader br2 = Files.newBufferedReader(Paths.get(filePath2), StandardCharsets.US_ASCII);
        BufferedReader br_stopword = Files.newBufferedReader(Paths.get(stopWordPath), StandardCharsets.US_ASCII);

        // setup stop word list
        while ((line = br_stopword.readLine()) != null) {
            stopWord.add(line.toLowerCase(Locale.ROOT));
        }

        System.out.println("....Begin Tokenizing....");

        System.out.println("....Tokenizing Wiki Dataset....");

        // loop until all lines are read
        while ((line = br1.readLine()) != null) {
            String text = removeLink(line);
            text = removeHandle(text);
            text = removeHashtag(text);
            text = removeStopWord(text);
            dataset.add(text);
        }

        System.out.println("....Tokenizing Instagram Dataset....");

        while ((line = br2.readLine()) != null) {
            String text = removeLink(line);
            text = removeHandle(text);
            text = removeHashtag(text);
            text = removeStopWord(text);
            dataset.add(text);
        }

        System.out.println("....Tokenizing Done....");
        System.out.println("....Print Example....");

        System.out.println(dataset.get(751));

        System.out.println("....Saving Dataset....");

        for(String str: dataset) {
            cleanedDataset.write(str + System.lineSeparator());
        }

        System.out.println("....Dataset Saved....");


        //Test example
//        String data = "Dana Haji Aman dikelola oleh badan yang Independen Jangan terpengaruh berita hoaks #StopHoaxDanaHaji";
//        data = removeLink(data);
//        data = removeHandle(data);
//        data = removeHashtag(data);
//        data = removeStopWord(data);
//        System.out.println(data);

    }

    public static String cleanDataset(String data) throws IOException {
        data = removeLink(data);
        data = removeHandle(data);
        data = removeHashtag(data);
        data = removeStopWord(data);
        return data;
    }

    private static String removeLink(String text){
        text = text.replaceAll("http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+", "");
        return text;
    }

    private static String removeHandle(String text){
        text = text.replaceAll("(\\s+|^)@\\w+", "");
        return text;
    }

    private static String removeHashtag(String text){
        text = text.replaceAll("(\\s+|^)#\\w+", "");
        return text;
    }

    private static String removeStopWord(String text) throws IOException {
        ArrayList<String> words = Stream.of(text.toLowerCase(Locale.ROOT).split(" "))
                .collect(Collectors.toCollection(ArrayList<String>::new));
        words.removeAll(stopWord);

        text = String.join(" ", words).trim();
        return text;
    }
}
