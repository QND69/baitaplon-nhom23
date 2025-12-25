package com.example.farmSimulation.view;

import com.example.farmSimulation.config.SettingsMenuConfig;
import com.example.farmSimulation.config.WindowConfig;
import com.example.farmSimulation.model.SaveManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.BiConsumer;

/**
 * Character Creation Screen - Cho phép người chơi tạo nhân vật trước khi bắt đầu game
 */
public class CharacterCreationView {
    private final VBox root;
    private final TextField nameField;
    private final ComboBox<String> genderComboBox;
    private final Label errorLabel;
    private BiConsumer<String, String> onStartGameCallback; // Callback: (name, gender)
    private Runnable onLoadGameCallback; // Callback cho Load Game

    public CharacterCreationView() {
        // Root container
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: rgba(30, 30, 50, 0.95);");

        // Title
        Label titleLabel = new Label("Create Your Farmer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(one-pass-box, black, 3, 0, 0, 2);");

        // Form container
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(400);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15;");

        // Name field
        Label nameLabel = new Label("Name:");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);

        nameField = new TextField();
        nameField.setPromptText("Enter your farmer's name");
        nameField.setPrefWidth(300);
        nameField.setPrefHeight(35);
        nameField.setFont(Font.font("Arial", 16));
        nameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 5;");

        // Gender selection
        Label genderLabel = new Label("Gender:");
        genderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        genderLabel.setTextFill(Color.WHITE);

        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female", "LGBT 🫵❓");
        genderComboBox.setValue("Male"); // Default value
        genderComboBox.setPrefWidth(300);
        genderComboBox.setPrefHeight(35);
        // Set emoji-friendly font để hỗ trợ hiển thị emoji đúng cách
        genderComboBox.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9); " +
                        "-fx-background-radius: 5; " +
                        "-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'System';"
        );

        // Cũng áp dụng font cho cells của ComboBox
        genderComboBox.setCellFactory(listView -> {
            javafx.scene.control.ListCell<String> cell = new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'System';");
                    }
                }
            };
            return cell;
        });

        // Áp dụng font cho button cell (hiển thị giá trị đã chọn)
        genderComboBox.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'System';");
                }
            }
        });

        // Error label (initially hidden)
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Arial", 14));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Start Game button (Đã đổi tên)
        Button startButton = new Button(SettingsMenuConfig.START_NEW_GAME_TEXT);
        startButton.setPrefWidth(200);
        startButton.setPrefHeight(45);
        startButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 5;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;"));

        startButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String gender = genderComboBox.getValue();

            // Validate name
            if (name.isEmpty()) {
                errorLabel.setText("Please enter a name!");
                errorLabel.setVisible(true);
                return;
            }

            // Hide error if valid
            errorLabel.setVisible(false);

            // Call callback to start game
            if (onStartGameCallback != null) {
                onStartGameCallback.accept(name, gender);
            }
        });

        // [MỚI] Load Game button
        Button loadButton = new Button(SettingsMenuConfig.LOAD_BUTTON_TEXT);
        loadButton.setPrefWidth(200);
        loadButton.setPrefHeight(45);
        loadButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Kiểm tra file save để enable/disable nút
        if (SaveManager.hasSaveFile()) {
            loadButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
            loadButton.setOnMouseEntered(e -> loadButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-background-radius: 5;"));
            loadButton.setOnMouseExited(e -> loadButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;"));
            loadButton.setOnAction(e -> {
                if (onLoadGameCallback != null) {
                    onLoadGameCallback.run();
                }
            });
        } else {
            loadButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-background-radius: 5;");
            loadButton.setDisable(true); // Disable nếu không có file save
            loadButton.setText("NO SAVE FOUND");
        }

        // Add components to form
        formBox.getChildren().addAll(
                nameLabel, nameField,
                genderLabel, genderComboBox,
                errorLabel,
                startButton,
                loadButton // Thêm nút Load vào form
        );

        // Add to root
        root.getChildren().addAll(titleLabel, formBox);
    }

    /**
     * Set callback được gọi khi người chơi click "Start Game"
     * @param callback BiConsumer nhận (name, gender)
     */
    public void setOnStartGame(BiConsumer<String, String> callback) {
        this.onStartGameCallback = callback;
    }

    /**
     * [MỚI] Set callback cho nút Load Game
     */
    public void setOnLoadGame(Runnable callback) {
        this.onLoadGameCallback = callback;
    }

    /**
     * Tạo Scene từ view này
     * @return Scene với CharacterCreationView
     */
    public Scene createScene() {
        return new Scene(root, WindowConfig.SCREEN_WIDTH, WindowConfig.SCREEN_HEIGHT);
    }

    /**
     * Lấy root container
     * @return VBox root
     */
    public VBox getRoot() {
        return root;
    }
}