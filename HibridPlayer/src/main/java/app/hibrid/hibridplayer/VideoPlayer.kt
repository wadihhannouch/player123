/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import app.hibrid.hibridplayer.todelete.DaiWrapper
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.emsg.EventMessage
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/** A video player that plays HLS or DASH streams using ExoPlayer.  */
class VideoPlayer(
    private val context: Context,
    private val playerView: PlayerView,
    mWithIma: Boolean,
    mImaUrl: String
) {

    /** Video player callback to be called when TXXX ID3 tag is received or seeking occurs.  */
    interface SampleVideoPlayerCallback {
        fun onUserTextReceived(userText: String?)
        fun onSeek(windowIndex: Int, positionMs: Long)
    }
    var withIma = mWithIma;
    var imaUrl = mImaUrl;
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playerCallback: SampleVideoPlayerCallback? = null

    @C.ContentType
    private var currentlyPlayingStreamType = C.TYPE_OTHER
    private var streamUrl: String? = null
    var isStreamRequested = false
        private set

    private fun initPlayer() {
        release()
        simpleExoPlayer = SimpleExoPlayer.Builder(context).build()
        playerView.player = simpleExoPlayer
        simpleExoPlayer!!.playWhenReady = true
        playerView.setControlDispatcher(
            object : ControlDispatcher {
                override fun dispatchSetPlayWhenReady(
                    player: Player,
                    playWhenReady: Boolean
                ): Boolean {
                    player.playWhenReady = playWhenReady
                    return playWhenReady
                }

                override fun isRewindEnabled(): Boolean {
                    return false
                }

                override fun isFastForwardEnabled(): Boolean {
                    return false
                }

                override fun dispatchFastForward(p: Player): Boolean {
                    return false
                }

                override fun dispatchRewind(p: Player): Boolean {
                    return false
                }

                override fun dispatchNext(p: Player): Boolean {
                    return false
                }

                override fun dispatchPrevious(p: Player): Boolean {
                    return false
                }

                override fun dispatchSeekTo(
                    player: Player,
                    windowIndex: Int,
                    positionMs: Long
                ): Boolean {
                    return true
                }

                override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
                    return false
                }

                override fun dispatchSetShuffleModeEnabled(
                    player: Player,
                    shuffleModeEnabled: Boolean
                ): Boolean {
                    return false
                }

                override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                    return false
                }
            })
    }

    fun play() {
        if (isStreamRequested) {
            simpleExoPlayer!!.playWhenReady = true
            return
        }
        initPlayer()

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context, USER_AGENT
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        mediaSourceFactory.setAdViewProvider(playerView)
        // Create the MediaItem to play, specifying the content URI.
        val contentUri = Uri.parse(streamUrl)
        val mediaItem = MediaItem.Builder().setUri(contentUri).build()
        val mediaSource: MediaSource
        currentlyPlayingStreamType = Util.inferContentType(Uri.parse(streamUrl))

        mediaSource =
        if(withIma){
            ImaWrapper().init(
                playerView = playerView,
                url = streamUrl!!,
                imaUrl = imaUrl,
                context = context,
                player = simpleExoPlayer!!
            )
        }
        else{
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }

        simpleExoPlayer!!.setMediaSource(mediaSource)
        simpleExoPlayer!!.prepare()
        // Register for ID3 events.



        simpleExoPlayer!!.addMetadataOutput { metadata ->
            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is TextInformationFrame) {
                    val textFrame = entry
                    if ("TXXX" == textFrame.id) {
                        Log.d(LOG_TAG, "Received user text: " + textFrame.value)
                        if (playerCallback != null) {
                            playerCallback!!.onUserTextReceived(textFrame.value)
                        }
                    }
                } else if (entry is EventMessage) {
                    val eventMessageValue = String(entry.messageData)
                    Log.d(LOG_TAG, "Received user text: $eventMessageValue")
                    if (playerCallback != null) {
                        playerCallback!!.onUserTextReceived(eventMessageValue)
                    }
                }
            }
        }
        isStreamRequested = true
    }

    fun pause() {
        simpleExoPlayer!!.playWhenReady = false
    }

    fun seekTo(positionMs: Long) {
        simpleExoPlayer!!.seekTo(positionMs)
    }

    fun seekTo(windowIndex: Int, positionMs: Long) {
        simpleExoPlayer!!.seekTo(windowIndex, positionMs)
    }

    private fun release() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
            isStreamRequested = false
        }
    }

    fun setStreamUrl(streamUrl: String?) {
        this.streamUrl = streamUrl
        isStreamRequested = false // request new stream on play
    }

    fun enableControls(doEnable: Boolean) {
        if (doEnable) {
            playerView.showController()
        } else {
            playerView.hideController()
        }
    }

    // Methods for exposing player information.
    fun setSampleVideoPlayerCallback(callback: SampleVideoPlayerCallback?) {
        playerCallback = callback
    }// Adjust position to be relative to start of period rather than window, to account for DVR
    // window.
// This case is when the dash stream has a format of non-sliding window.
    /** Returns current offset position of the playhead in milliseconds for DASH and HLS stream.  */
    val currentOffsetPositionMs: Long
        get() {
            val currentTimeline = simpleExoPlayer!!.currentTimeline
            if (currentTimeline.isEmpty) {
                return simpleExoPlayer!!.currentPosition
            }
            val window = Timeline.Window()
            simpleExoPlayer!!.currentTimeline.getWindow(
                simpleExoPlayer!!.currentWindowIndex,
                window
            )
            if (window.isLive && currentlyPlayingStreamType == C.TYPE_DASH) {
                // This case is when the dash stream has a format of non-sliding window.
                return if (window.presentationStartTimeMs == C.TIME_UNSET
                    || window.windowStartTimeMs == C.TIME_UNSET
                ) {
                    simpleExoPlayer!!.currentPosition
                } else simpleExoPlayer!!.currentPosition
                +window.windowStartTimeMs - window.presentationStartTimeMs
            } else {
                // Adjust position to be relative to start of period rather than window, to account for DVR
                // window.
                val period = currentTimeline.getPeriod(
                    simpleExoPlayer!!.currentPeriodIndex,
                    Timeline.Period()
                )
                return simpleExoPlayer!!.currentPosition - period.positionInWindowMs
            }
        }
    val duration: Long
        get() = simpleExoPlayer!!.duration

    companion object {
        private const val LOG_TAG = "SampleVideoPlayer"
        private val USER_AGENT =
            "ImaSamplePlayer (Linux;Android " + Build.VERSION.RELEASE + ") ImaSample/1.0"
    }
}