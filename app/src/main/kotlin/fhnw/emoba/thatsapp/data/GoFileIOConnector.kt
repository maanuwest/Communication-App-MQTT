package fhnw.emoba.thatsapp.data.gofileio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

/*

Eintrag im AndroidManifest.xml

    <!-- Zugriff auf's Internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

*/

// token generieren: curl https://store4.gofile.io/createAccount

class GoFileIOConnector() {
    private var TOKEN: String = ""
    private var SERVER: String = ""

    init {
        getServer()
        setToken()
    }

    private fun setToken(){
        val url = "https://$SERVER.gofile.io/createAccount"

        with(URL(url).openConnection() as HttpsURLConnection) {
            addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0"
            )
            instanceFollowRedirects = false
            try {
                connect()
                val code = responseCode
                if (code == HttpURLConnection.HTTP_OK) {
                    val response = JSONObject(message())
                    println(response.toString())
                    TOKEN = response.getJSONObject("data").getString("token")
                } else {
                    println("code: $code")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getServer() {
        val url = "https://api.gofile.io/getServer"

        with(URL(url).openConnection() as HttpsURLConnection) {
            addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0"
            )
            instanceFollowRedirects = false
            try {
                connect()
                val code = responseCode
                if (code == HttpURLConnection.HTTP_OK) {
                    val response = JSONObject(message())
                    println(response.toString())
                    SERVER = response.getJSONObject("data").getString("server")
                } else {
                    println("code: $code")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uploadBitmapToGoFileIO(
        bitmap: Bitmap,
        onSuccess: (url: String) -> Unit,
        onError: (responseCode: Int, json: String) -> Unit = { _, _ -> }
    ) {
        val file = "photo.jpg"
        val crlf = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****Boundary*****"
        getServer()
        with(URL("https://$SERVER.gofile.io/uploadFile").openConnection() as HttpsURLConnection) {
            //Request
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

            val requestStringStart = crlf + crlf +
                    twoHyphens + boundary + crlf +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"$file\"" + crlf + crlf
            val requestStringEnd = crlf + twoHyphens + boundary + twoHyphens + crlf + crlf

            val request = DataOutputStream(getOutputStream())

            request.writeBytes(requestStringStart)
            request.write(bitmap.asByteArray())
            request.writeBytes(requestStringEnd)

            request.flush()
            request.close()
            val responseC = responseCode
            println("responseCode: $responseC")
            //Response
            if (responseC == 200) {
                val response = message()
                println(JSONObject(response).toString())
                val parentFolder =
                    JSONObject(response).getJSONObject("data").getString("parentFolder")
                val fileName = JSONObject(response).getJSONObject("data").getString("fileName")

                onSuccess(parentFolder)
            } else {
                return onError(responseCode, "")
            }
        }
    }

    private fun downloadJSONFromGoFileIO(parentFolder: String): String {
        val url =
            "https://api.gofile.io/getContent?contentId=$parentFolder&token=$TOKEN&websiteToken=12345"
        var targetLink = ""
        with(URL(url).openConnection() as HttpsURLConnection) {
            addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0"
            )
            instanceFollowRedirects = false
            try {
                connect()
                val code = responseCode
                if (code == HttpURLConnection.HTTP_OK) {
                    val response = JSONObject(message())
                    println(response.toString())
                    val childName = response.getJSONObject("data").getJSONArray("childs").getString(0)
                    targetLink = response.getJSONObject("data").getJSONObject("contents").getJSONObject(childName).getString("link")
                } else {
                    println("code: $code")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return targetLink
    }

    fun downloadBitmapFromGoFileIO(
        parentFolder: String,
        onSuccess: (bitmap: Bitmap) -> Unit,
        onDeleted: () -> Unit = {},
        onError: (exception: Exception) -> Unit = { e -> e.printStackTrace() }
    ) {
        val url = downloadJSONFromGoFileIO(parentFolder)
        println("url: $url")
        with(URL(url).openConnection() as HttpsURLConnection) {

            addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0"
            )
            addRequestProperty("Accept", "Accept: image/avif,image/webp,*/*")
            addRequestProperty("Accept-Encoding", "Accept-Encoding: gzip, deflate, br")
            addRequestProperty("Host", "${url.subSequence(8, url.indexOf("."))}.gofile.io")
            addRequestProperty("Sec-Fethc-Dest", "image")
            addRequestProperty("Sec-Fetch-Mode", "no-cors")
            addRequestProperty("Sec-Fetch-Site", "same-site")
            addRequestProperty("Cookie", "accountToken=$TOKEN")

            instanceFollowRedirects = false

            var redirect = false
            try {
                connect()
                val status = responseCode

                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                    )
                        redirect = true;
                }
                if (redirect) {
                    println("Redirected")
                    downloadBitmapFromGoFileIO(
                        headerFields.get("Location")!!.first(),
                        onSuccess,
                        onDeleted,
                        onError
                    )
                    return
                }

            } catch (e: Exception) {
                onError(e)
            }
            try {
                if (!redirect) onSuccess(bitmap())

            } catch (e: Exception) {  //das ist nur eine Heuristik: Wenn die Response nicht in ein Bitmap umgewandelt werden kann,
                // dann muss der File wohl inzwischen geloescht worden sein
                onDeleted()
            }
        }
    }

    // ein paar hilfreiche Extension Functions
    private fun HttpsURLConnection.message(): String {
        val reader = BufferedReader(InputStreamReader(this.inputStream, StandardCharsets.UTF_8))
        val message = reader.readText()
        reader.close()

        return message
    }

    private fun HttpsURLConnection.bitmap(): Bitmap {
        val bitmapAsBytes = inputStream.readBytes()
        inputStream.close()
        return BitmapFactory.decodeByteArray(bitmapAsBytes, 0, bitmapAsBytes.size)
    }

    private fun Bitmap.asByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        baos.close()
        return byteArray
    }
}