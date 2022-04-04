package com.example.application.views.sentimentanalysis;

import org.nd4j.common.io.ClassPathResource;

import java.io.BufferedReader;
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

    public static String cleanTweetText(String data) throws IOException {
        if(stopWord.isEmpty()){
            String stopWordPath = new ClassPathResource("Models/StopWord.txt").getFile().getAbsolutePath();
            BufferedReader br_stopword = Files.newBufferedReader(Paths.get(stopWordPath), StandardCharsets.US_ASCII);
            String line;
            while ((line = br_stopword.readLine()) != null) {
                stopWord.add(line.toLowerCase(Locale.ROOT));
            }
        }

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
