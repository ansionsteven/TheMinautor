package com.steven.theminautaur.menu;

import com.steven.theminautaur.AppState;
import com.steven.theminautaur.SceneRouter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Menu BONUS : titre, pas de barre difficulté, pas de choix rôle. Actions : ENTRER / RETOUR.
 * Difficulté = dernière utilisée (AppState), défaut 3. Rôle = runner.
 */
public final class BonusConfigView {

    private MenuLayout layout;
    private int actionIndex = 0;
    private Text action1Text;
    private Text action2Text;
    private StackPane root;

    public StackPane build() {
        layout = new MenuLayout();
        double w = 1180;
        double h = 720;
        try {
            w = SceneRouter.getStage().getWidth();
            h = SceneRouter.getStage().getHeight();
        } catch (Exception ignored) {}
        layout.update(w, h);

        root = HorrorBackground.buildLayers(w, h, "bonus");
        root.widthProperty().addListener((o, a, n) -> layout.update(n.doubleValue(), root.getHeight()));
        root.heightProperty().addListener((o, a, n) -> layout.update(root.getWidth(), n.doubleValue()));

        Pane uiLayer = new Pane();
        root.getChildren().add(uiLayer);

        Text titleText = new Text("BONUS");
        MenuFonts.applyTitle(titleText, 52 * layout.scale());
        titleText.setFill(Color.web("#8B0000"));
        titleText.setLayoutX(layout.x(900));
        titleText.setLayoutY(layout.y(120));
        uiLayer.getChildren().add(titleText);

        action1Text = new Text("ENTREZ DANS LE LABYRINTHE");
        action2Text = new Text("— RETOUR");
        action1Text.setFont(MenuFonts.getSerif(18 * layout.scale()));
        action2Text.setFont(MenuFonts.getSerif(18 * layout.scale()));
        action1Text.setLayoutX(layout.x(600));
        action1Text.setLayoutY(layout.y(400));
        action2Text.setLayoutX(layout.x(600));
        action2Text.setLayoutY(layout.y(480));
        uiLayer.getChildren().addAll(action1Text, action2Text);

        syncActions();

        root.setFocusTraversable(true);
        root.requestFocus();
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.BACK_SPACE) {
                SceneRouter.fadeOutThen(SceneRouter::showMainMenu, root);
                e.consume();
            } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                actionIndex = 1 - actionIndex;
                syncActions();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                if (actionIndex == 0) {
                    AppState.setMode(AppState.Mode.BONUS);
                    if (AppState.getDifficulty() < 1 || AppState.getDifficulty() > 5) AppState.setDifficulty(3);
                    AppState.setRole(AppState.Role.RUNNER);
                    SceneRouter.fadeOutThen(SceneRouter::showGame, root);
                } else {
                    SceneRouter.fadeOutThen(SceneRouter::showMainMenu, root);
                }
                e.consume();
            }
        });

        return root;
    }

    private void syncActions() {
        action1Text.setFill(actionIndex == 0 ? Color.web("#CC3333") : Color.LIGHTGRAY);
        action2Text.setFill(actionIndex == 1 ? Color.web("#CC3333") : Color.LIGHTGRAY);
    }
}
