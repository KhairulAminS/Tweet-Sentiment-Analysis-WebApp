package tweets;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class getTweets {

    private static String bearerToken = "AAAAAAAAAAAAAAAAAAAAAG4kaQEAAAAA5btWp9vwZ4DZvWh%2FZCRRsvd3jxc%3DUG0FNQ6zBRoQyBFzc2pjHWNHWg09UDJI0KXErgpFjXoOyuNwrY";
    static String url = "https://api.twitter.com/2/tweets/search/stream?tweet.fields=created_at,text&expansions=author_id&&user.fields=id,name,username,profile_image_url";
    static String ruleURL = "https://api.twitter.com/2/tweets/search/stream/rules";
//    static String sampleURL = "https://api.twitter.com/2/tweets/sample/stream?&expansions=author_id";
    static List<String> rules = new ArrayList<>();
    public static List<Tweet> tweets = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {

        URL obj = new URL(url);
        setRules("stevejobs");
        setRules("cats");
        postRules();
        System.out.println("::::::::  Start Streaming  ::::::::");
        getStream(obj);
    }

    private static void deleteRules() throws IOException{

//        String data = "{ \"delete\": { \"ids\": [\"1504370686369796097 \"]}}";

        System.out.println(":::::::  Deleting Existed Rules  :::::::");
        URL url = new URL(ruleURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);
        System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        String data = null;

        while ((output = reader.readLine()) != null) {
            data = getFormattedString(output);
        }

        assert data != null;
        postHTTP(url, data);
        System.out.println(data);
        reader.close();
        con.disconnect();

    }

    public static void getStream(URL url) throws IOException, InterruptedException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);
        System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;

        while ((output = reader.readLine()) != null) {
            System.out.println(output);
            if(!output.equals("")) {
                saveTweets(output);
            }
            System.out.println("\n");
            long millis = System.currentTimeMillis();
            Thread.sleep(2000 - millis % 1000);

        }
        reader.close();
        con.disconnect();
    }

    private static void saveTweets(String output) {
        JSONObject object = new JSONObject(output);

        String id = object.getJSONObject("data").getString("id");
        String name = object.getJSONObject("includes").getJSONArray("users").getJSONObject(0).getString("name");
        String username = object.getJSONObject("includes").getJSONArray("users").getJSONObject(0).getString("username");
        String picURL = object.getJSONObject("includes").getJSONArray("users").getJSONObject(0).getString("profile_image_url");
        String text = object.getJSONObject("data").getString("text");
        String createdAt = object.getJSONObject("data").getString("created_at");
//        System.out.println(Arrays.asList(id, name, username, picURL, text, createdAt));
        Tweet tweet = new Tweet(id,name,username,picURL,text,createdAt);
        tweets.add(tweet);

        if (tweets.size() >= 10) {
            tweets.remove(0);
        }

    }

    public static void postHTTP(URL url, String data) throws IOException {
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-type", "application/json");
        http.setRequestProperty("Authorization", "Bearer " + bearerToken);

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();
    }

    private static String getFormattedString(String output){
        JSONObject json = new JSONObject(output);
        JSONObject deleteJSON = new JSONObject();
        List<String> ids = new ArrayList<>();
        int count = 0;

        count = json.getJSONObject("meta").getInt("result_count");
        while (count != 0){
            ids.add(String.valueOf(json.getJSONArray("data").getJSONObject(count-1).getString("id")));
            count--;
        }

        System.out.println(ids);
        System.out.println(json);
        deleteJSON.put("delete", new JSONObject().put("ids", ids));
        System.out.println(deleteJSON);

        return deleteJSON.toString();
    }

    public static void setRules(String searchValue){
        String header = "{ \"value\" : \"";
        String footer = " -is:retweet -has:links\" }";
        String body = String.join("", header, searchValue, footer);
        rules.add(body);
//        System.out.println(body);
    }

    public static void postRules() throws IOException {
        deleteRules();
        System.out.println(":::::::  Adding New Rules  :::::::");
        String header = "{\n \"add\": \n";
        String footer = "\n}";
        String rulesToPost = String.join("", header, rules.toString(), footer);
//        System.out.println(rulesToPost);
        postHTTP(new URL(ruleURL), rulesToPost);
        rules.clear();
    }


}