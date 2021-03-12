package app.hibrid.hibridplayer.Api

import app.hibrid.hibridplayer.model.PlayerSettings
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface IntefaceAPI {

    @GET("{channelKey}")
    fun getSettings(@Path("channelKey") channelKey:String, @Query("t") timeStamp: String?, @Query("h") hashSha1: String?): Call<PlayerSettings?>?
}