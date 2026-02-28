package com.steven.theminautaur.game;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Barre HUD en jeu (score, vie, etc.).
 */
public final class HudBar extends HBox {

    private final Text infoText = new Text();

    public HudBar() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(20);
        infoText.setFont(Font.font("Serif", 14));
        infoText.setFill(Color.LIGHTGRAY);
        getChildren().add(infoText);
    }

    public void setInfo(String text) {
        infoText.setText(text);
    }
}
