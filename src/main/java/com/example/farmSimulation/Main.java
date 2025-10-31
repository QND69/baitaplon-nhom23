package com.example.farmSimulation;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.view.MainGame;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Farm Simulation");
        //  primaryStage.show();

        // 1. Tạo Model
        Player mainPlayer = new Player("Bob", 100, 100.0);
        MainGame mainView = new MainGame(); // View
        GameController gameController = new GameController(); // Controller

        // 2. Tạo GameManager và "tiêm" các thành phần kia vào
        GameManager gameManager = new GameManager(mainPlayer, mainView, gameController);

        // 3. Khởi tạo UI (sẽ tự động gán Input Listener)
        mainView.initUI(primaryStage, gameController);

        // 4. BẮT ĐẦU GAME
        gameManager.startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
