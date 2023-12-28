package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicPlayerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Test.fxml"));
        primaryStage.setTitle("Music Player");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


}
