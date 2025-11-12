package com.example.farmSimulation.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// Chứa TOÀN BỘ đường dẫn đến tài nguyên (assets)
public final class AssetPaths {

    // Player
    public static final String PLAYER_SHEET = "/assets/images/entities/player/player_scaled_4x_pngcrushed.png";

    // World Tiles
    public static final String GRASS = "/assets/images/world/grassDraft.png";
    public static final String SOIL = "/assets/images/world/soilDraft.png";
    public static final String WATER = "/assets/images/world/waterDraft.png";

    // GUI
    public static final String LOGO = "/assets/images/GUI/logo.png";
}