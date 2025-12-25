package com.example.farmSimulation.model;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE_NAME = "farm_save.dat";

    // Hàm Lưu: Nhận vào GameSaveState và ghi ra file
    public static void saveGame(GameSaveState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
            oos.writeObject(state);
            System.out.println("Game Saved Successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save game.");
        }
    }

    // Hàm Tải: Đọc file và trả về GameSaveState
    public static GameSaveState loadGame() {
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) return null; // Chưa có file save

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameSaveState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Kiểm tra xem có file save không (để hiện nút Continue)
    public static boolean hasSaveFile() {
        return new File(SAVE_FILE_NAME).exists();
    }
}