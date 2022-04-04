package com.example.application.views;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.io.ClassPathResource;

import java.io.IOException;

import static tweets.getTweets.*;


/**
 * The main view is a top-level placeholder for other views.
 */

@PageTitle("main")
@Route(value = "main")
@RouteAlias(value = "")
public class MainLayout extends VerticalLayout {

    public static TextField searchbar = new TextField();

    public static MultiLayerNetwork model;
    public static Word2Vec word2Vec;

    public MainLayout() throws IOException {

        model = ModelSerializer.restoreMultiLayerNetwork(new ClassPathResource("Models/LSTM model.zip").getFile());
        word2Vec = WordVectorSerializer.readWord2VecModel(new ClassPathResource("Models/word2vec.vector").getFile().getPath());


        Span header = new Span("Twitter Sentiment Analysis");
        header.setClassName("view-title");

        searchbar.setPlaceholder("eg.puasa");
        searchbar.setHelperText("Enter up to 5 topics (separated by comma)");
        searchbar.setWidth("30em");

        Button searchButton = new Button("Search");
        searchButton.addClassName("button");
        searchButton.addClickListener(buttonClickEvent -> UI.getCurrent().navigate("sentiment"));

        setSpacing(true);
        setSizeFull();
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(header, searchbar, searchButton);
    }
}
