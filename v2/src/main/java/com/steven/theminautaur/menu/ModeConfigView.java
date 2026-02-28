package com.steven.theminautaur.menu;

import com.steven.theminautaur.AppState;
import com.steven.theminautaur.SceneRouter;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Config mode (CLASSIQUE / MULTIJOUEUR / SURVIVAL) : titre, barre 6 crânes (difficulté 1..5),
 * choix rôle JOUEUR / MINOTOR, actions ENTRER DANS LE LABYRINTHE / RETOUR.
 */
public final class ModeConfigView {

    private static final String[] DIFF_PHRASES = {
        "Tu as peur et tu es faible",
        "tu commences à te sentir chaud ?",
        "On commence seulement à s'amuser",
        "tu vas transpirer… le minautor aussi",
        "tu viens de signer ton arrêt de mort"
    };

    private final AppState.Mode mode;
    private MenuLayout layout;
    private int difficulty = AppState.getDifficulty();
    private int roleIndex = 0; // 0 = JOUEUR, 1 = MINOTOR (ou inverse pour SURVIVAL)
    private int focusZone = 0; // 0 = difficulty, 1 = role, 2 = actions
    private int actionIndex = 0; // 0 = ENTRER..., 1 = RETOUR
    private List<Circle> skulls = new ArrayList<>();
    private Text diffPhraseText;
    private Text roleLeftText;
    private Text roleRightText;
    private Text action1Text;
    private Text action2Text;
    private StackPane root;
    private Pane uiLayer;

    public ModeConfigView(AppState.Mode mode) {
        this.mode = mode;
    }

    public StackPane build() {
        layout = new MenuLayout();
        double w = 1180;
        double h = 720;
        try {
            w = SceneRouter.getStage().getWidth();
            h = SceneRouter.getStage().getHeight();
        } catch (Exception ignored) {}
        layout.update(w, h);

        String bgKey = mode == AppState.Mode.CLASSIQUE ? "classique" : (mode == AppState.Mode.MULTIJOUEUR ? "multi" : "survival");
        root = HorrorBackground.buildLayers(w, h, bgKey);
        root.widthProperty().addListener((o, a, n) -> layout.update(n.doubleValue(), root.getHeight()));
        root.heightProperty().addListener((o, a, n) -> layout.update(root.getWidth(), n.doubleValue()));

        uiLayer = new Pane();
        root.getChildren().add(uiLayer);

        String title = mode == AppState.Mode.CLASSIQUE ? "CLASSIQUE" : (mode == AppState.Mode.MULTIJOUEUR ? "MULTIJOUEUR" : "SURVIVAL");
        Text titleText = new Text(title);
        MenuFonts.applyTitle(titleText, 52 * layout.scale());
        titleText.setFill(Color.web("#8B0000"));
        titleText.setLayoutX(layout.x(900));
        titleText.setLayoutY(layout.y(120));
        uiLayer.getChildren().add(titleText);

        // Barre 6 crânes
        double skullY = layout.y(280);
        double skullSpacing = 60 * layout.scale();
        double skullStartX = layout.x(700);
        skulls = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Circle skull = new Circle(skullStartX + i * skullSpacing, skullY, 20 * layout.scale());
            skull.setFill(i < difficulty ? Color.web("#CC3333") : Color.gray(0.4));
            skulls.add(skull);
            uiLayer.getChildren().add(skull);
        }

        diffPhraseText = new Text();
        diffPhraseText.setFont(MenuFonts.getSerif(16 * layout.scale()));
        diffPhraseText.setFill(Color.LIGHTGRAY);
        diffPhraseText.setLayoutX(layout.x(500));
        diffPhraseText.setLayoutY(layout.y(340));
        uiLayer.getChildren().add(diffPhraseText);
        updateDiffPhrase();

        // Rôle : CLASSIQUE/MULTI gauche=JOUEUR droite=MINOTOR ; SURVIVAL gauche=MINOTOR droite=JOUEUR
        boolean survivalOrder = (mode == AppState.Mode.SURVIVAL);
        roleLeftText = new Text(survivalOrder ? "MINOTOR" : "JOUEUR");
        roleRightText = new Text(survivalOrder ? "JOUEUR" : "MINOTOR");
        roleLeftText.setFont(MenuFonts.getSerif(20 * layout.scale()));
        roleRightText.setFont(MenuFonts.getSerif(20 * layout.scale()));
        roleLeftText.setFill(Color.LIGHTGRAY);
        roleRightText.setFill(Color.LIGHTGRAY);
        roleLeftText.setLayoutX(layout.x(550));
        roleLeftText.setLayoutY(layout.y(420));
        roleRightText.setLayoutX(layout.x(950));
        roleRightText.setLayoutY(layout.y(420));
        uiLayer.getChildren().addAll(roleLeftText, roleRightText);

        String enterText = (mode == AppState.Mode.CLASSIQUE) ? "ENTRER DANS LE LABYRINTHE" : "ENTREZ DANS LE LABYRINTHE";
        String backText = (mode == AppState.Mode.CLASSIQUE) ? "RETOUR" : "— RETOUR";
        action1Text = new Text(enterText);
        action2Text = new Text(backText);
        action1Text.setFont(MenuFonts.getSerif(18 * layout.scale()));
        action2Text.setFont(MenuFonts.getSerif(18 * layout.scale()));
        action1Text.setLayoutX(layout.x(600));
        action1Text.setLayoutY(layout.y(520));
        action2Text.setLayoutX(layout.x(600));
        action2Text.setLayoutY(layout.y(600));
        uiLayer.getChildren().addAll(action1Text, action2Text);

        syncRoleAndActions();

        setupKeyboard(root);
        return root;
    }

    private void updateDiffPhrase() {
        int d = Math.max(1, Math.min(5, difficulty));
        diffPhraseText.setText("Niveau " + d + " : " + DIFF_PHRASES[d - 1] + " !");
        for (int i = 0; i < skulls.size(); i++) {
            skulls.get(i).setFill(i < d ? Color.web("#CC3333") : Color.gray(0.4));
        }
    }

    private void syncRoleAndActions() {
        boolean survivalOrder = (mode == AppState.Mode.SURVIVAL);
        roleLeftText.setFill(focusZone == 1 && roleIndex == 0 ? Color.web("#CC3333") : Color.LIGHTGRAY);
        roleRightText.setFill(focusZone == 1 && roleIndex == 1 ? Color.web("#CC3333") : Color.LIGHTGRAY);
        action1Text.setFill(focusZone == 2 && actionIndex == 0 ? Color.web("#CC3333") : Color.LIGHTGRAY);
        action2Text.setFill(focusZone == 2 && actionIndex == 1 ? Color.web("#CC3333") : Color.LIGHTGRAY);
    }

    private void setupKeyboard(StackPane root) {
        root.setFocusTraversable(true);
        root.requestFocus();
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.BACK_SPACE) {
                SceneRouter.fadeOutThen(SceneRouter::showMainMenu, this.root);
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.LEFT) {
                if (focusZone == 0) { difficulty = Math.max(1, difficulty - 1); updateDiffPhrase(); }
                else if (focusZone == 1) { roleIndex = 0; syncRoleAndActions(); }
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.RIGHT) {
                if (focusZone == 0) { difficulty = Math.min(5, difficulty + 1); updateDiffPhrase(); }
                else if (focusZone == 1) { roleIndex = 1; syncRoleAndActions(); }
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.UP) {
                focusZone = Math.max(0, focusZone - 1);
                syncRoleAndActions();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.DOWN) {
                focusZone = Math.min(2, focusZone + 1);
                syncRoleAndActions();
                e.consume();
                return;
            }
            if (e.getCode() == KeyCode.ENTER) {
                if (focusZone == 1) { /* sélection rôle */ syncRoleAndActions(); }
                else if (focusZone == 2 && actionIndex == 0) {
                    AppState.setMode(mode);
                    AppState.setDifficulty(difficulty);
                    AppState.setRole(roleIndex == 0 ? (mode == AppState.Mode.SURVIVAL ? AppState.Role.MINOTAUR : AppState.Role.RUNNER)
                        : (mode == AppState.Mode.SURVIVAL ? AppState.Role.RUNNER : AppState.Role.MINOTAUR));
                    SceneRouter.fadeOutThen(SceneRouter::showGame, this.root);
                } else if (focusZone == 2 && actionIndex == 1) {
                    SceneRouter.fadeOutThen(SceneRouter::showMainMenu, this.root);
                }
                e.consume();
            }
        });
    }
}
