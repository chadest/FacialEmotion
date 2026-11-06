# FacePulse AR - Masque Facial Géométrique & Émotions en Temps Réel

Application Android moderne en Jetpack Compose permettant d'activer la caméra (avant/arrière), de suivre le visage en temps réel avec ML Kit, d'appliquer un masque géométrique futuriste minimaliste avec contours et points de repère, de calculer les émotions faciales avec jauge de confiance, et d'envoyer instantanément des réactions interactives flottantes à l'écran.

## User Review & Critical Decisions

> [!IMPORTANT]
> Les réponses aux questions de cadrage ont été intégrées dans cette spécification :
> - **Style de masque** : Maillage facial géométrique minimaliste et contours de repères (polygones polygonaux légers, contours yeux/nez/bouche/mâchoire, accents néon haute précision).
> - **Action d'envoi d'émotion** : Diffusion d'une réaction flottante instantanée à l'écran avec particules animées lors de l'appui sur l'emoji en bas ou sur détection forte.
> - **Visualisation des émotions** : Badge d'émotion dynamique en direct avec jauge circulaire ou barre de confiance (%) et emoji expressif interactif.

---

### 1. Overview & Core Concept

- **Fonctionnalité principale** : Prévisualisation caméra fluide (CameraX), analyse continue d'images via Google ML Kit Face Detection pour extraire les repères faciaux (yeux, bouche, joues, nez, contour du visage) et probabilités d'expression (sourire, clignements, inclinaisons).
- **Masque Géométrique AR** : Tracé réactif sur `Canvas` au-dessus du flux vidéo calculant la mise à l'échelle et la mise en miroir automatique pour caméra avant, dessinant un maillage filaire cyberpunk/minimaliste connecté aux repères réels du visage.
- **Moteur d'Émotions** : Déduction de l'émotion dominante (Joie/Sourire, Surprise, Clin d'œil/Complice, Sérénité, Concentration) avec pourcentage de confiance en temps réel.
- **Réactions Flottantes** : Dock inférieur avec emoji dynamique principal reflétant l'émotion en cours et déclencheur d'envoi de réactions en cascade flottant vers le haut de l'écran avec feedback haptique.

---

### 2. User Experience & Visual Design

- **Aesthetic Direction** : Minimaliste technologique, sombre immersif ("OLED Dark AR") sans dégradés criards, typographie M3 nette et icônes vectorielles SVG.
- **Éléments d'interface utilisateur** :
  - **Zone Caméra Plein Écran** : Flux CameraX avec gestion automatique des permissions Android (`CAMERA`) et bascule caméra avant/arrière.
  - **Canvas AR Superposé** : Rendu des polygones géométriques et contours faciaux calculés à 30+ FPS avec lissage d'interpolations.
  - **En-tête HUD supérieur** : Bouton de bascule caméra (avant/arrière), toggle du masque on/off, et sélecteur de teinte du maillage (Cyan électrique, Blanc pur, Vert matrix).
  - **Badge d'émotion en direct** : Pilule translucide élégante affichant le libellé de l'émotion et la jauge de certitude (ex: "Heureux • 94%").
  - **Barre d'action inférieure** : Bouton central emoji réactif grand format (48dp+) permettant d'envoyer immédiatement une vague de réactions flottantes animées à l'écran, accompagné d'un sélecteur rapide d'émotions manuelles.

---

### 3. Key Product Decisions & Trade-Offs

- **ML Kit Face Detection On-Device** :
  - *Choix* : Détection 100% sur l'appareil via `com.google.mlkit:face-detection` (mode stream ultra-rapide avec classification de sourires et repères faciaux complets).
  - *Raison* : Confidentialité absolue (aucune image envoyée sur le cloud), latence quasi-nulle (<30ms par frame), zéro coût API.
- **CameraX + Compose Overlay** :
  - *Choix* : Utilisation de `androidx.camera.view.PreviewView` intégré dans Compose avec un `Canvas` synchronisé pour les calculs de coordonnées d'affichage (transformation miroir caméra frontale et ratio d'aspect).
  - *Raison* : Haute fluidité native Android, intégration propre avec le cycle de vie de l'Activity.
- **Gestion des permissions simplifiée** :
  - *Choix* : Écran d'accueil accueillant demandant la permission caméra avec explication claire et état de refus gracieux.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   MainActivity                         │
│  (Edge-to-Edge, Theme M3, Permissions Handler)         │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    CameraScreen                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ CameraPreview (CameraX PreviewView + Lifecycle)  │  │
│  └────────────────────────┬─────────────────────────┘  │
│                           │ Frames (ImageProxy)        │
│  ┌────────────────────────▼─────────────────────────┐  │
│  │ FaceAnalyzer (ML Kit FaceDetector)               │  │
│  │ - Extraits : Repères, Contours, Sourire, Yeux    │  │
│  └────────────────────────┬─────────────────────────┘  │
│                           │ FaceData StateFlow         │
│  ┌────────────────────────▼─────────────────────────┐  │
│  │ FaceMeshCanvas (Tracé géométrique AR minimaliste)│  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ FloatingReactionsOverlay (Animation des emojis)  │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ EmotionHudBar (Badge de confiance + Emoji sender)│  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

- **Dépendances à activer / ajouter** :
  - Décommenter `androidx-camera-*` (`camera-camera2`, `camera-lifecycle`, `camera-view`, `camera-core`).
  - Ajouter `com.google.mlkit:face-detection:16.1.7` pour la détection et les contours faciaux.
- **Gestion des états** :
  - `FaceUiState` : Liste des visages détectés, repères normalisés, émotion dominante, score de confiance (0-100%).
  - `FloatingReaction` : Liste des particules d'emojis avec coordonnées aléatoires, trajectoire ascendante et opacité progressive.
