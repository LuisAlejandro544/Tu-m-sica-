package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.MainActivity
import com.example.R
import com.example.data.db.TrackEntity
import java.io.File

class MediaPlaybackService : Service() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaSession()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles del reproductor nativo de Android en segundo plano"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "SpotLocalMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    MusicPlayerManager.instance?.togglePlayPause()
                }

                override fun onPause() {
                    MusicPlayerManager.instance?.togglePlayPause()
                }

                override fun onSkipToNext() {
                    MusicPlayerManager.instance?.nextTrack()
                }

                override fun onSkipToPrevious() {
                    MusicPlayerManager.instance?.previousTrack()
                }

                override fun onSeekTo(pos: Long) {
                    MusicPlayerManager.instance?.seekTo(pos)
                }

                override fun onStop() {
                    MusicPlayerManager.instance?.togglePlayPause()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY_PAUSE -> MusicPlayerManager.instance?.togglePlayPause()
            ACTION_NEXT -> MusicPlayerManager.instance?.nextTrack()
            ACTION_PREVIOUS -> MusicPlayerManager.instance?.previousTrack()
            ACTION_STOP -> {
                MusicPlayerManager.instance?.togglePlayPause()
                stopSelf()
            }
            ACTION_UPDATE -> {
                val track = currentTrack
                if (track != null) {
                    updateNotificationAndSession(track, isPlaying, currentPositionMs, durationMs, playbackSpeed)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun updateNotificationAndSession(
        track: TrackEntity,
        isPlaying: Boolean,
        positionMs: Long,
        durationMs: Long,
        speed: Float
    ) {
        val session = mediaSession ?: return

        // 1. Configurar estado de reproducción para la MediaSession
        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
                positionMs,
                speed
            )
            .build()
        session.setPlaybackState(playbackState)

        // 2. Cargar carátula WebP/JPG de la pista actual si está disponible
        val coverBitmap = loadCoverBitmap(track.coverArtPath)
        val metadataBuilder = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)

        if (coverBitmap != null) {
            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, coverBitmap)
        }
        session.setMetadata(metadataBuilder.build())

        // 3. PendingIntents para interactuar desde la notificación nativa
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construir notificación nativa en estilo Media
        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(session.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setStyle(mediaStyle)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(track.title)
            .setContentText("${track.artist} • ${track.album}")
            .setSubText("SpotLocal Player")
            .setContentIntent(contentIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)

        if (coverBitmap != null) {
            builder.setLargeIcon(coverBitmap)
        }

        // Acciones: Anterior, Reproducir/Pausa, Siguiente
        builder.addAction(
            Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_previous),
                "Anterior",
                prevIntent
            ).build()
        )

        builder.addAction(
            Notification.Action.Builder(
                Icon.createWithResource(
                    this,
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                ),
                if (isPlaying) "Pausar" else "Reproducir",
                playPauseIntent
            ).build()
        )

        builder.addAction(
            Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_next),
                "Siguiente",
                nextIntent
            ).build()
        )

        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun loadCoverBitmap(coverPath: String?): Bitmap? {
        if (coverPath.isNullOrEmpty()) return null
        return try {
            val file = File(coverPath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "spotlocal_media_channel_id"
        const val NOTIFICATION_ID = 1001

        const val ACTION_UPDATE = "com.example.spotlocal.ACTION_UPDATE"
        const val ACTION_PLAY_PAUSE = "com.example.spotlocal.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.spotlocal.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.spotlocal.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.spotlocal.ACTION_STOP"

        var currentTrack: TrackEntity? = null
        var isPlaying: Boolean = false
        var currentPositionMs: Long = 0L
        var durationMs: Long = 0L
        var playbackSpeed: Float = 1.0f

        fun syncNotification(
            context: Context,
            track: TrackEntity?,
            playing: Boolean,
            positionMs: Long,
            duration: Long,
            speed: Float
        ) {
            currentTrack = track
            isPlaying = playing
            currentPositionMs = positionMs
            durationMs = duration
            playbackSpeed = speed

            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_UPDATE
            }

            try {
                if (track != null && (playing || positionMs > 0)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } else {
                    context.stopService(intent)
                }
            } catch (e: Exception) {
                Log.e("MediaPlaybackService", "Sincronización de notificación de medios falló", e)
            }
        }
    }
}
