package com.steven.theminautaur.game;

import com.steven.theminautaur.AppState;
import com.steven.theminautaur.SceneRouter;
import com.steven.theminautaur.entity.Minotaur;
import com.steven.theminautaur.entity.Runner;
import com.steven.theminautaur.maze.Maze;
import com.steven.theminautaur.maze.MazeGenerator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Vue du jeu : labyrinthe, runner, minotaure, HUD, input, popup ESC.
 */
public final class GameView {

    private static final int TILE = 32;
    private static final int MAZE_W = 25;
    private static final int MAZE_H = 19;

    private StackPane root;
    private Pane mazeLayer;
    private Maze maze;
    private Runner runner;
    private Minotaur minotaur;
    private InputManager input;
    private GameLoop loop;
    private HudBar hudBar;
    private StackPane overlayPopup;

    public StackPane build() {
        root = new StackPane();
        root.setStyle("-fx-background-color: black;");

        maze = MazeGenerator.generate(MAZE_W, MAZE_H);
        runner = new Runner();
        minotaur = new Minotaur();
        runner.setCellPosition(1, 1);
        minotaur.setCellPosition(MAZE_W - 2, MAZE_H - 2);

        mazeLayer = new Pane();
        mazeLayer.setPrefSize(MAZE_W * TILE, MAZE_H * TILE);
        drawMaze();
        placeEntity(runner);
        placeEntity(minotaur);

        hudBar = new HudBar();
        hudBar.setInfo("Mode: " + AppState.getMode() + " | Difficulté: " + AppState.getDifficulty());

        Pane gamePane = new Pane();
        gamePane.getChildren().add(mazeLayer);
        StackPane.setAlignment(mazeLayer, javafx.geometry.Pos.CENTER);

        root.getChildren().add(gamePane);
        root.getChildren().add(hudBar);
        StackPane.setAlignment(hudBar, javafx.geometry.Pos.TOP_LEFT);

        input = new InputManager();
        loop = new GameLoop();
        loop.setOnUpdate(this::update);
        setupKeys();

        return root;
    }

    public void start() {
        root.requestFocus();
        loop.start();
    }

    private void drawMaze() {
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                Rectangle r = new Rectangle(TILE, TILE);
                r.setLayoutX(x * TILE);
                r.setLayoutY(y * TILE);
                r.setFill(maze.get(x, y) == Maze.WALL ? Color.gray(0.2) : Color.gray(0.35));
                r.setStroke(Color.gray(0.5));
                mazeLayer.getChildren().add(r);
            }
        }
    }

    private void placeEntity(com.steven.theminautaur.entity.Entity e) {
        if (e.getView() == null) return;
        mazeLayer.getChildren().add(e.getView());
        updateEntityView(e);
    }

    private void updateEntityView(com.steven.theminautaur.entity.Entity e) {
        e.getView().setLayoutX(e.getCellX() * TILE + (TILE - 24) / 2.0);
        e.getView().setLayoutY(e.getCellY() * TILE + (TILE - 24) / 2.0);
    }

    private void update() {
        double dx = 0, dy = 0;
        if (input.isPressed(KeyCode.UP) || input.isPressed(KeyCode.W)) dy = -1;
        if (input.isPressed(KeyCode.DOWN) || input.isPressed(KeyCode.S)) dy = 1;
        if (input.isPressed(KeyCode.LEFT) || input.isPressed(KeyCode.A)) dx = -1;
        if (input.isPressed(KeyCode.RIGHT) || input.isPressed(KeyCode.D)) dx = 1;
        if (dx != 0 || dy != 0) {
            int nx = (int) (runner.getCellX() + dx);
            int ny = (int) (runner.getCellY() + dy);
            if (maze.isWalkable(nx, ny)) {
                runner.setCellPosition(nx, ny);
                updateEntityView(runner);
            }
        }
    }

    private void setupKeys() {
        root.setOnKeyPressed(e -> {
            input.keyDown(e.getCode());
            if (e.getCode() == KeyCode.ESCAPE) {
                if (overlayPopup != null && root.getChildren().contains(overlayPopup)) {
                    root.getChildren().remove(overlayPopup);
                    overlayPopup = null;
                } else {
                    showEscapePopup();
                }
                e.consume();
            }
        });
        root.setOnKeyReleased(e -> input.keyUp(e.getCode()));
    }

    private void showEscapePopup() {
        overlayPopup = new StackPane();
        Rectangle bg = new Rectangle(350, 120);
        bg.setFill(Color.color(0.15, 0.15, 0.15, 0.98));
        bg.setStroke(Color.DARKRED);
        javafx.scene.text.Text msg = new javafx.scene.text.Text("Quitter la partie ? (Entrée = oui, Esc = non)");
        msg.setFill(Color.LIGHTGRAY);
        overlayPopup.getChildren().addAll(bg, msg);
        StackPane.setAlignment(overlayPopup, javafx.geometry.Pos.CENTER);
        root.getChildren().add(overlayPopup);
        overlayPopup.requestFocus();
        overlayPopup.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) {
                loop.stop();
                SceneRouter.showMainMenu();
                k.consume();
            } else if (k.getCode() == KeyCode.ESCAPE) {
                root.getChildren().remove(overlayPopup);
                overlayPopup = null;
                k.consume();
            }
        });
    }
}
