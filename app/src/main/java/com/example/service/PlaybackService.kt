package com.example.service

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.playback.PlaybackManager

class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return PlaybackManager.getInstance(applicationContext).mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
