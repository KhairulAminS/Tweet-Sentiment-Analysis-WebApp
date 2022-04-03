package com.example.application.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.*;
import tweets.getTweets;

import java.io.IOException;
import java.util.Arrays;

import static tweets.getTweets.*;


/**
 * The main view is a top-level placeholder for other views.
 */

@PageTitle("main")
@Route(value = "main")
@RouteAlias(value = "main")
//@CssImport(value = "./webapp/main-layout.css", themeFor = "button-64")
public class MainLayout extends VerticalLayout {

    static String topics;
    static String[] allTopics;
    static TextField searchbar = new TextField();

    public MainLayout() {

        Span header = new Span("Twitter Sentiment Analysis");
        header.setClassName("view-title");

        searchbar.setPlaceholder("eg.puasa");
        searchbar.setHelperText("Enter up to 5 topics (separated by comma)");
        searchbar.setWidth("30em");

        Button searchButton = new Button("Search");
        searchButton.addClassName("button");
        searchButton.addClickListener(buttonClickEvent -> {
            try {
                startStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        setSpacing(true);
        setSizeFull();
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(header, searchbar, searchButton);
    }

    private void startStream() throws IOException {
        topics = searchbar.getValue();
        allTopics = getSearchTopics(topics);
        for(String s:allTopics){
            setRules(s);
        }
        postRules();
    }

    private String[] getSearchTopics(String topics){
        return topics.split(",",0);
    }
}
