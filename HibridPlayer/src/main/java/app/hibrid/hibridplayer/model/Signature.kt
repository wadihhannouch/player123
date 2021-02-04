package app.hibrid.hibridplayer.model
import com.google.gson.annotations.SerializedName

data class Signature (

	@SerializedName("imaAdTag") val imaAdTag : String,
	@SerializedName("daiApiKey") val daiApiKey : String,
	@SerializedName("streamUrl") val streamUrl : String,
	@SerializedName("daiAssetKey") val daiAssetKey : String,
	@SerializedName("gaTrackingId") val gaTrackingId : String
)