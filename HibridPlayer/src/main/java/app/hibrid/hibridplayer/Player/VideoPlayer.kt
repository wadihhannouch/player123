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
package app.hibrid.hibridplayer.Player

import android.content.Context
import android.net.Uri
import app.hibrid.hibridplayer.Wrapper.ImaWrapper
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.SendGaTrackerEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.emsg.EventMessage
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.analytics.Tracker


/** A video player that plays HLS or DASH streams using ExoPlayer.  */
class VideoPlayer(
    private val context: Context,
    private val playerView: PlayerView,
    mWithIma: Boolean,
    mImaUrl: String,
    gaTracker: Tracker?,
    hibridSettings: HibridPlayerSettings,
    defaultBandwidthMeter: DefaultBandwidthMeter,
    channelId: String?
) : Player.EventListener, VideoListener {

    /** Video player callback to be called when TXXX ID3 tag is received or seeking occurs.  */
    interface SampleVideoPlayerCallback {
        fun onUserTextReceived(userText: String?)
        fun onSeek(windowIndex: Int, positionMs: Long)
    }
    var withIma = mWithIma;
    var imaUrl = mImaUrl;
    var mChannelId = channelId;
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var playerCallback: SampleVideoPlayerCallback? = null
    private var mGaTracker:Tracker? = gaTracker;
    private var mHibridSettings  = hibridSettings
    private var mDefaultBandwidthMeter  = defaultBandwidthMeter

    @C.ContentType
    private var currentlyPlayingStreamType = C.TYPE_OTHER
    private var streamUrl: String? = null
    var isStreamRequested = false
        private set

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        SendGaTrackerEvent(mGaTracker,mChannelId!!,"Video Falvor","$width x $height")
        super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
    }
    private fun initPlayer() {
        release()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        simpleExoPlayer!!.addVideoListener(this)
        simpleExoPlayer!!.addListener(this)
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
        val contentUri = Uri.parse(streamUrl)
        val mediaItem = MediaItem.Builder().setUri(contentUri).build()
        val mediaSource: MediaSource
        currentlyPlayingStreamType = Util.inferContentType(Uri.parse(streamUrl))
        mediaSource =
        if(withIma){
            ImaWrapper().init(
                url = streamUrl!!,
                playerView = playerView,
                player = simpleExoPlayer!!,
                imaUrl = imaUrl,
                context = context,
                gaTracker = mGaTracker,
                hibridSettings = mHibridSettings,
                defaultBandwidthMeter = mDefaultBandwidthMeter,
                channelId = mChannelId
            )
        }
        else{
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
//            SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }

        simpleExoPlayer!!.setMediaSource(mediaSource)
        simpleExoPlayer!!.prepare()
        simpleExoPlayer!!.addMetadataOutput { metadata ->
            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is TextInformationFrame) {
                    val textFrame = entry
                    if ("TXXX" == textFrame.id) {
                        if (playerCallback != null) {
                            playerCallback!!.onUserTextReceived(textFrame.value)
                        }
                    }
                } else if (entry is EventMessage) {
                    val eventMessageValue = String(entry.messageData)
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

    fun setSampleVideoPlayerCallback(callback: SampleVideoPlayerCallback?) {
        playerCallback = callback
    }

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
                (window.windowStartTimeMs - window.presentationStartTimeMs)
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
        private val USER_AGENT = "ImaHibridPlayer"
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        val playingTitle = if(isPlaying ) "Play" else "Pause";
          SendGaTrackerEvent(mGaTracker,channelKey = mChannelId!!,title = playingTitle,description = playingTitle)
        super.onIsPlayingChanged(isPlaying)
    }
}