package com.eigenbound;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX entry point for Eigenbound.
 */
public class App extends Application {

    private static final double WINDOW_WIDTH = 1180;
    private static final double WINDOW_HEIGHT = 720;

    private static Scene scene;

    /**
     * Creates the primary scene and opens the expedition map.
     *
     * @param stage primary JavaFX stage
     * @throws IOException when the initial FXML cannot be loaded
     */
    @Override
    public void start(Stage stage)
            throws IOException {

        scene = new Scene(
                loadFXML("expedition-map"),
                WINDOW_WIDTH,
                WINDOW_HEIGHT);

        stage.setTitle("Eigenbound");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Replaces the current scene root with another FXML view.
     *
     * @param fxmlName FXML filename without extension
     * @throws IOException when the view cannot be loaded
     */
    public static void setRoot(String fxmlName)
            throws IOException {

        if (scene == null) {
            throw new IllegalStateException(
                    "Application scene has not been initialized");
        }

        scene.setRoot(
                loadFXML(fxmlName));
    }

    /**
     * Loads an FXML resource from the application package.
     */
    private static Parent loadFXML(
            String fxmlName) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource(
                        fxmlName + ".fxml"));

        return loader.load();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}