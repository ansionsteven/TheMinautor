package com.steven.theminautaur;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée. Configure la fenêtre et affiche le menu principal.
 */
public final class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneRouter.init(primaryStage);
        loadSavedAvatar();
        SceneRouter.showMainMenu();
        primaryStage.show();
    }

    private void loadSavedAvatar() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainApp.class);
            int saved = prefs.getInt("avatarIndex", 1);
            if (saved >= 1 && saved <= 8) AppState.setAvatarIndex(saved);
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
