package com.steven.theminautaur.menu;

import com.steven.theminautaur.AppState;
import com.steven.theminautaur.SceneRouter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu principal : titre THE / THE MINOTAUR, items AVATAR, CLASSIQUE, MULTIJOUEUR, SURVIVALL, BONUS.
 * Navigation clavier ↑↓ (boucle), Enter valide, hover = underline, clic = valide.
 */
public final class MainMenuView {

    private static final double TITLE_X = 120;
    private static final double TITLE_Y = 70;
    private static final double ITEMS_X = 150;
    private static final double ITEMS_Y = 420;
    private static final double ITEM_SPACING = 92;
    private static final double HIGHLIGHT_W = 520;
    private static final double HIGHLIGHT_H = 55;

    private static final String[] ITEMS = {
        "AVATAR",
        "CLASSIQUE",
        "MULTIJOUEUR",
        "SURVIVALL",
        "BONUS"
    };

    private MenuLayout layout;
    private Pane uiLayer;
    private int selectedIndex = 0;
    private final List<Text> itemTexts = new ArrayList<>();
    private Rectangle highlightBar;
    private StackPane root;

    public StackPane build() {
        layout = new MenuLayout();
        double w = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        layout.update(w, h);

        root = HorrorBackground.buildLayers(w, h, "main");
        root.widthProperty().addListener((o, a, n) -> layout.update(n.doubleValue(), root.getHeight()));
        root.heightProperty().addListener((o, a, n) -> layout.update(root.getWidth(), n.doubleValue()));

        uiLayer = new Pane();
        uiLayer.setPickOnBounds(false);
        root.getChildren().add(uiLayer);

        // Titre : THE \n THE MINOTAUR
        Text title1 = new Text("THE");
        Text title2 = new Text("THE MINOTAUR");
        MenuFonts.applyTitle(title1, 48 * layout.scale());
        MenuFonts.applyTitle(title2, 56 * layout.scale());
        title1.setFill(Color.web("#8B0000"));
        title2.setFill(Color.web("#8B0000"));
        title1.setLayoutX(layout.x(TITLE_X));
        title1.setLayoutY(layout.y(TITLE_Y));
        title2.setLayoutX(layout.x(TITLE_X));
        title2.setLayoutY(layout.y(TITLE_Y) + 50 * layout.scale());
        uiLayer.getChildren().addAll(title1, title2);

        // Barre de surbrillance (derrière le texte sélectionné)
        highlightBar = new Rectangle(layout.width(HIGHLIGHT_W), layout.height(HIGHLIGHT_H));
        highlightBar.setFill(Color.color(0.4, 0, 0, 0.4));
        highlightBar.setVisible(true);
        uiLayer.getChildren().add(0, highlightBar);

        // Items
        for (int i = 0; i < ITEMS.length; i++) {
            final int idx = i;
            Text t = new Text(ITEMS[i]);
            t.setFont(MenuFonts.getSerif(22 * layout.scale()));
            t.setFill(Color.LIGHTGRAY);
            double y = layout.y(ITEMS_Y + i * ITEM_SPACING);
            t.setLayoutX(layout.x(ITEMS_X));
            t.setLayoutY(y);
            t.setUnderline(false);
            itemTexts.add(t);
            uiLayer.getChildren().add(t);

            Node hit = makeHitArea(idx, t);
            uiLayer.getChildren().add(hit);
            hit.setOnMouseEntered(e -> { selectedIndex = idx; syncSelection(); });
            hit.setOnMouseClicked(e -> { if (e.getButton() == MouseButton.PRIMARY) activate(); });
        }

        syncSelection();
        setupKeyboard(root);

        return root;
    }

    private Node makeHitArea(int idx, Text t) {
        Pane area = new Pane();
        area.setLayoutX(layout.x(ITEMS_X));
        area.setLayoutY(t.getLayoutY() - 25 * layout.scale());
        area.setMinSize(layout.width(520), layout.height(55));
        area.setPrefSize(layout.width(520), layout.height(55));
        area.setOnMouseEntered(e -> { selectedIndex = idx; syncSelection(); });
        area.setOnMouseExited(e -> {});
        return area;
    }

    private void syncSelection() {
        for (int i = 0; i < itemTexts.size(); i++) {
            itemTexts.get(i).setUnderline(i == selectedIndex);
            itemTexts.get(i).setFill(i == selectedIndex ? Color.web("#CC3333") : Color.LIGHTGRAY);
        }
        double yRef = ITEMS_Y + selectedIndex * ITEM_SPACING;
        highlightBar.setLayoutX(layout.x(ITEMS_X) - 5);
        highlightBar.setLayoutY(layout.y(yRef) - layout.height(HIGHLIGHT_H) * 0.6);
        highlightBar.setWidth(layout.width(HIGHLIGHT_W));
        highlightBar.setHeight(layout.height(HIGHLIGHT_H));
    }

    private void setupKeyboard(StackPane root) {
        root.setFocusTraversable(true);
        root.requestFocus();
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                selectedIndex = (selectedIndex - 1 + ITEMS.length) % ITEMS.length;
                syncSelection();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                selectedIndex = (selectedIndex + 1) % ITEMS.length;
                syncSelection();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                activate();
                e.consume();
            }
        });
    }

    private void activate() {
        switch (selectedIndex) {
            case 0: SceneRouter.fadeOutThen(SceneRouter::showAvatarMenu, root); break;
            case 1: SceneRouter.fadeOutThen(() -> SceneRouter.showModeConfig(AppState.Mode.CLASSIQUE), root); break;
            case 2: SceneRouter.fadeOutThen(() -> SceneRouter.showModeConfig(AppState.Mode.MULTIJOUEUR), root); break;
            case 3: SceneRouter.fadeOutThen(() -> SceneRouter.showModeConfig(AppState.Mode.SURVIVAL), root); break;
            case 4: SceneRouter.fadeOutThen(SceneRouter::showBonusConfig, root); break;
            default: break;
        }
    }
}
