package com.example.farmSimulation.view.assets;

import com.example.farmSimulation.config.AssetPaths;
import com.example.farmSimulation.model.Tile;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Tải và quản lý tất cả tài nguyên
public class AssetManager {
    // Cache là một cơ chế lưu trữ tạm thời dữ liệu đã tính toán hoặc đã tải về
    // để sử dụng lại sau này, nhằm tăng tốc độ truy xuất và giảm tài nguyên tiêu thụ.

    // Cache cho các ảnh <key, value> (key là đường dẫn)
    private final Map<String, Image> textureCache = new HashMap<>();

    // Cache đặc biệt cho Tile (tối ưu cho Enum)
    private final Map<Tile, Image> tileTextureMap = new EnumMap<>(Tile.class);

    /**
     * Tải TOÀN BỘ tài nguyên game vào bộ nhớ.
     * Sẽ được gọi 1 lần duy nhất khi game bắt đầu.
     */
    public void loadAssets() {
        // Tải Player
        getTexture(AssetPaths.PLAYER_SHEET);

        // Tải GUI
        getTexture(AssetPaths.LOGO);

        // Tải Tile Textures
        Image grass = getTexture(AssetPaths.GRASS);
        Image soil = getTexture(AssetPaths.SOIL);
        Image water = getTexture(AssetPaths.WATER);

        // Liên kết Tile (Model) với Image (View)
        tileTextureMap.put(Tile.GRASS, grass);
        tileTextureMap.put(Tile.SOIL, soil);
        tileTextureMap.put(Tile.WATER, water);
    }

    /**
     * Lấy một ảnh từ cache. Nếu chưa có, nó sẽ tải và lưu lại.
     *
     * @param path Đường dẫn từ class AssetPaths
     */
    public Image getTexture(String path) {
        // computeIfAbsent là một method của Map (Java 8+).
        // computeIfAbsent: Nếu 'path' chưa có trong cache,
        // nó sẽ chạy hàm lambda (v -> new Image(...)) để tải ảnh,
        // sau đó tự động 'put' vào cache và return ảnh đó.
        // Cú pháp nếu ko dùng ->: V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
        return textureCache.computeIfAbsent(path, p ->
                new Image(getClass().getResourceAsStream(p))
        );
    }

    /**
     * Lấy ảnh texture cho một loại Tile (GRASS, SOIL, etc.)
     * Siêu nhanh vì dùng EnumMap.
     */
    public Image getTileTexture(Tile tileType) {
        // Trả về ảnh mặc định (GRASS) nếu không tìm thấy
        return tileTextureMap.getOrDefault(tileType, tileTextureMap.get(Tile.GRASS));
    }
}