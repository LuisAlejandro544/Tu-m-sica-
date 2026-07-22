# SpotLocal - Reproductor de Música Local de Ultra-Baja Latencia

SpotLocal es una aplicación Android nativa diseñada para reproducir música local con una interfaz fluida e intuitiva inspirada en Spotify, ofreciendo un control total sobre tus archivos de audio sin llenar tu biblioteca con tonos del sistema o notas de voz no deseadas.

---

## 🌟 Características Principales

- 🎚️ **Barra y Panel de Volumen Flotante Personalizado**:
  - Interfaz HUD estilo Spotify en Jetpack Compose (`CustomVolumePanelHUD`) con fondo glassmorphic, indicador de porcentaje dinámico y gradiente verde.
  - Intercepción completa de botones de volumen del hardware (`VolumeController`) en `MainActivity` para mostrar el panel personalizado con animación fluida y auto-ocultado tras 2.5 segundos de inactividad.
  - Incluye slider táctil interactivo con nivelador de ondas, botón de silencio rápido y accesos directos de volumen (0%, 50%, 100%), además de un botón de volumen integrado en la barra del reproductor.
- 🎵 **Filtrado Inteligente Anti-Basura & Integración con Android "Abrir con..."**:
  - Solo se importan los archivos de audio seleccionados deliberadamente por el usuario.
  - Soporte completo para comandos **"Abrir con..."** desde cualquier administrador de archivos o navegador (`ACTION_VIEW` / `ACTION_SEND` para `audio/*`), cargando y reproduciendo inmediatamente el archivo seleccionado.
- 🎧 **Notificación Nativa MediaStyle & Control en Segundo Plano**:
  - Reproductor persistente en la barra de notificaciones y pantalla de bloqueo nativa de Android mediante `NotificationCompat.MediaStyle` y `MediaSessionCompat`.
  - Permite pausar, reanudar, cambiar de pista y ver la portada/metadatos mientras la aplicación funciona en segundo plano o con la pantalla apagada.
- 🔍 **Detector de Canciones Duplicadas (Fingerprinting Acústico)**:
  - Generación de hashes acústicos espectrales SHA-256 (`AudioFingerprintEngine`) combinados con coincidencia levinstein/metadatos.
  - Interfaz interactiva `DuplicateDetectorModal` para escanear la biblioteca, agrupar archivos idénticos o similares, escuchar previsualizaciones y eliminar duplicados con un solo toque.
- 🧹 **Limpiador y Auto-Corrector de Etiquetas ID3**:
  - Motor inteligente `Id3TagCleaner` que limpia automáticamente etiquetas con textos de relleno de descargas (ej. `[ytmp3.cc]`, `[128kbps]`, `www.download...`, `(Official Video)`).
  - Extrae y corrige automáticamente Título y Artista desde el nombre del archivo cuando las etiquetas están corruptas o ausentes.
  - Ejecutable individualmente desde el menú contextual de cualquier pista o en lote para toda la biblioteca.
- 🔊 **Normalización de Volumen Automática (EBU R128 / ReplayGain)**:
  - Motor `VolumeNormalizerEngine` que analiza la sonoridad integrada en LUFS (Loudness Units Full Scale) según la norma EBU R128.
  - Ajusta dinámicamente el nivel de salida hacia un nivel objetivo deseado (por defecto -14 LUFS) en `MusicPlayerManager`, evitando saltos bruscos de volumen entre canciones.
- 🔀 **Crossfade / Fundido Cruzado Continuo**:
  - Transición suave de audio mediante rampa de volumen progresiva en corrutinas de Kotlin al aproximarse al final de cada canción.
  - Duración de fundido cruzado totalmente personalizable de 0 a 10 segundos en la hoja de Opciones Avanzadas.
- 🔊 **Mejora de Audio 3D Espacial (Para 1 y 2 Bocinas)**:
  - Procesador de sonido tridimensional `Audio3dEnhancer` respaldado por `android.media.audiofx.Virtualizer` y `BassBoost`.
  - **Modo Doble Bocina (Stereo)**: Maximiza la separación de canales y amptitud espacial en smartphones con 2 altavoces.
  - **Modo Bocina Única (Mono/Single)**: Aplica una matriz de psicoacústica para simular profundidad surround en dispositivos con un único altavoz.
  - **Modo Audífonos**: Optimiza la inmersión tridimensional para auriculares.
  - Controles de encendido/apagado, intensidad de efecto (0% a 100%) y modo de altavoces en la hoja de "Opciones Avanzadas".
- 🐞 **Consola de Logs & Interceptor de Crashes en Vivo (Exclusivo APK Debug)**:
  - Botón flotante de depuración integrado únicamente en compilaciones Debug (`BuildConfig.DEBUG`).
  - Registro categorizado en tiempo real de **Acciones del usuario**, **Warnings/Advertencias** y **Crashes/Errores** con stack traces completos expuestos.
  - Interceptor global de excepciones no capturadas para reportar fallos y copia directa al portapapeles.
- 🤖 **CI/CD con GitHub Actions (`apk debug` y `CI Check`)**:
  - Workflows automatizados en `.github/workflows/apk-debug.yml` y `.github/workflows/ci-check.yml` con filtro inteligente de archivos (`paths-ignore` para `.md` y archivos de documentación) para evitar builds innecesarios.
  - **`apk debug`**: Compila y firma automáticamente el APK de depuración con la clave de depuración estándar y genera el artefacto listo para instalar.
  - **`CI Check`**: Ejecuta la compilación de código y la suite de pruebas unitarias para garantizar que los cambios no rompan la aplicación.
- 🐍 **IA Separación de Stems con Python & ONNX**:
  - Modelo neuronal liviano (<5MB) entrenado en Python y optimizado para ONNX Runtime Mobile.
  - Permite separar pistas en **Original, Solo Voces, Solo Instrumental y Modo Karaoke** en tiempo real.
- 🦀 **Parsing Ultra-Seguro en Rust**:
  - Extracción e inspección rápida y libre de desbordamientos de memoria para etiquetas **ID3v1/ID3v2, FLAC, OGG/Vorbis y WAV/RIFF** mediante el módulo nativo Rust `spotlocal_rust_parser`.
- 🎚️ **Control DSP & Ecualizador Avanzado C++ & Rust**:
  - **Ecualizador de 5 Bandas**: Controles deslizantes para 60Hz, 230Hz, 910Hz, 3.6kHz y 14kHz (-12dB a +12dB).
  - **Filtros Biquad Nativo C++**: Cálculo en tiempo real de coeficientes de filtro Peaking EQ ($b_0, b_1, b_2, a_1, a_2$) a nivel de hardware NDK.
  - **Curva de Respuesta en Frecuencia Rust**: Cálculo de respuesta logarítmica (20Hz a 20kHz) en Rust (`RustEqualizerEngine`) renderizada dinámicamente en un Canvas interactivo con gradiente.
  - **Presets de Audio**: Plano, Bajos Potentes, Rock, Pop, Jazz, Voz Clara, Acústico, Electrónica y Personalizado.
  - **Controles DSP**: Slider para ajuste de **Velocidad de reproducción** (0.25x a 2.0x) y **Tono (Pitch Shift)** (0.25x a 2.0x).
- 🎨 **Carátulas Inteligentes & Generador por Semilla WebP**:
  - Extracción e inserción automática de carátulas embebidas de metadatos de audio convertidas a formato WebP.
  - Si la música importada no posee carátula, la app genera automáticamente una portada artística personalizada mediante colores y patrones semilla basados en el título/artista.
  - **Soporte para Carátulas Personalizadas WebP**: Opción para seleccionar cualquier imagen local, procesándola y comprimiéndola a formato WebP máxima calidad en segundo plano (`Dispatchers.IO`) para evitar que la interfaz se congele.
- 📝 **Soporte para Letras Sincronizadas (LRC & Texto)**:
  - Módulo `LrcParser` para procesamiento de letras sincronizadas con marcas de tiempo `[mm:ss.xx]`.
  - Componente `PlayerLyricsView` con desplazamiento automático inteligente y resaltado de la línea activa en tiempo real según el progreso de la canción.
  - Editor interactivo para agregar, pegar o modificar letras en cualquier canción desde el reproductor o menú contextual.
- 📁 **Estructura Organizativa Limpia en `Android/data/app/files/`**:
  - `music/`: Carpeta dedicada para música local importada.
  - `images/`: Carpeta dedicada para carátulas comprimidas en formato WebP.
  - `json/`: Carpeta dedicada con caché `library_cache.json` y archivos metadatos por canción `track_{id}.json`.
  - **Garantía de Cero Archivos Huérfanos**: Al utilizar la estructura interna de la aplicación en `getExternalFilesDir()`, el sistema operativo Android elimina **automáticamente** todo el contenido (`music/`, `images/`, `json/`) cuando el usuario desinstala la aplicación. También incluye una función manual de limpieza de caché.
- 📂 **Organización e Interfaz Independiente**:
  - Pestañas de Navegación: Inicio, Buscar y Tu Biblioteca.
  - **Pantallas Independientes de Playlist y Favoritos**: Al presionar sobre "Canciones que te gustan", una lista personalizada o una carpeta, se abre una pantalla dedicada (`PlaylistDetailScreen`) con botón de reproducción aleatoria, cabecera de portada y lista de pistas.
  - **Menú de Opciones por Pulsación Larga**: Mantener presionada cualquier canción despliega el menú contextual de opciones (favoritos, carátula personalizada, eliminación).
  - Sincronización e intercambio instantáneo de color animado con `animateColorAsState` para el icono de corazón favorito en el reproductor a tamaño completo, mini reproductor (`MiniPlayer`) y listas de reproducción.
- 💾 **Persistencia y Respaldo Local**:
  - Base de datos Room en SQLite.
  - Función de exportación de biblioteca en formato JSON para respaldos o migración.

---

## 🛠️ Stack Tecnológico Híbrido Multilenguaje

SpotLocal está construido bajo una arquitectura híbrida de alto rendimiento:

1. **Kotlin**:
   - UI declarativa con **Jetpack Compose** y **Material Design 3**.
   - Consola modal de logs y capturador de errores `DebugLogger` en compilaciones Debug.
   - Gestión de estado reactiva con `ViewModel`, `StateFlow` y `collectAsStateWithLifecycle`.
   - Persistencia local mediante **Room DB** con soporte KSP.
2. **C++ (Oboe & NDK Audio Engine)**:
   - Motor DSP nativo para procesamiento de audio en tiempo real con ultra-baja latencia.
   - Algoritmos para pitch-shifting y time-stretching eficientes a nivel de hardware.
3. **Rust (Cargo Core Parser / JNI)**:
   - Módulo en `rust_core/` enfocado en **parsing seguro de metadatos de audio (ID3, FLAC, OGG, WAV)** que elimina vulnerabilidades de memoria comunes en parsers en C tradicionales.
4. **Python (AI Stems Engine / ONNX Export)**:
   - Módulo en `python_ai/` con scripts de exportación, cuantización INT8 y ejecución de modelos Mobile-U-Net para separación de fuentes de audio (Voces vs Acompañamiento / Karaoke).
5. **GitHub Actions (CI/CD Workflows `apk debug` y `CI Check`)**:
   - Workflows en `.github/workflows/apk-debug.yml` y `.github/workflows/ci-check.yml` para ensamblar el APK Debug y ejecutar verificaciones CI automáticas con filtros de ruta (`paths-ignore`).

---

## 🚀 Cómo Ejecutar el Proyecto o Generar el APK Debug

1. **Generación Automática con GitHub Action**:
   - Cada commit o pull request activa la acción `apk debug`.
   - Puedes descargar directamente el archivo `app-debug.apk` firmado automáticamente desde la pestaña **Actions** en GitHub.
2. **Ejecución Local**:
   - Clona este repositorio o ábrelo en Android Studio.
   - Compila y ejecuta la variante `debug` para contar con la consola flotante de logs y capturador de crashes.

---

## 📄 Licencia

Este proyecto utiliza la licencia **PolyForm Noncommercial License 1.0.0** (Código Visible pero No Comercial).
Permite visualizar, estudiar y ejecutar el código para uso privado y personal no comercial. Queda estrictamente prohibida su explotación comercial, redistribución lucrativa o venta sin autorización expresa del autor.

---

## 🤝 Contribuciones y Pull Requests

**Este proyecto es un repositorio personal cerrado a contribuciones externas.**
No se aceptan Pull Requests, propuestas de código ni contribuciones de terceros. El repositorio se mantiene público únicamente como demostración, uso personal y auditoría de código visible.

