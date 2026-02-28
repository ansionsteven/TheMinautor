# The Minautor (v2)

Projet JavaFX Maven – Labyrinthe / Minotaure.

## Prérequis

- JDK 25
- Maven 3.x (ou wrapper `mvnw`)
- OpenJFX 21 (déclaré dans le POM, pas de module Java)

## Compilation et exécution

```bash
mvn clean compile
mvn javafx:run
```

Fenêtre : taille minimale 1180×720, puis maximisée. Pas de plein écran.

## Structure livrée

- Code source : `src/main/java/com/steven/theminautaur/`
- Ressources : `src/main/resources/assets/` (fonts, ui, avatars)
- Voir **ressources.md** pour la liste des images à fournir (optionnelles, fallbacks si absentes).
