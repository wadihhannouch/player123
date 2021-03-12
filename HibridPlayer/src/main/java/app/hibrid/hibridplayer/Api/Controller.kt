package app.hibrid.hibridplayer.Api

import app.hibrid.hibridplayer.model.PlayerSettings
import com.google.android.exoplayer2.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Controller {
    fun start(channelKey: String, t:String,h:String) :Call<PlayerSettings?>{
         var BASE_URL = "https://hiplayer.hibridcdn.net/c/"
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val interfaceApi = retrofit.create(IntefaceAPI::class.java)
        val call: Call<PlayerSettings?>? = interfaceApi.getSettings(channelKey, t,h);
        return call!!;

    }


}

