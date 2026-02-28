package com.steven.theminautaur.overlay;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Overlay plein écran (ex: popup confirmation ESC en jeu).
 */
public final class ScreenOverlay {

    public static StackPane confirmation(StackPane parent, String message, Runnable onConfirm, Runnable onCancel) {
        Rectangle bg = new Rectangle(400, 150);
        bg.setFill(Color.color(0.1, 0.1, 0.1, 0.95));
        bg.setStroke(Color.DARKRED);
        Text msg = new Text(message);
        msg.setFill(Color.LIGHTGRAY);
        StackPane box = new StackPane(bg, msg);
        box.setStyle("-fx-background-color: transparent;");
        parent.getChildren().add(box);
        StackPane.setAlignment(box, javafx.geometry.Pos.CENTER);
        return box;
    }
}
