# 🤖 AI_CONTEXT.md - Manual Contextual para Asistentes e IAs

Este archivo actúa como la guía oficial para cualquier inteligencia artificial (Gemini, ChatGPT, Claude, Copilot, etc.) o desarrollador que colabore en la base de código de **SpotLocal**.

---

## 🎯 Visión y Reglas de Oro del Proyecto

1. **Propósito del Proyecto**:
   SpotLocal es un reproductor de música local de alta fidelidad, con estética Spotify y filtrado inteligente. No escanea ni añade audios de WhatsApp, notas de voz ni tonos del sistema. El usuario decide exactamente qué archivos importar.

2. **Stack Tecnológico Multilenguaje OBLIGATORIO**:
   - **Kotlin**: Lenguaje primario para la UI (Jetpack Compose), ViewModels, Room DB y lógica Android. Incluye `DebugLogger` para capturar acciones, advertencias y crashes en tiempo real.
   - **C++ (Oboe / NDK)**: Lenguaje para el motor de audio nativo de baja latencia, cálculo Biquad de coeficientes de ecualización de 5 bandas y algoritmos DSP (Pitch shift, Time stretch).
   - **Rust (Cargo Core / JNI)**: Lenguaje activo para **parsing ultra-seguro de metadatos (ID3, FLAC, OGG, WAV)** y cálculo de curva de respuesta en frecuencia para el ecualizador visual.
   - **Python (AI Stems Engine / ONNX Export)**: Lenguaje oficial para la creación, cuantización (INT8) y prueba de modelos ligeros (<5MB) de **separación de voz e instrumental (Stems)** compatibles con ONNX Runtime en Android.
   - **GitHub Actions (CI/CD)**: Workflow `apk debug` en `.github/workflows/apk-debug.yml` para compilar y firmar automáticamente el APK Debug listo para instalar en dispositivos reales.

3. **Principios de Diseño e Interfaz**:
   - Tema oscuro inspirado en Spotify: Fondo base `#121212`, tarjetas `#282828` / `#181818`, verde acento `#1DB954`.
   - Modificadores Compose: Todos los elementos interactivos clave deben incluir su respectivo `testTag` en `snake_case` (ej. `speed_slider`, `pitch_slider`, `debug_log_fab`).
   - Sin componentes genéricos "AI Slop": Utiliza espaciado generoso, bordes redondeados (6.dp a 12.dp), textos nítidos y contraste elevado.

---

## 🏗️ Pautas para la IA al Modificar Código

### En Código Kotlin:
- Usa `collectAsStateWithLifecycle` para consumir flujos en Composables.
- Mantén la reactividad con `StateFlow` y `MutableStateFlow` en `MusicPlayerManager`, `PlayerViewModel` y `StemSeparatorEngine`.
- Sincroniza en tiempo real los estados de favorito (`isFavorite`) e icono de corazón con cambio de color animado (`animateColorAsState`) entre el reproductor a pantalla completa, mini reproductor (`MiniPlayer`) y las listas.
- Pantalla dedicada `PlaylistDetailScreen` para "Canciones que te gustan", playlists personalizadas y carpetas.
- Menú contextual desplegable por pulsación larga (`combinedClickable`) en elementos de pista (`TrackItem`).
- Utiliza `LrcParser` para parsear letras sincronizadas con marcas de tiempo `[mm:ss.xx]` y `PlayerLyricsView` para auto-scroll y resaltado en verde de la frase activa según el tiempo actual.
- Utiliza `DebugLogger.logAction()`, `DebugLogger.logWarning()` y `DebugLogger.logCrash()` para eventos clave que ayuden al diagnóstico en dispositivos reales.
- Utiliza `LocalStorageManager` para la gestión de subcarpetas en `android/data/app/files/` (`music/`, `images/`, `json/`), garantizando que al desinstalar la app, el sistema operativo elimine automáticamente todos los archivos sin dejar residuos. Incluye la función `clearAllCache()` para vaciado manual.
- Utiliza `RustMetadataParser` al importar o inspeccionar metadatos de archivos de audio.
- La Mejora de Audio 3D Espacial se gestiona mediante `AudioDspEngine` (`Virtualizer` + `BassBoost`) apoyado por `Audio3dSpeakerMode` (`SINGLE_SPEAKER`, `DUAL_SPEAKER`, `HEADPHONES`) y `PlayerAudio3dEnhancerView`.
- La Detección de Duplicados Acústicos utiliza `AudioFingerprintEngine` (hashes SHA-256 espectrales) y la vista modal `DuplicateDetectorModal`.
- La Limpieza e Inspección de Etiquetas ID3 se ejecuta mediante `Id3TagCleaner` para eliminar patrones de descarga basurita (`[ytmp3.cc]`, etc.) e inferir títulos/artistas limpios.
- La Normalización de Volumen utiliza `VolumeNormalizerEngine` conforme a EBU R128 (-14 LUFS) ajustando dinámicamente el multiplicador de salida en `MusicPlayerManager`.
- El Panel de Volumen Flotante Personalizado utiliza `VolumeController` e `CustomVolumePanelHUD` para interceptar las teclas físicas de volumen del hardware en `MainActivity` mostrando un indicador estético en Jetpack Compose con degradado verde, slider táctil e indicador de ondas.
- El Fundido Cruzado (Crossfade) se gestiona con corrutinas de ajuste progresivo de volumen al aproximarse al final del tema en `MusicPlayerManager`.
- La reproducción continua y la notificación interactiva de sistema en pantalla de bloqueo se sincroniza con `MediaNotificationManager` y `MusicPlaybackService` (`MediaStyle` + `MediaSessionCompat`).
- El soporte para "Abrir con..." desde exploradores de archivos externos se procesa mediante `handleIncomingAudioIntent(intent)` en `MainActivity`.

### En CI/CD y Workflows GitHub:
- El workflow de GitHub Actions debe llamarse exactamente `apk debug` (`.github/workflows/apk-debug.yml`) y generar el APK Debug firmado por defecto.

### En Código C++ / JNI:
- Las llamadas nativas C++ deben gestionarse a través del paquete `com.example.player`.
- Asegura la gestión de memoria nativa (evita memory leaks en JNI env pointers y arrays).

### En Código Rust (`rust_core/`):
- Mantén las firmas JNI expuestas bajo la convención de paquetes Java de Android (`Java_com_example_data_rust_...`).
- Utiliza bibliotecas puras en Rust (como `lofty` o `id3`) que garantizan la seguridad de memoria sin `unsafe` blocks innecesarios.

### En Código Python (`python_ai/`):
- Los modelos entrenados o exportados deben ser ultraligeros (<5MB, cuantizados INT8) para permitir su ejecución local en cualquier procesador o NPU móvil.

### En Documentación y Archivos de Configuración:
- Mantén en sincronía constante los 4 archivos .md principales (`README.md`, `ROADMAP.md`, `STRUCTURE.md`, `AI_CONTEXT.md`) y `AGENTS.md`.
- Nunca hardcodees claves secretas o rutas absolutas de archivo local en el repositorio.

---

## 📌 Guía Rápida de Comandos para la IA

- **Compilar Applet**: Utiliza la herramienta del sistema `compile_applet` para verificar que el código compila sin errores de Kotlin ni Gradle.
- **Nombres de Recursos**: Todas las cadenas visibles para el usuario deben mantenerse o agregarse con nombrado descriptivo en `strings.xml`.
