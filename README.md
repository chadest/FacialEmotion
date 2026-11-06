
Application Android moderne construite avec **Jetpack Compose**, **CameraX** et **Google MediaPipe Face Landmarker**, capable d'appliquer un masque facial géométrique 3D en réalité augmentée, de classifier les émotions faciales en temps réel via 52 blendshapes et de diffuser des réactions interactives flottantes à l'écran.

---

##  Fonctionnalités Clés

- **Maillage Facial 3D Haute Densité (478 Repères)** :
  - Détection faciale ultra-rapide et continue exécutée à 60 FPS sur l'appareil.
  - Rendu en réalité augmentée via `Canvas` avec triangulation géométrique cyberpunk (contours du visage, yeux, arcade, nez, lèvres et triangulation polygonale du front et des joues).
  - Adaptation automatique aux dimensions de l'écran, au ratio d'aspect et miroir automatique sur caméra frontale.

- **Moteur d'Émotions par 52 Blendshapes (Standard ARKit)** :
  - Analyse des micro-expressions du visage (`mouthSmileLeft/Right`, `jawOpen`, `eyeBlinkLeft/Right`, `browDown`, `eyeWide`, etc.).
  - Détection en temps réel des émotions : **Joie**, **Surprise**, **Complice (Clin d'œil)**, **Sérénité**, **Concentré**, et **Neutre**.
  - Badge tête haute (HUD) avec icône vectorielle et jauge de confiance en pourcentage.

- **Réactions Flottantes Instantanées** :
  - Bouton central ergonomique (48dp+) et sélecteur d'émotions rapide.
  - Diffusion ascendante de bulles d'émotions animées avec trajectoires oscillantes, fondu progressif et retour haptique.

- **Contrôles & Personnalisation** :
  - Bascule instantanée entre la caméra avant et arrière.
  - Activation / désactivation du masque filaire.
  - Sélecteur de teintes géométriques néon (Cyan électrique, Menthe néon, Ambre doré, Blanc pur).

---

##  Stack Technique

- **Langage** : [Kotlin](https://kotlinlang.org/) (Coroutines, Flow, StateFlow)
- **UI Toolkit** : [Jetpack Compose](https://developer.android.com/jetpack/compose) avec Material Design 3 (M3)
- **Vision & AR** : [Google MediaPipe Tasks Vision](https://developers.google.com/mediapipe/solutions/vision/face_landmarker) (`face_landmarker.task` embarqué localement)
- **Caméra** : [AndroidX CameraX](https://developer.android.com/training/camerax) (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Architecture** : MVVM / Clean Architecture réactive
- **Tests** : JUnit 4 & Robolectric

---

## Structure du Projet

```text
app/src/main/
├── assets/
│   └── face_landmarker.task          # Modèle IA MediaPipe pour les repères 3D et blendshapes
├── java/com/example/
│   ├── MainActivity.kt               # Point d'entrée avec gestion Edge-to-Edge
│   ├── ml/
│   │   └── FaceAnalyzer.kt           # Pipeline d'analyse CameraX -> MediaPipe Live Stream
│   ├── model/
│   │   └── FaceTrackData.kt          # Modèles de données (478 points 3D, émotions, réactions)
│   └── ui/
│       ├── CameraScreen.kt           # Écran principal orchestrant le flux vidéo et l'interface
│       ├── components/
│       │   ├── CameraPreviewView.kt  # Intégration PreviewView CameraX dans Compose
│       │   ├── GeometricMeshCanvas.kt# Rendu Canvas du maillage 3D AR
│       │   ├── EmotionHudOverlay.kt  # Badge de confiance, contrôles et bouton d'action
│       │   └── FloatingReactionsOverlay.kt # Système de particules d'émotions flottantes
│       └── theme/
│           ├── Color.kt              # Palette de couleurs AR sombre cyberpunk
│           └── Theme.kt              # Configuration du thème Material Design 3
└── res/
    ├── drawable/                     # Icônes vectorielles et assets
    └── mipmap-*/                     # Icônes adaptatives de l'application
```

---

## Installation & Compilation

### Prérequis

- **Android Studio** : Ladybug / Jellyfish ou supérieur
- **JDK** : Version 17 ou supérieure
- **Android SDK** : Compile SDK 36 (Min SDK 24 - Android 7.0+)

### Cloner et compiler

```bash
# 1. Cloner le dépôt
git clone https://github.com/votre-utilisateur/facepulse-ar.git
cd facepulse-ar

# 2. Lancer la compilation via Gradle
gradle assembleDebug

# 3. Exécuter les tests unitaires et Robolectric
gradle :app:testDebugUnitTest
```

---

## Permissions Requises

L'application déclare les permissions suivantes dans `AndroidManifest.xml` :

- `android.permission.CAMERA` : Indispensable pour capturer le flux vidéo et analyser le visage.
- `android.permission.VIBRATE` : Utilisé pour le retour haptique lors de la diffusion des émotions.

> **Confidentialité & Sécurité** : Tout le traitement de vision et la détection d'émotions s'exécutent **100% en local** sur l'appareil via MediaPipe. Aucune image, vidéo ou donnée biométrique n'est transmise vers des serveurs distants.

---

## Licence

Ce projet est sous licence MIT. Consultez le fichier [LICENSE](LICENSE) pour plus d'informations.
