package com.elena.chat;

import com.elena.chat.client.ClientController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {

    ClientController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("/sample.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 450);
        controller = fxmlLoader.getController();
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            controller.dispose();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}



