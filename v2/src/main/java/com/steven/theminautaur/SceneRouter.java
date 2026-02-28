package com.steven.theminautaur;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Route les scènes : menu principal, avatar, config mode/bonus, jeu.
 * Gère les transitions fade et la fenêtre (min 1180x720, maximized).
 */
public final class SceneRouter {

    private static Stage stage;
    private static final double MIN_W = 1180;
    private static final double MIN_H = 720;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(MIN_W);
        stage.setMinHeight(MIN_H);
        stage.setMaximized(true);
        stage.setTitle("The Minautor");
    }

    public static Stage getStage() { return stage; }

    public static void showMainMenu() {
        Platform.runLater(() -> {
            com.steven.theminautaur.menu.MainMenuView view = new com.steven.theminautaur.menu.MainMenuView();
            StackPane root = view.build();
            applyScene(root, 220);
        });
    }

    public static void showAvatarMenu() {
        Platform.runLater(() -> {
            com.steven.theminautaur.menu.AvatarMenuView view = new com.steven.theminautaur.menu.AvatarMenuView();
            StackPane root = view.build();
            applyScene(root, 220);
        });
    }

    public static void showModeConfig(AppState.Mode mode) {
        Platform.runLater(() -> {
            com.steven.theminautaur.menu.ModeConfigView view = new com.steven.theminautaur.menu.ModeConfigView(mode);
            StackPane root = view.build();
            applyScene(root, 220);
        });
    }

    public static void showBonusConfig() {
        Platform.runLater(() -> {
            com.steven.theminautaur.menu.BonusConfigView view = new com.steven.theminautaur.menu.BonusConfigView();
            StackPane root = view.build();
            applyScene(root, 220);
        });
    }

    public static void showGame() {
        Platform.runLater(() -> {
            com.steven.theminautaur.game.GameView view = new com.steven.theminautaur.game.GameView();
            StackPane root = view.build();
            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            stage.setScene(scene);
            view.start();
        });
    }

    private static void applyScene(StackPane root, int fadeInMs) {
        root.setOpacity(0);
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        stage.setScene(scene);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(fadeInMs), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Fade-out puis callback (ex: showMainMenu). */
    public static void fadeOutThen(Runnable then, StackPane fromRoot) {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(160), fromRoot);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> then.run());
        ft.play();
    }
}
