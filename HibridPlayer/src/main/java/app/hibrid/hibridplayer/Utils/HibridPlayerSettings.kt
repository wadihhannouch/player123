package app.hibrid.hibridplayer.Utils

class HibridPlayerSettings(
    var withIma: Boolean = false,
    var withDai: Boolean = false,
    var imaUrl: String = "",
    var daiAssetKey: String = "",
    var daiApiKey: String? = "",
    var autoplay: Boolean = true,
    var baseUrl: String,
    var channelKey: String
);
