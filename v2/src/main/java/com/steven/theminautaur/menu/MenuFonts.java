package com.steven.theminautaur.menu;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.InputStream;

/**
 * horror.ttf UNIQUEMENT pour les titres. Sinon Serif / Times.
 */
public final class MenuFonts {

    private static final String HORROR_PATH = "assets/fonts/horror.ttf";

    public static Font getTitleFont(double sizePx) {
        try (InputStream is = MenuFonts.class.getResourceAsStream("/" + HORROR_PATH)) {
            if (is != null) return Font.loadFont(is, sizePx);
        } catch (Exception ignored) {}
        return Font.font("Serif", sizePx);
    }

    public static Font getSerif(double sizePx) {
        return Font.font("Serif", sizePx);
    }

    /** Applique la police titre à un Text (horror.ttf). */
    public static void applyTitle(Text t, double sizeRef) {
        t.setFont(getTitleFont(sizeRef));
    }
}
