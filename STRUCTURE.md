# 📂 Estructura del Proyecto - SpotLocal

Este documento detalla la arquitectura de carpetas, capas de código y responsabilidades del proyecto SpotLocal.

---

## 🏗️ Árbol de Directorios Principal

```
SpotLocal/
├── .github/
│   └── workflows/
│       └── apk-debug.yml                         # Workflow GitHub Action "apk debug" para generar APK Debug
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt               # Entrypoint principal, inicializa ViewModel, Launchers y delegación a Scaffold
│   │   │   │   ├── data/
│   │   │   │   │   ├── ai/
│   │   │   │   │   │   ├── StemSeparatorEngine.kt# Coordinador del estado de separación de Stems (Voces vs Instrumental)
│   │   │   │   │   │   ├── StemMode.kt           # Enums y data classes para los estados y atenuación de Stems
│   │   │   │   │   │   └── OnnxInferenceRunner.kt# Ejecutor modular de inferencias ONNX / Python AI
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── TrackEntity.kt        # Entidad de canción importada
│   │   │   │   │   │   ├── PlaylistEntity.kt     # Entidad de lista de reproducción
│   │   │   │   │   │   ├── TrackDao.kt           # Data Access Object para canciones
│   │   │   │   │   │   ├── PlaylistDao.kt        # Data Access Object para playlists
│   │   │   │   │   │   └── AppDatabase.kt        # Base de datos Room SQLite
│   │   │   │   │   ├── storage/
│   │   │   │   │   │   └── LocalStorageManager.kt # Gestor de almacenamiento en android/data/app/ (music/, images/, json/)
│   │   │   │   │   ├── importer/
│   │   │   │   │   │   ├── AudioImporter.kt      # Módulo de importación y extracción de etiquetas
│   │   │   │   │   │   └── SampleAudioGenerator.kt # Generador de canciones demo e imágenes semilla WebP
│   │   │   │   │   └── rust/
│   │   │   │   │       └── RustMetadataParser.kt # Puente Kotlin para el motor de parsing seguro en Rust
│   │   │   │   ├── player/
│   │   │   │   │   ├── MusicPlayerManager.kt     # Gestor modular de reproducción, colas, crossfade y normalización
│   │   │   │   │   ├── VolumeController.kt       # Controlador de volumen de sistema e intercepción de hardware
│   │   │   │   │   ├── VolumeNormalizerEngine.kt # Motor de análisis y normalización de volumen EBU R128 (LUFS)
│   │   │   │   │   ├── PlaybackState.kt          # Definición de modos de repetición y estados de parámetros
│   │   │   │   │   ├── AudioDspEngine.kt         # Motor DSP de velocidad, pitch, ecualización y efectos 3D (Virtualizer/BassBoost)
│   │   │   │   │   ├── Audio3dSpeakerMode.kt     # Enums de modo de bocinas (Single, Dual, Headphones)
│   │   │   │   │   ├── MediaNotificationManager.kt # Gestor de notificaciones MediaStyle en segundo plano
│   │   │   │   │   └── MusicPlaybackService.kt   # Servicio Foreground para reproducción continua con pantalla apagada
│   │   │   │   ├── util/
│   │   │   │   │   ├── AudioFingerprintEngine.kt# Motor de hashes acústicos y detección de duplicados
│   │   │   │   │   ├── Id3TagCleaner.kt          # Limpiador y auto-corrector inteligente de metadatos ID3
│   │   │   │   │   ├── DebugLogger.kt            # Registrador e interceptor de acciones, warnings y crashes
│   │   │   │   │   └── LrcParser.kt              # Parser de letras sincronizadas [mm:ss.xx] LRC y texto plano
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── SpotLocalMainScaffold.kt # Scaffold modular principal con navegación y modales
│   │   │   │   │   │   ├── MiniPlayer.kt         # Reproductor en barra flotante
│   │   │   │   │   │   ├── TrackItem.kt          # Elemento individual de lista de canción
│   │   │   │   │   │   ├── SpotifyBottomNav.kt   # Barra de navegación inferior
│   │   │   │   │   │   ├── DebugLogConsoleModal.kt # Consola modal de logs en vivo para la APK Debug
│   │   │   │   │   │   ├── TrackOptionsDialog.kt # Modal de opciones de canción (Limpieza ID3, Portada, Favoritos)
│   │   │   │   │   │   ├── ImportExportDialog.kt # Diálogos de creación de listas e info
│   │   │   │   │   │   ├── library/
│   │   │   │   │   │   │   └── DuplicateDetectorModal.kt # Modal interactivo para escanear y eliminar duplicados acústicos
│   │   │   │   │   │   └── player/               # Módulos descomprimidos del reproductor pantalla completa
│   │   │   │   │   │       ├── CustomVolumePanelHUD.kt # Panel flotante HUD de volumen personalizado estilo Spotify
│   │   │   │   │   │       ├── PlayerTopBar.kt   # Barra superior de navegación con botón de letras y volumen
│   │   │   │   │   │       ├── PlayerAlbumArt.kt # Componente de portada de álbum
│   │   │   │   │   │       ├── PlayerLyricsView.kt # Vista de letras sincronizadas LRC con auto-scroll y editor
│   │   │   │   │   │       ├── PlayerTrackHeader.kt # Cabecera de título, artista y botón favorito
│   │   │   │   │   │       ├── PlayerSeekBar.kt  # Barra de progreso y tiempo de reproducción
│   │   │   │   │   │       ├── PlayerPlaybackControls.kt # Botones de reproducción (Play, Prev, Next, Shuffle, Repeat)
│   │   │   │   │   │       ├── PlayerDspControls.kt # Sliders de velocidad y tono DSP
│   │   │   │   │   │       ├── PlayerEqSheet.kt  # Hoja modal de Ecualizador Avanzado de 5 Bandas y Presets
│   │   │   │   │   │       ├── EqResponseCurveCanvas.kt # Gráfico Canvas de curva de respuesta en frecuencia Rust
│   │   │   │   │   │       ├── EqPreset.kt       # Presets de ecualización (Plano, Rock, Pop, Jazz, Bass Boost)
│   │   │   │   │   │       ├── PlayerStemSelector.kt # Selector de modo IA de Stems (Original, Voces, Instrumental, Karaoke)
│   │   │   │   │   │       ├── PlayerAudio3dEnhancerView.kt # Componente de Mejora de Audio 3D (Bocina única, Doble, Audífonos)
│   │   │   │   │   │       └── PlayerFooterBadge.kt # Badge de verificación de archivo local y cola
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt         # Pantalla principal e importación rápida
│   │   │   │   │   │   ├── SearchScreen.kt       # Búsqueda y categorías
│   │   │   │   │   │   ├── LibraryScreen.kt      # Biblioteca organizada con filtros
│   │   │   │   │   │   ├── PlaylistDetailScreen.kt # Pantalla independiente de detalle de playlist, carpetas y canciones favoritas
│   │   │   │   │   │   └── PlayerFullScreen.kt   # Reproductor expansivo compuesto por subcomponentes modulares
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   └── Theme.kt              # Paleta de colores Spotify
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── PlayerViewModel.kt    # ViewModel central con StateFlows
│   │   │   └── cpp/                               # Módulo nativo C++ / Oboe (Audio Engine)
│   │   │       ├── native-audio.cpp              # Implementación C++ JNI
│   │   │       └── CMakeLists.txt                # Configuración de compilación CMake NDK
│   ├── rust_core/                                # Módulo nativo Rust (Tag Parsing Engine)
│   │   ├── Cargo.toml                            # Configuración y dependencias Cargo (Lofty, JNI)
│   │   └── src/
│   │       └── lib.rs                            # Código Rust para parsing ultra-seguro de ID3, FLAC, OGG, WAV
│   ├── python_ai/                                # Módulo Python (AI Stem Separator / ONNX Export)
│   │   ├── model_config.py                       # Especificación de arquitectura y metadatos del modelo AI
│   │   ├── audio_processor.py                    # Funciones matemáticas de audio y ganancia de Stems
│   │   ├── stem_separator.py                     # Wrapper CLI modular para inferencia de separación de stems
│   │   └── export_onnx.py                        # Exporter modular para cuantizar modelos a ONNX
│   ├── gradle/
│   │   └── libs.versions.toml                    # Catálogo de versiones centralizado
│   ├── README.md                                 # Guía e introducción del proyecto
│   ├── ROADMAP.md                                # Planificación de fases y características
│   ├── STRUCTURE.md                              # Este archivo de arquitectura de archivos
│   ├── AI_CONTEXT.md                             # Manual contextual para asistentes de IA
│   └── AGENTS.md                                 # Instrucciones persistentes para agentes IA
```

---

## ⚙️ Descripción de Capas

### 1. Capa CI/CD (`.github/workflows/`)
- `apk-debug.yml`: Workflow GitHub Action llamado `apk debug` para compilar y firmar automáticamente el APK Debug en cada cambio.

### 2. Capa de Presentación (`ui/` y `util/`)
Construida exclusivamente con **Jetpack Compose** y **Material Design 3**.
- `screens/`: Vistas completas de la aplicación.
- `components/`: UI modular reutilizable. Incluye `SpotLocalMainScaffold.kt` para la estructura global de navegación y el paquete `components/player/` con componentes modulares desacoplados (`PlayerTopBar`, `PlayerAlbumArt`, `PlayerTrackHeader`, `PlayerSeekBar`, `PlayerPlaybackControls`, `PlayerDspControls`, `PlayerStemSelector`, `PlayerFooterBadge`).
- `util/DebugLogger.kt`: Módulo capturador de eventos, advertencias y excepciones no atrapadas.

### 3. Capa de Dominio y Estado (`viewmodel/`, `player/` y `data/ai/`)
- `PlayerViewModel.kt`: Centraliza el estado de la UI y las operaciones de importación/exportación JSON.
- `MusicPlayerManager.kt`: Encapsula la reproducción de audio y delegación a motores modulares (`PlaybackState` y `AudioDspEngine`).
- `AudioDspEngine.kt`: Procesa ajustes de velocidad, tono y puente de audio JNI con el motor Oboe C++.
- `StemSeparatorEngine.kt`: Administra el estado de la IA de separación de audio mediante `StemMode.kt` y `OnnxInferenceRunner.kt`.

### 4. Capa de Datos e Almacenamiento (`data/`)
- **Gestor de Almacenamiento Local (`data/storage/LocalStorageManager.kt`)**: Organiza y mantiene el sistema de archivos en `android/data/com.aistudio.../files/`:
  - `music/`: Directorio asignado a archivos de música local importados.
  - `images/`: Directorio de carátulas procesadas e imágenes personalizadas WebP a máxima calidad en hilos secundarios.
  - `json/`: Caché de respaldo estructurado `library_cache.json` y metadatos individuales `track_{id}.json` para acelerar la carga inicial de la biblioteca.
- **Room Database (`data/db/`)**: Persiste de forma reactiva todas las canciones importadas, listas de reproducción y estado de favoritos.
- **Rust Tag Parser (`data/rust/`)**: Puente JNI seguro hacia el motor Rust `spotlocal_rust_parser` para parsing ultra-seguro de archivos ID3, FLAC, OGG y WAV.

### 5. Capa Nativa C++ (`cpp/`)
- Diseñada para la integración con **Oboe** (Motor de audio NDK de baja latencia de Google). Permite procesar audio mediante flujos AAudio/OpenSL ES a nivel de sistema.

### 6. Capa Nativa Rust (`rust_core/`)
- Módulo en **Rust** para parsing de etiquetas e inspección de archivos de audio.

### 7. Capa IA Python (`python_ai/`)
- Módulo en **Python** para desarrollo, prueba y exportación ONNX de modelos livianos de separación de stems, estructurado en `model_config.py`, `audio_processor.py`, `stem_separator.py` y `export_onnx.py`.
