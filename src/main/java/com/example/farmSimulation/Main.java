package com.example.farmSimulation;

import com.example.farmSimulation.controller.GameController;
import com.example.farmSimulation.model.GameManager;
import com.example.farmSimulation.model.Player;
import com.example.farmSimulation.view.MainGameView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Player mainPlayer = new Player(); // Tạo người chơi

        GameController gameController = new GameController(); // Tạo controller (điều khiển input và logic)

        MainGameView mainGameView = new MainGameView(); // Tạo view (UI game chính)

        GameManager gameManager = new GameManager(mainPlayer, gameController, mainGameView); // Tạo bộ não quản lý logic game (GameManager)

        gameController.setGameManager(gameManager);

        // ---  BẮT ĐẦU GAME  ---


        // Khởi tạo giao diện (hàm này nằm ở class MainGame (view) )
        mainGameView.initUI(primaryStage, gameController);

        // Liên kết controller ↔ view (hàm này nằm ở class GameController (controller) )
        //gameController.setMainGameView(mainGameView);

        // Khởi tạo game loop (hàm này nằm ở class GameManager (model) )
        gameManager.startGame();
    }
}
