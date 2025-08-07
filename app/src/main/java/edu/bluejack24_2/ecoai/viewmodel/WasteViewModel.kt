package edu.bluejack24_2.ecoai.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.ecoai.data.WasteRepository
import edu.bluejack24_2.ecoai.model.WasteAnalysisResult
import edu.bluejack24_2.ecoai.model.WasteHistoryItem
import edu.bluejack24_2.ecoai.model.WasteItem
import edu.bluejack24_2.ecoai.utils.GeminiApiService
import kotlinx.coroutines.launch
import org.json.JSONObject

class WasteViewModel(
    private val repository: WasteRepository = WasteRepository()
) : ViewModel() {

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _recentlyUploadedWaste = MutableLiveData<List<WasteHistoryItem>>()
    val recentlyUploadedWaste: LiveData<List<WasteHistoryItem>> get() = _recentlyUploadedWaste

    private val _wasteDatabaseItems = MutableLiveData<List<WasteItem>>()
    val wasteDatabaseItems: LiveData<List<WasteItem>> get() = _wasteDatabaseItems

    fun addWasteItemWithImage(
        context: Context,
        imageBitmap: Bitmap,
        imageUri: Uri,
        uploadedBy: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val resultString = GeminiApiService.classifyImage(imageBitmap)

                val analysis = parseWasteAnalysisResult(resultString)

                if (analysis != null) {
                    repository.addWasteItemWithImage(
                        context = context,
                        name = analysis.name,
                        co2e = analysis.carbon_footprint_data,
                        imageUri = imageUri,
                        uploadedBy = uploadedBy,
                        disposalMethod = analysis.disposal_methods
                    )
                    onSuccess()
                } else {
                    Log.e("WasteViewModel", "Analisa gagal, data null")
                }
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Upload error: ${e.message}", e)
            } finally {
                _isUploading.value = false
            }
        }
    }

    private fun parseWasteAnalysisResult(jsonString: String): WasteAnalysisResult? {
        return try {
            val json = JSONObject(jsonString)
            WasteAnalysisResult(
                name = json.getString("name"),
                item_details = json.getString("item_details"),
                carbon_footprint_data = json.getInt("carbon_footprint_data"),
                disposal_methods = json.getString("disposal_methods")
            )
        } catch (e: Exception) {
            Log.e("WasteViewModel", "Parsing JSON gagal: ${e.message}", e)
            null
        }
    }

    fun addWasteItemFromDatabase(
        name: String,
        co2e: Int,
        imageUrl: String,
        uploadedBy: String,
        disposalMethod: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addWasteItemFromDatabase(
                    name = name,
                    co2e = co2e,
                    imageUrl = imageUrl,
                    uploadedBy = uploadedBy,
                    disposalMethod = disposalMethod
                )
                onSuccess()
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Add from database error: ${e.message}", e)
            }
        }
    }

    fun fetchHistory(userId: String) {
        viewModelScope.launch {
            try {
                val items = repository.getWasteHistoryItems().filter { it.uploadedBy == userId }
                _recentlyUploadedWaste.postValue(items)
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Fetch history error: ${e.message}", e)
            }
        }
    }

    fun fetchWasteDatabaseItems() {
        viewModelScope.launch {
            try {
                val items = repository.getWasteDatabaseItems()
                _wasteDatabaseItems.postValue(items)
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Fetch waste database error: ${e.message}", e)
            }
        }
    }

    fun searchWasteDatabaseItems(query: String) {
        viewModelScope.launch {
            try {
                val items = repository.searchWasteDatabaseItems(query)
                _wasteDatabaseItems.postValue(items)
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Search waste database error: ${e.message}", e)
            }
        }
    }

}
