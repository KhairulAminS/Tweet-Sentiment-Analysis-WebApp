package com.example.application.views.sentimentanalysis;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import tweets.Tweet;

import java.io.IOException;

import static Inference.getSentiment.getTweetsSentiment;
import static com.example.application.views.MainLayout.*;
import static tweets.getTweets.*;
import static tweets.getTweets.getStream;

@PageTitle("Sentiment Analysis")
@Route(value = "sentiment")
@RouteAlias(value = "sentiment")
public class SentimentAnalysisView extends Div implements AfterNavigationObserver {

    Grid<Tweet> grid = new Grid<>();
    static String topics;
    static String[] allTopics;

    public SentimentAnalysisView() throws IOException, InterruptedException {
        addClassName("main-view");
        setSizeFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);

        setupStream();

        getStream();

        grid.addColumn(tweets -> {
            try {
                return createCard(tweets);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        add(grid);
    }

    private HorizontalLayout createCard(Tweet tweet) throws IOException {
        HorizontalLayout card = new HorizontalLayout();

        String sentiment = getTweetsSentiment(tweet.getText(), model, word2Vec);

        if(sentiment.equals("Positive")){
            card.addClassName("positive");
        } else if (sentiment.equals("Negative")){
            card.addClassName("negative");
        } else{
            card.addClassName("neutral");
        }

        card.setSpacing(false);
        card.getThemeList().add("spacing-s");


        Image image = new Image();
        image.setSrc(tweet.getImageURL());
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span name = new Span(tweet.getName());
        name.addClassName("name");
        Span username = new Span(tweet.getUsername());
        username.addClassName("username");
//            Span date = new Span(person.getDate());
//            date.addClassName("date");
        header.add(name, username);

        Span post = new Span(tweet.getText());
        post.addClassName("post");

        description.add(header, post);
        card.add(image, description);
        return card;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        System.out.println("after navigation");
        grid.setItems(tweets);
    }

    private void setupStream() throws IOException {
        topics = searchbar.getValue();
        allTopics = getSearchTopics(topics);
        for(String s:allTopics){
            setRules(s);
        }
        System.out.println(":::::: Posting Rules ::::::");
        postRules();
        System.out.println(":::::: New Rules Posted ::::::");
        System.out.println(":::::: Start tweet stream ::::::");
    }

    private String[] getSearchTopics(String topics){
        return topics.split(",",0);
    }

}
