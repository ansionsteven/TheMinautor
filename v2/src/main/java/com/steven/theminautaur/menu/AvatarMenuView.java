package com.steven.theminautaur.menu;

import com.steven.theminautaur.AppState;
import com.steven.theminautaur.SceneRouter;
import com.steven.theminautaur.overlay.FadeText;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu avatar : 8 avatars en grille, navigation clavier ←→↑↓ (wrap), Enter = valider et sauver Preferences.
 * Esc/Backspace = retour. Fallback si PNG absent : cercle coloré + disque noir.
 */
public final class AvatarMenuView {

    private static final double WREF = 2048;
    private static final double HREF = 1365;
    private static final int COLS = 4;
    private static final int ROWS = 2;
    private static final String[] AVATAR_PATHS = {
        "assets/avatars/a1.png", "assets/avatars/a2.png", "assets/avatars/a3.png", "assets/avatars/a4.png",
        "assets/avatars/a5.png", "assets/avatars/a6.png", "assets/avatars/a7.png", "assets/avatars/a8.png"
    };
    private static final Color[] FALLBACK_COLORS = {
        Color.DARKRED, Color.DARKBLUE, Color.DARKGREEN, Color.DARKGOLDENROD,
        Color.PURPLE, Color.TEAL, Color.MAROON, Color.NAVY
    };

    private MenuLayout layout;
    private int selectedIndex = AppState.getAvatarIndex() - 1;
    private final List<StackPane> cells = new ArrayList<>();
    private StackPane root;
    private Pane uiLayer;

    public StackPane build() {
        layout = new MenuLayout();
        double w = 1180;
        double h = 720;
        try {
            w = SceneRouter.getStage().getWidth();
            h = SceneRouter.getStage().getHeight();
        } catch (Exception ignored) {}
        layout.update(w, h);

        root = HorrorBackground.buildLayers(w, h, null);
        root.widthProperty().addListener((o, a, n) -> layout.update(n.doubleValue(), root.getHeight()));
        root.heightProperty().addListener((o, a, n) -> layout.update(root.getWidth(), n.doubleValue()));

        uiLayer = new Pane();
        root.getChildren().add(uiLayer);

        double cellW = 180 * layout.scale();
        double cellH = 180 * layout.scale();
        double startX = layout.x(600);
        double startY = layout.y(400);

        for (int i = 0; i < 8; i++) {
            int col = i % COLS;
            int row = i / COLS;
            StackPane cell = buildAvatarCell(i, cellW, cellH);
            cell.setLayoutX(startX + col * (cellW + 40));
            cell.setLayoutY(startY + row * (cellH + 40));
            final int idx = i;
            cell.setOnMouseEntered(e -> { selectedIndex = idx; syncSelection(); });
            cell.setOnMouseClicked(e -> applyAndFeedback());
            cells.add(cell);
            uiLayer.getChildren().add(cell);
        }

        syncSelection();
        setupKeyboard(root);

        return root;
    }

    private StackPane buildAvatarCell(int index, double w, double h) {
        StackPane cell = new StackPane();
        cell.setMinSize(w, h);
        cell.setPrefSize(w, h);

        Image img = null;
        try (InputStream is = AvatarMenuView.class.getResourceAsStream("/" + AVATAR_PATHS[index])) {
            if (is != null) img = new Image(is);
        } catch (Exception ignored) {}

        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            double s = Math.min(w / img.getWidth(), h / img.getHeight());
            iv.setFitWidth(img.getWidth() * s);
            iv.setFitHeight(img.getHeight() * s);
            Rectangle clip = new Rectangle(w, h);
            clip.setArcWidth(w);
            clip.setArcHeight(h);
            iv.setClip(new Circle(w / 2, h / 2, Math.min(w, h) / 2));
            cell.getChildren().add(iv);
        } else {
            double r = Math.min(w, h) / 2 - 4;
            Circle bg = new Circle(r);
            bg.setFill(FALLBACK_COLORS[index]);
            Circle inner = new Circle(r / 2);
            inner.setFill(Color.BLACK);
            cell.getChildren().addAll(bg, inner);
        }

        return cell;
    }

    private void syncSelection() {
        for (int i = 0; i < cells.size(); i++) {
            StackPane c = cells.get(i);
            if (c.getChildren().isEmpty()) continue;
            if (c.getChildren().get(0) instanceof ImageView) {
                ((ImageView) c.getChildren().get(0)).setEffect(i == selectedIndex
                    ? new javafx.scene.effect.Glow(0.4) : null);
            }
            c.setStyle(i == selectedIndex ? "-fx-border-color: #CC3333; -fx-border-width: 3; -fx-border-radius: 90;" : "");
        }
    }

    private void applyAndFeedback() {
        int choice = selectedIndex + 1;
        AppState.setAvatarIndex(choice);
        try {
            java.util.prefs.Preferences.userNodeForPackage(getClass()).put("avatarIndex", String.valueOf(choice));
        } catch (Exception ignored) {}
        FadeText.show(root, "Avatar enregistré", 1200);
    }

    private void setupKeyboard(StackPane root) {
        root.setFocusTraversable(true);
        root.requestFocus();
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.BACK_SPACE) {
                SceneRouter.fadeOutThen(SceneRouter::showMainMenu, this.root);
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                applyAndFeedback();
                e.consume();
            } else if (e.getCode() == KeyCode.LEFT) {
                selectedIndex = (selectedIndex - 1 + 8) % 8;
                syncSelection();
                e.consume();
            } else if (e.getCode() == KeyCode.RIGHT) {
                selectedIndex = (selectedIndex + 1) % 8;
                syncSelection();
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                selectedIndex = (selectedIndex - COLS + 8) % 8;
                syncSelection();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                selectedIndex = (selectedIndex + COLS) % 8;
                syncSelection();
                e.consume();
            }
        });
    }
}
