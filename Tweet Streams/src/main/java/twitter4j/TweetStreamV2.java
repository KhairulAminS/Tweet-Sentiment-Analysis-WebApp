package twitter4j;

public class TweetStreamV2 {
    public static void main(String[] args) throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        final TweetsResponse tweets = GetTweetsKt.getTweets(twitter,
                new long[]{656974073491156992L},
                null, null, null, null, null, "");
        System.out.println("tweets = " + tweets);

        TweetStreamV2 ts = new TweetStreamV2();




        //--------------------------------------------------
        // getUsers example
        //--------------------------------------------------
        final long twitterDesignId = 87532773L;
        final UsersResponse users = GetUsersKt.getUsers(twitter, new long[]{twitterDesignId}, null, null, "");
        System.out.println("users = " + users);
    }
}
