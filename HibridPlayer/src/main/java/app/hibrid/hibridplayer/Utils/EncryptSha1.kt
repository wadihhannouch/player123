package app.hibrid.hibridplayer.Utils

import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object HashUtils {
    fun getSHA1(input:String):String{
        val md: MessageDigest = MessageDigest.getInstance("SHA-1")
        val messageDigest = md.digest(input.toByteArray())
        // Convert byte array into signum representation
        val no = BigInteger(1, messageDigest)
        // Convert message digest into hex value
        var hashtext: String = no.toString(16)
        // Add preceding 0s to make it 32 bit
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        // return the HashText
        return hashtext
    }
}