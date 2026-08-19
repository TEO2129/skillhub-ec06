package com.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/AuthView.fxml"));
        VBox root = loader.load();

        Scene scene = new Scene(root, 480, 520);
        scene.getStylesheets().add(
                getClass().getResource("/fxml/style.css").toExternalForm());

        primaryStage.setTitle("TP1 – Authentification");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}