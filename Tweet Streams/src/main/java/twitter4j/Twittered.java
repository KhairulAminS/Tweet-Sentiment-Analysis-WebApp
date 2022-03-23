package twitter4j;

import com.github.scribejava.core.model.Response;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.testng.AssertJUnit.assertNotNull;


public class Twittered {

    static TwitterClient twitterClient = new TwitterClient(TwitterCredentials.builder()
            .accessToken("INSERT ACCESS TOKEN HERE")
            .accessTokenSecret("INSERT ACCESS SECRET TOKEN HERE")
            .apiKey("INSERT API KEY HERE")
            .apiSecretKey("INSERT API SECRET KEY HERE")
            .build());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        twitterClient.addFilteredStreamRule("value", "elonmusk -has:links lang:ja");
        testStartStream();
    }

    public static void testStartStream() throws InterruptedException, ExecutionException {
        Future<Response> future = twitterClient.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(int i, String s) {
                System.err.println("stopping stream... ");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.err.println("restarting after 5min sleep");
            }

            @Override
            public void onTweetStreamed(Tweet tweet) {
                System.out.println(tweet.getText());
            }

            @Override
            public void onUnknownDataStreamed(String s) {

            }

            @Override
            public void onStreamEnded(Exception e) {

            }
        });
        try {
            System.out.println(future.get(5, TimeUnit.SECONDS));
        } catch (TimeoutException exc) {
            // It's OK
        }
        assertNotNull(future.get());
    }
}
