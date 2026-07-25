package com.eigenbound;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(
                loadFXML("vector-laboratory"),
                WINDOW_WIDTH,
                WINDOW_HEIGHT);

        stage.setTitle(
                "Eigenbound — Valley of Vectors");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fileName)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource(
                        fileName + ".fxml"));

        return loader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}