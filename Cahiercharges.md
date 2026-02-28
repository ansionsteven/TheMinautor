TU ES UNE IA GENERATRICE DE CODE.
Tu dois produire un projet JavaFX COMPLET, structuré, compilable immédiatement dans NetBeans avec JDK 25 sous Windows.
Ce cahier des charges est UNIQUE et fait foi. Il remplace toute version précédente.
En cas de conflit, les règles “CAHIER DES CHARGES – MENUS” priment.

========================================
CONTRAINTES BUILD / EXECUTION
========================================
- JavaFX n’est PAS inclus dans JDK 25.
- Projet OBLIGATOIREMENT Maven avec OpenJFX configuré.
- Utiliser OpenJFX version stable LTS 21.x.
- DOIT compiler sans erreur.
- Exécution uniquement via : mvn javafx:run
- Interdiction setFullScreen(true).
- Utiliser stage.setMaximized(true).
- Taille minimale fenêtre : 1180x720 (stage.setMinWidth/MinHeight), puis maximisée.
- Sans Java Modules (PAS de module-info.java).
- Plugin javafx-maven-plugin configuré avec :
  mainClass = com.steven.theminautaur.MainApp
- Plateforme : Windows uniquement.

========================================
IDENTITE PROJET
========================================
GroupId      : com.steven
ArtifactId   : the-minautaur
Package root : com.steven.theminautaur
Sous-packages autorisés mais cohérents.

========================================
LIVRAISON OBLIGATOIRE
========================================
- Code en MULTIPLES BLOCS
- Chaque bloc = UN FICHIER COMPLET
- Chaque bloc commence par le chemin exact du fichier
- Fournir pom.xml
- Fournir README.md minimal
- Ne rien omettre (projet complet directement compilable).

========================================
RESOURCES
========================================
Chemin resources :
src/main/resources/assets/
 ├─ fonts/horror.ttf
 ├─ ui/menu_fade.png
 ├─ avatars/a1.png … a8.png
 (Optionnel recommandé pour respecter le visuel menus : ui/bg_main.png, ui/bg_classique.png, ui/bg_multi.png, ui/bg_survival.png, ui/bg_bonus.png)

Règles assets :
- Conserver proportions images (jamais d’étirement).
- Si un asset manque -> fallback graphique simple (AUCUNE exception, aucun crash, aucune stacktrace).
- horror.ttf utilisé UNIQUEMENT pour les TITRES de menus (jamais ailleurs).
- Avatars affichés ronds (clip cercle ou rendu circulaire).
- Si avatar PNG absent -> fallback :
  - cercle coloré (couleur unique déterministe par index)
  - + cercle noir central (silhouette simple = disque noir).

========================================
PERSISTENCE (Preferences)
========================================
Utiliser java.util.prefs.Preferences.
Stocker UNIQUEMENT :
- avatar sélectionné (entier 1..8)
Rien d’autre.

========================================
ARCHITECTURE OBLIGATOIRE (CLASSES)
========================================
MainApp
SceneRouter
AppState

MainMenuView
AvatarMenuView
ModeConfigView
BonusConfigView
HorrorBackground

GameView
HudBar
GameLoop
InputManager

Maze
MazeGenerator
DoorManager
PortalManager
ChestManager

Entity
Runner
Minotaur
Avatar

RunnerAI
MinotaurAI
Pathing
LineOfSight
TeamComms

ScreenOverlay
FadeText

NOTE : respecter ces noms de classes EXACTS et un package cohérent com.steven.theminautaur (sous-packages permis).

========================================
STYLE HORREUR (GLOBAL MENUS)
========================================
- Fond noir + ambiance horreur.
- Pas de Button JavaFX visible, pas de Dialog standard dans les menus.
- Navigation souris + clavier obligatoire.
- Hover = soulignement TEXTE uniquement (pas de rectangle “bouton”).
- Le titre des menus utilise horror.ttf UNIQUEMENT.
- AUCUNE interprétation graphique libre : respecter STRICTEMENT les visuels menus fournis.
- Aucun crash si assets manquants.

========================================
CAHIER DES CHARGES – MENUS (PRIORITAIRE)
THE MINAUTOR
========================================

IMPORTANT :
Tous les menus doivent respecter STRICTEMENT le visuel fourni.
Aucune interprétation graphique libre n’est autorisée.
Aucun Button JavaFX visible.
Navigation 100% clavier obligatoire.
Aucun crash si un asset est manquant.

========================================
SYSTEME DE COORDONNEES (MENUS)
========================================
Résolution de référence :
Wref = 2048
Href = 1365

Pour toute fenêtre réelle (W,H) :

s  = min(W / Wref, H / Href)
ox = (W - Wref * s) / 2
oy = (H - Href * s) / 2

Toute coordonnée (x,y,w,h) donnée dans ce document est exprimée
en pixels de référence puis convertie :

X = ox + x * s
Y = oy + y * s
Width  = w * s
Height = h * s

Interdiction de positionnement approximatif.

========================================
STRUCTURE COMMUNE A TOUS LES MENUS
========================================
Root = StackPane

Ordre des couches (bas -> haut) :
1) BackgroundImageLayer
2) VignetteLayer
3) FogLayer
4) UIContentLayer
5) InputHintLayer (optionnel)

========================================
BACKGROUND (MENUS)
========================================
Tenter de charger :
assets/ui/bg_main.png
assets/ui/bg_classique.png
assets/ui/bg_multi.png
assets/ui/bg_survival.png
assets/ui/bg_bonus.png

Si absent :
fallback = assets/ui/menu_fade.png

Image affichée en mode cover.
Aucune déformation des proportions.

========================================
VIGNETTE + PULSE ROUGE (MENUS)
========================================
Vignette noire :
- Centre alpha 0
- Bords alpha ~0.55

Pulse rouge :
- Intervalle aléatoire 450–850 ms
- Alpha aléatoire 0.05–0.25
- Effet uniquement sur les bords
- Jamais plein écran rouge

========================================
FOG LAYER (MENUS)
========================================
- Texture bruit/grain procédurale
- Opacité 0.08 à 0.18
- Translation lente ±20 px ref sur 8–14 s
- Variation légère d’alpha

========================================
TYPOGRAPHIE (MENUS)
========================================
Police horror.ttf :
UNIQUEMENT pour les titres.

Reste :
Serif ou Times New Roman.

Couleurs :
- Titres = rouge/orange sang
- Texte secondaire = gris clair
- Plaques = pierre sombre + éclaboussures rouges
- Sélection = glow rouge discret

========================================
COMPOSANT PLAQUE (STONE PLATE) (MENUS)
========================================
Géométrie :
- Rectangle
- Coins arrondis 6–10 px ref
- Double bordure (extérieur sombre, intérieur clair)
- Ombre portée blur 18 px ref

Texture :
- Dégradé sombre
- Bruit grain
- Eclaboussures rouges
- Rayures fines

Etats :
Idle :
- normal
- texte rouge sombre

Hover :
- texte plus lumineux
- glow rouge léger

Selected (clavier) :
- glow visible
- scale 1.02–1.04

Pressed :
- scale 0.98
- glow réduit

Interdiction Button JavaFX.

========================================
MENU PRINCIPAL – MainMenuView
========================================
TITRE :
Position approx :
x ≈ 120
y ≈ 70

Texte EXACT :
THE
THE MINOTAUR

Police horror.ttf

LISTE DES ITEMS :
Zone approx :
x ≈ 150
y ≈ 420

Ordre EXACT :
AVATAR
CLASSIQUE
MULTIJOUEUR
SURVIVALL
BONUS

Respecter EXACTEMENT :
SURVIVALL (2 L)

Espacement vertical ≈ 92 px ref.

SELECTION :
Si sélection clavier :
1) Flèche à gauche
2) Barre de surbrillance derrière le texte
   largeur ≈ 520 px ref
   hauteur ≈ 55 px ref

Hover souris :
- underline texte uniquement

La sélection clavier suit automatiquement le hover.

NAVIGATION :
↑ ↓  -> change sélection (boucle)
Enter -> valide
Souris hover -> underline
Souris clic -> valide

ACTIONS :
AVATAR       -> goAvatarMenu()
CLASSIQUE    -> goModeConfig(CLASSIQUE)
MULTIJOUEUR  -> goModeConfig(MULTIJOUEUR)
SURVIVALL    -> goModeConfig(SURVIVAL)
BONUS        -> goBonusConfig()

========================================
MENU AVATAR – AvatarMenuView
========================================
- Afficher 8 avatars (a1.png … a8.png) en grille.
- Navigation clavier :
  - ← → et ↑ ↓ déplacent la sélection dans la grille.
  - La sélection boucle si on dépasse les bords (wrap) pour éviter blocage.
- Souris :
  - hover = underline du libellé (si libellé présent) ou highlight discret (sans Button)
  - clic = sélection courante
- Avatar sélectionné :
  - highlight clair (outline + glow discret) et/ou soulignement.
- Enter :
  - applique immédiatement la sélection et sauvegarde Preferences avatarIndex (1..8).
  - affiche un feedback discret (FadeText 1.2s) sans popup.
- Esc / Backspace :
  - retour menu principal.
- Si PNG absent -> fallback :
  - cercle coloré déterministe par index
  - + disque noir central.

========================================
MODE CONFIG – ModeConfigView (CLASSIQUE / MULTIJOUEUR / SURVIVAL)
========================================
Ce menu remplace l’ancien “slider difficulté” : la difficulté est contrôlée par une barre de crânes conforme au visuel.

TITRE :
Position centre, y ≈ 120
Texte EXACT :
CLASSIQUE
MULTIJOUEUR
SURVIVAL
Police horror.ttf

BARRE DIFFICULTE (CRANES) :
- 6 crânes horizontaux.
- Difficulté = entier 1..5 (jamais de valeur décimale).
- 1..difficulty lumineux, >difficulty ternes.
- crâne actif = effet drip rouge.

Navigation difficulté :
← diminue
→ augmente
Bornes strictes 1 à 5.

TEXTE DIFFICULTE :
Format EXACT :
Niveau X : <phrase> !

Phrases EXACTES :
1 : Tu as peur et tu es faible
2 : tu commences à te sentir chaud ?
3 : On commence seulement à s’amuser
4 : tu vas transpirer… le minautor aussi
5 : tu viens de signer ton arrêt de mort

CHOIX ROLE (plaques centrales) :
Textes EXACTS :
JOUEUR
MINOTOR
Respecter EXACTEMENT :
MINOTOR (sans A)

Disposition :
CLASSIQUE : gauche = JOUEUR, droite = MINOTOR
MULTIJOUEUR : gauche = JOUEUR, droite = MINOTOR
SURVIVAL : gauche = MINOTOR, droite = JOUEUR

Navigation rôle :
← → change rôle
Enter sélectionne (sans lancer le jeu)
↑ ↓ change zone (vers actions bas)

ACTIONS BAS (plaques) :
CLASSIQUE :
ENTRER DANS LE LABYRINTHE
RETOUR

MULTIJOUEUR :
ENTREZ DANS LE LABYRINTHE
— RETOUR

SURVIVAL :
ENTREZ DANS LE LABYRINTHE
— RETOUR

Esc / Backspace = retour menu principal.
Enter sur “ENTRER/ENTREZ …” = lance GameView.

AppState mis à jour au lancement :
- mode (CLASSIQUE/MULTIJOUEUR/SURVIVAL)
- difficulté (1..5)
- rôle (RUNNER ou MINOTAUR)
- avatarIndex (depuis Preferences)

========================================
MENU BONUS – BonusConfigView
========================================
TITRE :
BONUS
Police horror.ttf

Pas de barre difficulté visuelle.
Pas de choix rôle.

ACTIONS (plaques) :
ENTREZ DANS LE LABYRINTHE
— RETOUR

LOGIQUE BONUS :
- difficulté = dernière difficulté utilisée durant cette exécution de l’app
  (stockée en mémoire dans AppState)
- si aucune difficulté n’a encore été choisie -> difficulté = 3
- rôle forcé = runner humain
- Enter sur “ENTREZ…” lance directement GameView en mode BONUS
- Esc/Backspace ou “— RETOUR” revient au menu principal

========================================
TRANSITIONS (MENUS)
========================================
Entrée menu :
fade-in 220 ms

Sortie menu :
fade-out 160 ms

Fog + vignette ne s’arrêtent pas brutalement.

========================================
CONTRAINTES STRICTES (MENUS)
========================================
- Aucun Button visible
- Aucun Dialog standard dans les menus
- Navigation 100% clavier
- La sélection clavier suit le hover souris (pas de double focus)
- Tous les textes strictement identiques (respecter SURVIVALL et MINOTOR)
- Aucun crash si image absente
- Mise à l’échelle proportionnelle obligatoire (s, ox, oy)

========================================
RESTE DU PROJET (JEU) – RAPPEL
========================================
Toutes les autres sections du cahier des charges (Input en jeu, Render, Maze, IA, Modes, HUD, Overlays, Popup in-game, Scene routing, Qualité code/commentaires) restent applicables et inchangées.
La popup confirmation (ESC) est UNIQUEMENT en jeu (pas dans les menus) et doit être custom (sans Dialog standard).