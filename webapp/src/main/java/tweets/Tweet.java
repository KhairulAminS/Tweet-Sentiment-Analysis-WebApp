package tweets;

public class Tweet {

    String id;
    String name;
    String username;
    String imageURL;
    String text;
    String createdTime;

    public Tweet(String id, String name, String username, String imageURL, String text, String createdTime){
        this.id = id;
        this.name = name;
        this.username = "@" + username;
        this.imageURL = imageURL;
        this.text = text;
        this.createdTime = createdTime;
    }



    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getImageURL() {return imageURL;}

    public void setImageURL(String imageURL) {this.imageURL = imageURL;}

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public String getCreatedTime() {return createdTime;}

    public void setCreatedTime(String createdTime) {this.createdTime = createdTime;}
}
