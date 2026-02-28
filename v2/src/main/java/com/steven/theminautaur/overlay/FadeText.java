package com.steven.theminautaur.overlay;

import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Affiche un texte temporaire en fade-in / fade-out (ex: feedback avatar enregistré).
 */
public final class FadeText {

    public static void show(StackPane parent, String message, int durationMs) {
        Text t = new Text(message);
        t.setFont(Font.font("Serif", 18));
        t.setFill(Color.LIGHTGRAY);
        StackPane box = new StackPane(t);
        box.setMouseTransparent(true);
        parent.getChildren().add(box);
        StackPane.setAlignment(box, javafx.geometry.Pos.CENTER);
        box.setOpacity(0);

        FadeTransition in = new FadeTransition(Duration.millis(200), box);
        in.setFromValue(0);
        in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(300), box);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> parent.getChildren().remove(box));
        in.setOnFinished(e -> out.play());
        in.play();
    }
}
