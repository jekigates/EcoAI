package edu.bluejack24_2.ecoai.utils

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend.Companion.googleAI
import com.google.firebase.ai.type.content
import java.io.ByteArrayOutputStream

object GeminiApiService {
    private const val MODEL_NAME = "gemini-2.5-flash"

    private val generativeModel: GenerativeModel by lazy {
        Firebase.ai(backend = googleAI())
            .generativeModel(
                modelName = MODEL_NAME,
                generationConfig = com.google.firebase.ai.type.GenerationConfig.Builder()
                    .setResponseMimeType("application/json")
                    .build()
            )
    }

    suspend fun classifyImage(imageBitmap: Bitmap): String {
        return try {
            val prompt = """
            Analyze the waste item in this image. Provide the following information in a **valid JSON** format:
            {
              "name": "Only the name of the waste item (short and clear, e.g., 'Plastic Bottle', 'Aluminum Can')",
              "item_details": "Name of the waste item and its material composition",
              "carbon_footprint_data": "Only return the estimated carbon footprint in g CO2e as an integer (e.g., 25, 100, 250)",
              "disposal_methods": "Recommended disposal methods (e.g., 'Recycle with plastics', 'Compost', 'Landfill', 'Special hazardous waste disposal')."
            }
            Do NOT include extra text or explanation. Only return the JSON object.
        """.trimIndent()

            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val imageBytes = outputStream.toByteArray()

            val content = content {
                inlineData(imageBytes, "image/jpeg")
                text(prompt)
            }

            val response = generativeModel.generateContent(content)
            response.text ?: "No response text found."
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error classifying image with Gemini API: ${e.message}", e)
            "Error: ${e.localizedMessage}"
        }
    }

}
