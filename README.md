# JavaTP3 - Spotify Clone

Application de lecteur musical en JavaFX, construite avec Maven.

## Fonctionnalites

- Chargement automatique des fichiers audio `mp3` et `wav`
- Affichage de la bibliotheque musicale locale
- Lecture, pause, piste suivante et precedente
- Modes shuffle et repetition
- Barre de progression et controle du volume
- Styles visuels via CSS

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

## Structure

- `spotify-clone/pom.xml` : configuration Maven et JavaFX
- `spotify-clone/src/main/java` : code source Java
- `spotify-clone/src/main/resources/music` : fichiers audio detectes au lancement
- `spotify-clone/src/main/resources/styles/app.css` : styles de l'interface

## Notes

- L'application lit les morceaux presents dans `spotify-clone/src/main/resources/music`.
- Les metadonnees audio sont simplifiees pour le moment : le titre est deduit du nom du fichier.
- Si Maven ou Java ne sont pas reconnus, verifie qu'ils sont bien disponibles dans le `PATH`.
