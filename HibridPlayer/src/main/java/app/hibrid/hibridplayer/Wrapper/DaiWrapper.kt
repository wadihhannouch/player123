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
package app.hibrid.hibridplayer.Wrapper

import android.content.Context
import android.view.ViewGroup
import app.hibrid.hibridplayer.Player.VideoPlayer
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.SendGaTrackerEvent
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer.VideoStreamPlayerCallback
import com.google.android.exoplayer2.util.Log
import com.google.android.gms.analytics.Tracker
import java.util.*

/** This class adds ad-serving support to Sample HlsVideoPlayer  */
class DaiWrapper(
    private val context: Context,
    private val videoPlayer: VideoPlayer?,
    private val adUiContainer: ViewGroup,
    mWithIma: Boolean,
    mImaUrl: String,
    var mDaiAssetKey: String,
    var mdaiApiKey: String?,
    gaTracker: Tracker?,
    hibridSettings: HibridPlayerSettings
) : AdErrorListener, AdsLoadedListener, AdEvent.AdEventListener {

    private val sdkFactory: ImaSdkFactory = ImaSdkFactory.getInstance()
    private var adsLoader: AdsLoader? = null
    private var displayContainer: StreamDisplayContainer? = null
    private var streamManager: StreamManager? = null
    private val playerCallbacks: MutableList<VideoStreamPlayerCallback> = ArrayList()
    private var fallbackUrl: String? = null
    private  var mGaTracker : Tracker? = gaTracker;
    private  var mHibridSettings: HibridPlayerSettings = hibridSettings;


    init {
        val settings = sdkFactory.createImaSdkSettings()
        settings.playerType = PLAYER_TYPE
        val videoStreamPlayer = createVideoStreamPlayer()
        displayContainer = ImaSdkFactory.createStreamDisplayContainer(adUiContainer, videoStreamPlayer)
        videoPlayer!!.setSampleVideoPlayerCallback(
            object : VideoPlayer.SampleVideoPlayerCallback {
                override fun onUserTextReceived(userText: String?) {
                    for (callback in playerCallbacks) {
                        callback.onUserTextReceived(userText)
                    }
                }
                override fun onSeek(windowIndex: Int, positionMs: Long) {
                }
            })
        adsLoader = sdkFactory.createAdsLoader(context, settings, displayContainer)
    }

    fun requestAndPlayAds() {
        adsLoader!!.addAdErrorListener(this)
        adsLoader!!.addAdsLoadedListener(this)
        adsLoader!!.requestStream(buildStreamRequest())
    }

    private fun buildStreamRequest(): StreamRequest {
        return sdkFactory.createLiveStreamRequest(mDaiAssetKey, mdaiApiKey)
    }

    private fun createVideoStreamPlayer(): VideoStreamPlayer {
        return object : VideoStreamPlayer {
            override fun loadUrl(url: String, subtitles: List<HashMap<String, String>>) {
                videoPlayer!!.setStreamUrl(url)
                videoPlayer.play()
            }

            override fun pause() {
                videoPlayer!!.pause()
            }

            override fun resume() {
                videoPlayer!!.play()
            }

            override fun getVolume(): Int {
                return 100
            }

            override fun addCallback(videoStreamPlayerCallback: VideoStreamPlayerCallback) {
                playerCallbacks.add(videoStreamPlayerCallback)
            }

            override fun removeCallback(videoStreamPlayerCallback: VideoStreamPlayerCallback) {
                playerCallbacks.remove(videoStreamPlayerCallback)
            }

            override fun onAdBreakStarted() {
                videoPlayer!!.enableControls(false)
                SendGaTrackerEvent(mGaTracker,mHibridSettings.channelKey,"onAdBreakStarted","Dai")
            }

            override fun onAdBreakEnded() {
                // Re-enable player controls.
                videoPlayer?.enableControls(true)
                SendGaTrackerEvent(mGaTracker,mHibridSettings.channelKey,"onAdBreakEnded","Dai")
            }

            override fun onAdPeriodStarted() {
                Log.d("TAG","Ad Period Started\n")
            }

            override fun onAdPeriodEnded() {
                Log.d("TAG","Ad Period Ended\n")
            }

            override fun seek(timeMs: Long) {

                videoPlayer!!.seekTo(timeMs)
                Log.d("TAG","seek")
            }

            override fun getContentProgress(): VideoProgressUpdate {
                return VideoProgressUpdate(
                    videoPlayer!!.currentOffsetPositionMs, videoPlayer.duration
                )
            }
        }
    }

    /** AdErrorListener implementation  */
    override fun onAdError(event: AdErrorEvent) {
        // play fallback URL.
        SendGaTrackerEvent(mGaTracker,mHibridSettings.channelKey,title = "Ad Error", description = "Message: "+event.error.message.toString() + " Code :"+ event.error.errorCodeNumber.toString())
        videoPlayer!!.setStreamUrl(fallbackUrl)
        videoPlayer.enableControls(true)
        videoPlayer.play()
    }

    /** AdEventListener implementation  */
    override fun onAdEvent(event: AdEvent) {
        when (event.type) {
            AdEventType.AD_PROGRESS -> {
            }
            AdEventType.CLICKED-> {

                SendGaTrackerEvent(mGaTracker,mHibridSettings.channelKey,"ad_click","ima_ad")
            }
            else -> Log.d("TAG",String.format("Event Type: %s\n", event.type))
        }
    }

    override fun onAdsManagerLoaded(event: AdsManagerLoadedEvent) {
        streamManager = event.streamManager
        streamManager!!.addAdErrorListener(this)
        streamManager!!.addAdEventListener(this)
        streamManager!!.init()
    }


    companion object {
        private const val PLAYER_TYPE = "DAISamplePlayer"
    }



}