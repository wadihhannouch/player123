package app.hibrid.hibridplayer.model

import com.google.gson.annotations.SerializedName

data class PlayerSettings (

    @SerializedName("poster") val poster : String,
    @SerializedName("channelId") val channelId : String,
    @SerializedName("signature") val signature : Signature,
    @SerializedName("daiEnabled") val daiEnabled : Boolean,
    @SerializedName("imaEnabled") val imaEnabled : Boolean,
    @SerializedName("err") val err : Int,
    @SerializedName("message") val message : String,
    @SerializedName("withCredentials") val withCredentials : Boolean
)