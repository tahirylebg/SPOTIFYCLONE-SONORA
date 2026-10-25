param(
    # backend : l'API Spring Boot | web : le site React | desktop : l'appli JavaFX
    [ValidateSet("backend", "web", "desktop")]
    [string]$App = "desktop"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

switch ($App) {
    "backend" { & mvn -f "backend/pom.xml" spring-boot:run }
    "web" {
        if (-not (Test-Path "web/node_modules")) { & npm --prefix web install }
        & npm --prefix web run dev
    }
    "desktop" { & mvn -f "desktop/pom.xml" javafx:run }
}
