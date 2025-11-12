package com.example.farmSimulation;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo và bắt đầu game
        Game game = new Game();
        game.start(primaryStage);
    }
}
