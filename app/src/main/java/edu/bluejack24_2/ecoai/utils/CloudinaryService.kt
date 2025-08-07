package edu.bluejack24_2.ecoai.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import edu.bluejack24_2.ecoai.BuildConfig

class CloudinaryService {
    companion object {
        private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
        private val UPLOAD_PRESET = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        private val API_KEY = BuildConfig.CLOUDINARY_API_KEY
        private val API_SECRET = BuildConfig.CLOUDINARY_API_SECRET

        private val BASE_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME"
        private const val TAG = "CloudinaryService"
    }

    private val client = OkHttpClient()

    suspend fun uploadMedia(context: Context, uri: Uri, isVideo: Boolean = false): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(context, uri)
                val mediaType = if (isVideo) "video/*" else "image/*"
                val endpoint = if (isVideo) "video/upload" else "image/upload"
                val timestamp = (System.currentTimeMillis() / 1000).toString()

                val params = "timestamp=$timestamp&upload_preset=$UPLOAD_PRESET"
                val signature = generateSignature(params)
                val apiKey = API_KEY

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.name, file.asRequestBody(mediaType.toMediaType()))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("timestamp", timestamp)
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("api_key", apiKey)
                    .apply {
                        if (isVideo) {
                            addFormDataPart("resource_type", "video")
                        }
                    }
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/$endpoint")
                    .post(requestBody)
                    .build()

                Log.d(TAG, "Request URL: $BASE_URL/$endpoint")
                Log.d(TAG, "Request Body: $requestBody")

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d(TAG, "Response: $responseBody")

                if (response.isSuccessful) {
                    val url = parseCloudinaryResponse(responseBody)
                    Result.success(url)
                } else {
                    Result.failure(Exception("Upload failed: ${response.code} - $responseBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during upload: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun uploadProfileImage(context: Context, uri: Uri): Result<String> {
        return uploadMedia(context, uri, false)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val extension = when {
            uri.toString().contains("video") -> ".mp4"
            else -> ".jpg"
        }
        val tempFile = File.createTempFile("upload", extension, context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun parseCloudinaryResponse(response: String?): String {
        return try {
            response?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("secure_url")
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response: ${e.message}", e)
            ""
        }
    }

    private fun generateSignature(params: String): String {
        val message = "$params$API_SECRET"
        val digest = MessageDigest.getInstance("SHA-1").digest(message.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}