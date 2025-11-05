package com.example.farmSimulation.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerView {
    private final double scale = 0.2; // tỉ lệ nhân vật
    private Image playerImage;
    private ImageView sprite;
    private double width;
    private double height;
    private String playerPath = "/assets/images/entities/player2Draft.png";

    public PlayerView() {
        playerImage = new Image(getClass().getResourceAsStream(playerPath));

        this.sprite = new ImageView(playerImage);
        this.width = playerImage.getWidth() * scale;
        this.height = playerImage.getHeight() * scale;

        sprite.setFitWidth(this.width);
        sprite.setFitHeight(this.height);
        sprite.setPreserveRatio(true); // giữ đúng tỉ lệ ảnh
    }
}
