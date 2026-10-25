# JavaTP3 - Spotify Clone

Application de lecteur musical en JavaFX, construite avec Maven.

## Fonctionnalites

- Chargement automatique des fichiers audio `mp3` et `wav`
- Vraies durees et pochettes lues dans les fichiers (tags ID3)
- Artiste et titre deduits du nom de fichier (`Artiste - Titre.mp3`)
- Recherche par titre ou artiste (insensible aux accents)
- Lecture, pause, piste suivante et precedente (au-dela de 3 s, "precedent" revient au debut)
- Modes shuffle et repetition (tout / un seul)
- Barre de progression cliquable, volume et mute
- Morceau en cours mis en evidence dans la liste

## Pre-requis

- Java 17
- Maven

## Lancer le projet

Depuis la racine du depot :

```powershell
.\run.ps1
```

Ou, si tu utilises l'invite de commandes Windows :

```cmd
run.cmd
```

Ces scripts lancent :

```powershell
mvn -f spotify-clone/pom.xml javafx:run
```

## Utilisation

- **Double-clic** ou **Entree** sur un titre pour le lancer
- `Espace` : lecture / pause
- `Ctrl + ->` / `Ctrl + <-` : piste suivante / precedente
- `Ctrl + F` : rechercher, `Echap` : vider la recherche

## Structure

- `spotify-clone/pom.xml` : configuration Maven et JavaFX
- `spotify-clone/src/main/java` : code source Java
- `spotify-clone/src/main/resources/music` : fichiers audio detectes au lancement
- `spotify-clone/src/main/resources/styles/app.css` : styles de l'interface

## Notes

- L'application lit les morceaux presents dans `spotify-clone/src/main/resources/music`.
- Nomme tes fichiers `Artiste - Titre.mp3`. Les mentions du type `(Visualizer)`, `CLIP OFFICIEL` ou une annee sont retirees du titre. Si le nom ne contient pas ` - `, ce sont les tags ID3 qui sont utilises.
- Si Maven ou Java ne sont pas reconnus, verifie qu'ils sont bien disponibles dans le `PATH`.
