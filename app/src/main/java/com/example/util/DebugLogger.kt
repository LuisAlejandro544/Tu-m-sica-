package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogType {
    ACTION,      // Acciones de usuario / información
    WARNING,     // Advertencias
    CRASH_ERROR  // Errores graves y excepciones
}

data class LogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
    val type: LogType,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)

object DebugLogger {

    private const val TAG = "DebugLogger"
    private const val MAX_LOGS = 200

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var defaultUncaughtHandler: Thread.UncaughtExceptionHandler? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        logAction("System", "DebugLogger inicializado en APK Debug (BuildConfig.DEBUG = ${BuildConfig.DEBUG})")

        // Intercept uncaught crashes to capture full stacktrace before death/restart
        defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTraceStr = sw.toString()

            logCrash("UncaughtException", "CRASH DETECTADO en Hilo: ${thread.name} - ${throwable.localizedMessage}", throwable)

            // Save crash report to local file cache so it persists across app restarts
            try {
                val file = context.getFileStreamPath("last_crash.txt")
                file.writeText("CRASH AT ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n$stackTraceStr")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando informe de crash local", e)
            }

            defaultUncaughtHandler?.uncaughtException(thread, throwable)
        }

        // Check if there was a previous crash recorded
        try {
            val crashFile = context.getFileStreamPath("last_crash.txt")
            if (crashFile.exists()) {
                val previousCrash = crashFile.readText()
                logCrash("PreviousCrashReport", "Crash previo recuperado del almacenamiento local:\n$previousCrash")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo reporte de crash previo", e)
        }
    }

    fun logAction(tag: String, message: String) {
        Log.i(tag, message)
        appendLog(LogEntry(type = LogType.ACTION, tag = tag, message = message))
    }

    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        appendLog(LogEntry(type = LogType.WARNING, tag = tag, message = message))
    }

    fun logCrash(tag: String, message: String, throwable: Throwable? = null) {
        val stackTraceStr = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }
        Log.e(tag, message, throwable)
        appendLog(LogEntry(type = LogType.CRASH_ERROR, tag = tag, message = message, stackTrace = stackTraceStr))
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    @Synchronized
    private fun appendLog(entry: LogEntry) {
        val currentList = _logs.value.toMutableList()
        currentList.add(0, entry) // Newest logs first
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(currentList.lastIndex)
        }
        _logs.value = currentList
    }

    fun getAllLogsFormattedText(): String {
        return _logs.value.joinToString(separator = "\n-------------------------\n") { entry ->
            "[${entry.timestamp}] [${entry.type.name}] [${entry.tag}]\n${entry.message}" +
                    if (entry.stackTrace != null) "\nSTACK TRACE:\n${entry.stackTrace}" else ""
        }
    }
}
