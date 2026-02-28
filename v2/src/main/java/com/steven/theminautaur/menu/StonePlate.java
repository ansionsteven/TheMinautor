package com.steven.theminautaur.menu;

import javafx.geometry.Pos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

/**
 * Plaque type pierre (menus) : rectangle coins arrondis, double bordure, ombre, grain.
 * Pas de Button JavaFX. États : idle, hover, selected, pressed.
 */
public final class StonePlate extends StackPane {

    private static final Random RND = new Random();
    private final Text label;
    private final Rectangle back;
    private final double wRef;
    private final double hRef;
    private final MenuLayout layout;
    private boolean selected;
    private boolean pressed;

    public StonePlate(MenuLayout layout, double wRef, double hRef, String text) {
        this.layout = layout;
        this.wRef = wRef;
        this.hRef = hRef;
        setPickOnBounds(true);

        back = new Rectangle();
        back.setArcWidth(layout.scale() * 8);
        back.setArcHeight(layout.scale() * 8);
        back.setFill(createStoneFill());
        back.setStrokeWidth(2);
        back.setStrokeType(StrokeType.OUTSIDE);
        back.setStroke(Color.DARKGRAY);
        DropShadow shadow = new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 18 * layout.scale(), 0.25, 0, 2);
        back.setEffect(shadow);

        label = new Text(text);
        label.setFont(MenuFonts.getSerif(14 * layout.scale()));
        label.setFill(Color.web("#8B0000"));
        label.setMouseTransparent(true);

        getChildren().addAll(back, label);
        setAlignment(Pos.CENTER);
        updateSize();
    }

    private void updateSize() {
        double w = layout.width(wRef);
        double h = layout.height(hRef);
        back.setWidth(w);
        back.setHeight(h);
        setMinSize(w, h);
        setPrefSize(w, h);
        setMaxSize(w, h);
    }

    public void bindLayout() {
        layout.update(getScene() != null ? getScene().getWidth() : MenuLayout.WREF,
            getScene() != null ? getScene().getHeight() : MenuLayout.HREF);
        updateSize();
    }

    private Paint createStoneFill() {
        return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.gray(0.25, 0.95)),
            new Stop(0.5, Color.gray(0.2, 0.95)),
            new Stop(1, Color.gray(0.15, 0.95)));
    }

    public void setSelected(boolean sel) {
        if (selected == sel) return;
        selected = sel;
        if (sel) {
            label.setFill(Color.web("#CC3333"));
            back.setEffect(new javafx.scene.effect.Glow(0.3));
        } else {
            label.setFill(Color.web("#8B0000"));
            back.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 18 * layout.scale(), 0.25, 0, 2));
        }
    }

    public void setPressed(boolean p) {
        pressed = p;
        setScaleX(p ? 0.98 : (selected ? 1.03 : 1));
        setScaleY(p ? 0.98 : (selected ? 1.03 : 1));
    }

    public void setHover(boolean h) {
        if (!selected) label.setFill(h ? Color.web("#AA2222") : Color.web("#8B0000"));
    }

    public Text getLabel() { return label; }
}
