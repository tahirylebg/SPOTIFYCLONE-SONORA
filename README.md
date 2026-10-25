# Sonora

Lecteur de musique façon Spotify, découpé en un **backend** (API REST) et des **clients** qui l'utilisent.

```
backend/   API REST Spring Boot : bibliothèque, métadonnées, streaming audio, pochettes, historique
web/       Site React + Vite + TypeScript (bureau et mobile)
desktop/   Appli de bureau JavaFX d'origine (autonome, lit directement les fichiers)
music/     Fichiers .mp3 / .wav, partagés par le backend et l'appli desktop
```

L'appli mobile (React Native / Expo) viendra dans `mobile/` et réutilisera l'API et le client
TypeScript de `web/src/api/`.

## Prérequis

- Java 17 ou plus récent et Maven
- Node.js 20 ou plus récent (pour le site)

## Lancer le projet

Dans deux terminaux, depuis la racine du dépôt :

```powershell
.\run.ps1 backend   # API sur http://localhost:8080
.\run.ps1 web       # site sur http://localhost:5173
```

En dev, le site relaie `/api` vers le backend (voir `web/vite.config.ts`) : rien à configurer.
L'appli JavaFX se lance toujours avec `.\run.ps1 desktop`.

> **Erreur Maven `PKIX path building failed` ?** Un antivirus ou un proxy intercepte le HTTPS.
> Indique à Java d'utiliser les certificats de Windows avant de lancer Maven :
> `$env:MAVEN_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"`

## API

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/api/tracks` | Tous les morceaux |
| GET | `/api/tracks/{id}` | Un morceau |
| GET | `/api/tracks/{id}/audio` | Fichier audio (supporte `Range`, pour avancer dans le morceau) |
| GET | `/api/tracks/{id}/cover` | Pochette JPEG carrée, bandes noires retirées |
| GET | `/api/artists` | Artistes, avec leurs morceaux |
| GET | `/api/search?q=` | Recherche titres + artistes, insensible aux accents |
| GET | `/api/plays` | Historique d'écoute, du plus récent au plus ancien |
| POST | `/api/plays/{trackId}` | Enregistre une écoute |
| DELETE | `/api/plays` | Vide l'historique |
| POST | `/api/library/rescan` | Relit le dossier `music/` |

Réglages dans `backend/src/main/resources/application.properties` :
`sonora.music-dir` (dossier musique), `sonora.data-dir` (historique, `~/.sonora` par défaut)
et `sonora.cors-origins` (origines autorisées à appeler l'API depuis un navigateur).

Tests : `mvn -f backend/pom.xml test`

## Le site

- **Accueil** : accès rapide, écoutés récemment, tes artistes, tes plus écoutés
- **Rechercher** : grand champ de recherche, tuiles pour parcourir les artistes, meilleur résultat
- **Bibliothèque** : vues Titres (avec tri) et Artistes
- **Découvertes** : mix aléatoire, titres jamais écoutés, sélection au hasard
- Lecteur : aléatoire, répétition (tout / un seul), barre de progression, volume, commandes système
  (touches multimédia, écran de verrouillage)
- Raccourcis : `Espace` lecture/pause, `Ctrl + ←/→` piste précédente/suivante, `Ctrl + K` ou `/` recherche
- Sous 760 px de large, la sidebar laisse la place à une barre de navigation en bas

Pour viser un backend déployé ailleurs : `VITE_API_URL=https://mon-api.exemple.com npm run build`.

## Nommer ses fichiers

Nomme tes fichiers `Artiste - Titre.mp3`. Les mentions du type `(Visualizer)`, `CLIP OFFICIEL`
ou une année sont retirées du titre. Si le nom ne contient pas ` - `, ce sont les tags ID3 qui
sont utilisés. Après avoir ajouté des fichiers, relance le backend ou appelle `POST /api/library/rescan`.
