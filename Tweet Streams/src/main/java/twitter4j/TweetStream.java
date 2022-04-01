package twitter4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class TweetStream {

    private static String bearerToken = "AAAAAAAAAAAAAAAAAAAAAG4kaQEAAAAA5btWp9vwZ4DZvWh%2FZCRRsvd3jxc%3DUG0FNQ6zBRoQyBFzc2pjHWNHWg09UDJI0KXErgpFjXoOyuNwrY";
    static String url = "https://api.twitter.com/2/tweets/search/stream?&expansions=author_id";
    static String ruleURL = "https://api.twitter.com/2/tweets/search/stream/rules";
    static String sampleURL = "https://api.twitter.com/2/tweets/sample/stream?&expansions=author_id";

    public static void main(String[] args) throws IOException, InterruptedException {

        URL obj = new URL(url);
        String data = "{\n  \"add\": [\n {\"value\": \"elonmusk\"}, {\"value\": \"cars  -has:links\" }\n ]\n}";
//        String data = "{\n  \"add\": [\n  {\"value\": \"ukraine -is:retweet -has:links lang:en\" } ]\n}";
        deleteRules(new URL(ruleURL));
        postHTTP(new URL(ruleURL), data);
        getHTTP(new URL(ruleURL));
        getHTTP(obj);
    }

    private static void deleteRules(URL url) throws IOException, InterruptedException {
        List<String> existingRules = new ArrayList<>();

//        String data = "{ \"delete\": { \"ids\": [\"1504370686369796097 \"]}}";

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken);
        System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        String data = null;

        while ((output = reader.readLine()) != null) {
            existingRules.add(output);
            data = getFormattedString(output);
            long millis = System.currentTimeMillis();
            Thread.sleep(1000 - millis % 1000);
        }

        postHTTP(url, data);
        reader.close();
        con.disconnect();

    }

    public static void getHTTP(URL url) throws IOException, InterruptedException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + bearerToken.toString());
        System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;

        while ((output = reader.readLine()) != null) {
            System.out.println(output);
            System.out.println("\n");
            long millis = System.currentTimeMillis();
            Thread.sleep(1000 - millis % 1000);
        }
        reader.close();
        con.disconnect();
    }

    public static void postHTTP(URL url, String data) throws IOException {
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-type", "application/json");
        http.setRequestProperty("Authorization", "Bearer " + bearerToken.toString());

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

}


