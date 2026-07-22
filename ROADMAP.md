# 🗺️ Roadmap Tecnológico - SpotLocal

Este documento detalla la hoja de ruta y planificación futura para la evolución de SpotLocal.

---

## 📌 Fase 1: Arquitectura Base, CI/CD y UI (Completado ✅)
- [x] Interfaz de usuario con Jetpack Compose inspirada en Spotify (Tema oscuro `#121212`).
- [x] Importación seleccionada por el usuario (Anti-archivos basura).
- [x] Persistencia de biblioteca con Room Database.
- [x] MiniPlayer persistente y PlayerFullScreen interactivo.
- [x] Sliders para velocidad (Speed) y tono (Pitch) integrados en UI.
- [x] Exportación e importación de copias de seguridad de la biblioteca en JSON.
- [x] **Barra de Volumen Flotante Personalizada estilo Spotify**:
  - Panel HUD `CustomVolumePanelHUD` en Jetpack Compose con animación slide/fade, slider táctil con nivelador de ondas, indicador de porcentaje y accesos directos de volumen.
  - Controlador `VolumeController` que intercepta los botones de volumen del hardware en `MainActivity` y sincroniza en tiempo real con `AudioManager`.
- [x] **CI/CD GitHub Actions (`apk debug` y `CI Check`)**:
  - Workflow en `.github/workflows/apk-debug.yml` para compilar y firmar el APK Debug.
  - Workflow en `.github/workflows/ci-check.yml` para ejecutar verificación CI de compilación y pruebas unitarias.
  - Optimización con filtro de rutas (`paths-ignore` para `.md` y documentación) evitando ejecuciones innecesarias.
- [x] **Licencia de Código Visible pero No Comercial (`PolyForm Noncommercial 1.0.0`) y Repositorio Cerrado**:
  - Archivo `LICENSE` integrado para proteger la propiedad intelectual permitiendo la auditoría pública de código sin autorización comercial.
  - Especificación clara de **no aceptación de Pull Requests ni contribuciones externas**.
- [x] **Consola de Logs & Capturador de Crashes Debug**: Interceptor en vivo de acciones, warnings y excepciones con stack trace dentro de la app (solo activo en compilación Debug).
- [x] **Estructura Organizativa de Almacenamiento**: Subcarpetas `music/`, `images/` y `json/` en el directorio de la aplicación.
- [x] **Navegación e Pantallas Independientes de Playlist**:
  - Pantalla dedicada `PlaylistDetailScreen` para "Canciones que te gustan", listas personalizadas y carpetas.
  - Menú desplegable por pulsación larga en canciones (`combinedClickable`) para opciones rápidas.
  - Sincronización en tiempo real del estado de favorito e icono de corazón entre reproductor, mini reproductor y listas.
- [x] **Carátulas WebP e Imágenes por Semilla**:
  - Extracción e inserción automática de carátulas embebidas convertidas a WebP.
  - Generador de carátulas artísticas mediante semilla (colores gradientes y formas geométricas) para pistas sin carátula.
  - Selección de carátula personalizada con conversión lossless a WebP ejecutada en hilo secundario (`Dispatchers.IO`).
  - Sincronización continua con caché JSON (`library_cache.json` y `track_{id}.json`).
- [x] **Soporte para Letras Sincronizadas (LRC & Texto)**:
  - Módulo `LrcParser` para parseo de archivos LRC `[mm:ss.xx]` o texto plano.
  - Vista `PlayerLyricsView` integrada en el reproductor a pantalla completa con auto-scroll y resaltado verde de la frase activa según `currentPositionMs`.
  - Diálogo de edición interactivo para pegar o modificar letras en cualquier pista.
- [x] **Garantía de Almacenamiento Limpio y Cero Archivos Huérfanos**:
  - Almacenamiento estandarizado en `context.getExternalFilesDir(null)` (`Android/data/app/files/`).
  - Eliminación automática y completa de todas las subcarpetas (`music/`, `images/`, `json/`) por parte del sistema Android al desinstalar la app.
  - Método manual `clearAllCache()` para vaciar almacenamiento en caliente.

---

## 📌 Fase 2: Integración de Rust Core (Completado ✅)
- [x] Módulo `rust_core/` con puente JNI para lectura y parsing ultra-seguro de metadatos audio (ID3v1/v2, FLAC, OGG/Vorbis, WAV).
- [x] Integración de `RustMetadataParser` en `AudioImporter` para validar archivos de audio importados sin riesgo de memory corruption.
- [x] **Detector de Canciones Duplicadas (Fingerprinting Acústico)**: Generación de hashes acústicos SHA-256 (`AudioFingerprintEngine`) y modal interactivo `DuplicateDetectorModal` para gestionar grupos duplicados.
- [x] **Limpiador y Auto-Corrector de Etiquetas ID3**: Módulo `Id3TagCleaner` para eliminar etiquetas basura de sitios de descarga y extraer metadatos limpios del nombre del archivo.
- [x] **Normalización de Volumen Automática (EBU R128 / ReplayGain)**: Cálculo y aplicación de sonoridad LUFS mediante `VolumeNormalizerEngine` y ajuste en `MusicPlayerManager`.
- [x] **Crossfade / Fundido Cruzado**: Transición de volumen suavizada entre pistas ajustable de 0 a 10 segundos en la hoja de opciones avanzadas.
- [ ] Motor de caché de imágenes de carátulas en memoria Rust segura.
- [ ] Normalización de volumen R128 / ReplayGain calculado de forma asíncrona mediante hilos de Rust.

---

## 📌 Fase 3: Integración de IA & Python (Completado / Integrado ✅)
- [x] Módulo `python_ai/` con scripts `stem_separator.py` y `export_onnx.py` para modelos de separación de voces e instrumental.
- [x] Motor Kotlin `StemSeparatorEngine` y selector en la UI para intercambiar modos: **Original, Voces, Instrumental y Karaoke**.
- [ ] Soporte para modelo ONNX expandido a 4-stems (Voces, Bajo, Batería, Otros).

---

## 📌 Fase 4: Motor DSP Nativo C++ / Oboe & Ecualizador (Completado ✅)
- [x] Estructura C++ NDK con `CMakeLists.txt` y `native-audio.cpp` vinculados a `MusicPlayerManager`.
- [x] **Ecualizador Paramétrico de 5 Bandas**: Cálculo nativo C++ NDK de coeficientes de filtro Biquad peaking EQ ($b_0, b_1, b_2, a_1, a_2$) para procesamiento de hardware.
- [x] **Visualizador de Curva de Respuesta en Frecuencia Rust**: Cálculo de atenuación/ganancia logarítmica (20Hz-20kHz) en Rust (`RustEqualizerEngine`) dibujado en Canvas con gradiente.
- [x] **Presets de Ecualización & Controles DSP**: Presets predefinidos (Rock, Pop, Jazz, Bass Boost, etc.) y controles de velocidad/tono (Pitch/Speed).
- [x] **Mejora de Audio 3D Espacial e Intensificación de Graves**:
  - Motor DSP `AudioDspEngine` / `Virtualizer` + `BassBoost` para procesamiento multicanal 3D.
  - Modos optimizados para **Bocina Única (Mono/Single)**, **Doble Bocina (Stereo)** y **Audífonos**.
  - Control de intensidad regulable de 0% a 100% integrado en la hoja de Opciones Avanzadas (`PlayerAudio3dEnhancerView`).
- [x] **Integración Sistema Android ("Abrir con..." e Intent Filters)**:
  - Intent Filter en `AndroidManifest.xml` (`ACTION_VIEW` / `ACTION_SEND`) para reproducir e importar cualquier archivo de audio local abierto desde exploradores externos.
- [x] **Notificación Nativa MediaStyle & Control en Segundo Plano**:
  - `NotificationCompat.MediaStyle` + `MediaSessionCompat` para controles en pantalla de bloqueo y barra de notificaciones del sistema Android.
- [ ] Implementación de salida de audio ultra-baja latencia en hilos nativos AAudio/OpenSL ES.
- [ ] Visualizador de espectro de frecuencia en tiempo real mediante FFT (Fast Fourier Transform) ejecutado en C++.

---

## 📌 Fase 5: Conectividad y Ecosistema 🌐
- [ ] Sincronización Wi-Fi P2P entre dispositivos (Android, PC, Tablet).
- [ ] Soporte para Chromecast y Android Auto.
