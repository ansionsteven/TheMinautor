# Ressources externes – The Minautor

Placez les fichiers listés ci-dessous dans `src/main/resources/assets/` pour respecter le visuel des menus et du jeu. Si un fichier est absent, l’application utilise un fallback graphique (aucun crash).

---

## Obligatoires (ou fallback utilisé)

| Chemin | Nom du fichier | Ressource demandée |
|--------|----------------|--------------------|
| `assets/fonts/` | `horror.ttf` | Police d’horreur pour les **titres** des menus uniquement (THE MINOTAUR, CLASSIQUE, AVATAR, etc.). Style sanglant / angoissant. |
| `assets/ui/` | `menu_fade.png` | Image de fond de fallback pour tous les menus si les fonds spécifiques sont absents. Fond sombre, effet fade / brume. |

---

## Optionnels (recommandés pour le visuel menus)

| Chemin | Nom du fichier | Ressource demandée |
|--------|----------------|--------------------|
| `assets/ui/` | `bg_main.png` | Fond du **menu principal** (écran titre, liste AVATAR / CLASSIQUE / MULTIJOUEUR / SURVIVALL / BONUS). Ambiance horreur, proportions libres (affichage cover). |
| `assets/ui/` | `bg_classique.png` | Fond du menu **Mode Classique** (configuration difficulté + rôle). |
| `assets/ui/` | `bg_multi.png` | Fond du menu **Multijoueur**. |
| `assets/ui/` | `bg_survival.png` | Fond du menu **Survival**. |
| `assets/ui/` | `bg_bonus.png` | Fond du menu **Bonus**. |

---

## Avatars (optionnels)

| Chemin | Nom du fichier | Ressource demandée |
|--------|----------------|--------------------|
| `assets/avatars/` | `a1.png` … `a8.png` | 8 avatars pour la sélection dans le menu Avatar. Affichés **ronds**. Si absent : cercle coloré + disque noir central (silhouette). |

---

## Optionnel – Barre de difficulté (Mode Config)

| Chemin | Nom du fichier | Ressource demandée |
|--------|----------------|--------------------|
| `assets/ui/` | `skull.png` | Icône de **crâne** pour la barre de difficulté (6 crânes horizontaux, 1–5 actifs). Style horreur, effet “drip” rouge possible. Si absent : rendu procédural simple (cercle/crâne basique). |

---

## Règles rappel

- **Proportions** : les images ne sont jamais étirées (mode cover / proportions conservées).
- **horror.ttf** : utilisée **uniquement** pour les titres de menus.
- **Avatars** : toujours affichés en cercle (clip ou rendu circulaire).
