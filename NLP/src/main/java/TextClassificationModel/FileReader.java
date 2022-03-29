package TextClassificationModel;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class FileReader {

    static List<Tweet> tweets = new ArrayList<>();

    static void csvToString(String path) {

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.US_ASCII)) {
            // read the first line from the text file
            br.readLine();
            String line;
            // loop until all lines are read
            while ((line = br.readLine()) != null) {
                String[] attributes = line.split("\\t");
                Tweet tweet = createTweet(attributes);
                tweets.add(tweet);
            }

            makeTextFile();

//          print text output
//            int num = 75;
//            System.out.println(tweets.get(num).getText());
//            System.out.println(tweets.get(num).getSentiment());
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static Tweet createTweet(String[] metadata){
        String text = metadata[0];
        int sentiment = 0;

        if (Objects.equals(metadata[2], "Positive")){
            sentiment = 0;
        } else if (Objects.equals(metadata[2], "Negative")){
            sentiment = 1;
        } else{
            sentiment = 2;
        }

        return new Tweet(text, sentiment);
    }

    private static void makeTextFile() throws IOException {
        FileWriter positive = new FileWriter("positive.txt");
        FileWriter negative = new FileWriter("negative.txt");
        FileWriter neutral = new FileWriter("neutral.txt");
        List<String> text = new ArrayList<>(tweets.size());

        int[] label = new int[tweets.size()];

        for(int i = 0; i< tweets.size(); i++){
            text.add(tweets.get(i).getText());
            label[i] = tweets.get(i).getSentiment();
        }

        List<String> pos = new ArrayList<>(text.size());
        List<String> neg = new ArrayList<>(text.size());
        List<String> neu = new ArrayList<>(text.size());

        for(int i = 0; i< text.size(); i++){
            if(label[i] == 0){
                pos.add(text.get(i));
            } else if (label[i] == 1){
                neg.add(text.get(i));
            } else {
                neu.add(text.get(i));
            }
        }

        for(String str: pos) {
            positive.write(str + System.lineSeparator());
        }

        for(String str: neg) {
            negative.write(str + System.lineSeparator());
        }

        for(String str: neu) {
            neutral.write(str + System.lineSeparator());
        }
    }
}

class Tweet {
    private final String text;
    private final int sentiment;

    public Tweet(String t, int s) {
        this.text = t;
        this.sentiment = s;
    }

    public String getText(){
        return text;
    }

    public int getSentiment(){
        return sentiment;
    }
}
