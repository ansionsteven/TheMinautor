package com.steven.theminautaur.menu;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Random;

/**
 * Couches communes aux menus : fond (cover), vignette, pulse rouge, fog procédural.
 */
public final class HorrorBackground {

    private static final Random RND = new Random();
    private static final String[] BG_PATHS = {
        "assets/ui/bg_main.png",
        "assets/ui/bg_classique.png",
        "assets/ui/bg_multi.png",
        "assets/ui/bg_survival.png",
        "assets/ui/bg_bonus.png",
        "assets/ui/menu_fade.png"
    };

    /** Construit un StackPane avec BackgroundImageLayer, VignetteLayer, FogLayer (ordre bas -> haut). */
    public static StackPane buildLayers(double width, double height, String preferredBgKey) {
        StackPane root = new StackPane();
        root.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        root.setPrefSize(width, height);
        root.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        // 1) Background
        Image bgImage = loadBackground(preferredBgKey);
        if (bgImage != null) {
            ImageView bgView = new ImageView(bgImage);
            bgView.setPreserveRatio(true);
            bgView.setSmooth(true);
            StackPane.setAlignment(bgView, javafx.geometry.Pos.CENTER);
            root.widthProperty().addListener((o, a, n) -> fitCover(bgView, n.doubleValue(), height));
            root.heightProperty().addListener((o, a, n) -> fitCover(bgView, width, n.doubleValue()));
            fitCover(bgView, width, height);
            root.getChildren().add(bgView);
        } else {
            Rectangle fallback = new Rectangle(width, height);
            fallback.setFill(Color.BLACK);
            root.getChildren().add(fallback);
        }

        // 2) Vignette + pulse
        Pane vignetteLayer = new Pane();
        vignetteLayer.setPickOnBounds(false);
        vignetteLayer.setMinSize(width, height);
        vignetteLayer.setPrefSize(width, height);
        root.getChildren().add(vignetteLayer);

        // Vignette radiale (centre alpha 0, bords ~0.55)
        Rectangle vignette = new Rectangle(width, height);
        vignette.setFill(createVignetteFill(width, height));
        vignetteLayer.getChildren().add(vignette);

        // Pulse rouge (bords, 450-850 ms, alpha 0.05-0.25)
        Rectangle pulseRect = new Rectangle(width, height);
        pulseRect.setFill(Color.TRANSPARENT);
        pulseRect.setVisible(false);
        vignetteLayer.getChildren().add(pulseRect);
        startPulse(pulseRect, width, height, vignetteLayer);

        // 3) Fog (grain procédural, opacité 0.08-0.18, translation lente)
        Pane fogLayer = buildFogLayer(width, height);
        root.getChildren().add(fogLayer);

        return root;
    }

    private static Image loadBackground(String preferredKey) {
        String path = pathForKey(preferredKey);
        try (InputStream is = HorrorBackground.class.getResourceAsStream("/" + path)) {
            if (is != null) return new Image(is);
        } catch (Exception ignored) {}
        for (String p : BG_PATHS) {
            if (p.equals(path)) continue;
            try (InputStream is = HorrorBackground.class.getResourceAsStream("/" + p)) {
                if (is != null) return new Image(is);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String pathForKey(String key) {
        if (key == null) return BG_PATHS[BG_PATHS.length - 1];
        switch (key.toLowerCase()) {
            case "main": return "assets/ui/bg_main.png";
            case "classique": return "assets/ui/bg_classique.png";
            case "multi": return "assets/ui/bg_multi.png";
            case "survival": return "assets/ui/bg_survival.png";
            case "bonus": return "assets/ui/bg_bonus.png";
            default: return "assets/ui/menu_fade.png";
        }
    }

    private static void fitCover(ImageView iv, double w, double h) {
        Image img = iv.getImage();
        if (img == null || w <= 0 || h <= 0) return;
        double iw = img.getWidth();
        double ih = img.getHeight();
        double scale = Math.max(w / iw, h / ih);
        double vw = iw * scale;
        double vh = ih * scale;
        iv.setViewport(new Rectangle2D(0, 0, iw, ih));
        iv.setFitWidth(vw);
        iv.setFitHeight(vh);
    }

    private static RadialGradient createVignetteFill(double w, double h) {
        return new RadialGradient(0, 0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0, 0, 0, 0)),
            new Stop(1, Color.color(0, 0, 0, 0.55)));
    }

    private static void startPulse(Rectangle pulseRect, double w, double h, Pane parent) {
        pulseRect.setWidth(w);
        pulseRect.setHeight(h);
        pulseRect.setFill(Color.TRANSPARENT);
        // Effet bord uniquement : gradient radial inverse (centre transparent, bords rouge)
        RadialGradient pulseFill = new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.TRANSPARENT),
            new Stop(0.7, Color.TRANSPARENT),
            new Stop(1, Color.color(0.5, 0, 0, 0.15)));
        pulseRect.setFill(pulseFill);
        pulseRect.setVisible(true);
        Timeline t = new Timeline();
        t.setCycleCount(Animation.INDEFINITE);
        KeyFrame kf = new KeyFrame(Duration.millis(450 + RND.nextInt(401)), e -> {
            double alpha = 0.05 + RND.nextDouble() * 0.20;
            pulseRect.setOpacity(alpha);
        });
        t.getKeyFrames().add(kf);
        t.play();
    }

    private static Pane buildFogLayer(double width, double height) {
        Pane fog = new Pane();
        fog.setMinSize(width, height);
        fog.setPrefSize(width, height);
        fog.setPickOnBounds(false);

        int tile = 4;
        int cols = (int) Math.ceil(width / tile) + 2;
        int rows = (int) Math.ceil(height / tile) + 2;
        double opacity = 0.08 + RND.nextDouble() * 0.10;
        for (int i = 0; i < cols * rows; i++) {
            Rectangle r = new Rectangle(tile, tile);
            r.setFill(Color.gray(RND.nextDouble() * 0.3 + 0.2, opacity));
            r.setLayoutX((i % cols) * tile);
            r.setLayoutY((i / cols) * tile);
            fog.getChildren().add(r);
        }

        double dx = (RND.nextDouble() - 0.5) * 40;
        Timeline move = new Timeline(
            new KeyFrame(Duration.ZERO, e -> fog.setLayoutX(0)),
            new KeyFrame(Duration.seconds(8 + RND.nextDouble() * 6), e -> fog.setLayoutX(dx))
        );
        move.setCycleCount(Animation.INDEFINITE);
        move.setAutoReverse(true);
        move.play();

        return fog;
    }
}
