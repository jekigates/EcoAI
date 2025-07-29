package com.bluejack242.ecoai.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluejack242.ecoai.data.WasteRepository
import com.bluejack242.ecoai.model.WasteHistoryItem
import kotlinx.coroutines.launch

class WasteViewModel(
    private val repository: WasteRepository = WasteRepository()
) : ViewModel() {

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> get() = _isUploading

    private val _recentlyUploadedWaste = MutableLiveData<List<WasteHistoryItem>>()
    val recentlyUploadedWaste: LiveData<List<WasteHistoryItem>> get() = _recentlyUploadedWaste

    private val _carbonTrack = MutableLiveData<Int>()
    val carbonTrack: LiveData<Int> get() = _carbonTrack

    private val _weeklyStreak = MutableLiveData<Int>()
    val weeklyStreak: LiveData<Int> get() = _weeklyStreak

//    fun fetchRecentlyUploadedWaste(limit: Int = 5) {
//        viewModelScope.launch {
//            val items = repository.getRecentlyUploadedWaste(limit)
//            _recentlyUploadedWaste.postValue(items)
//        }
//    }

    fun fetchCarbonTrack() {
        viewModelScope.launch {
            val track = repository.getUserCarbonTrack()
            _carbonTrack.postValue(track)
        }
    }

    fun fetchWeeklyStreak() {
        viewModelScope.launch {
            val streak = repository.getUserWeeklyStreak()
            _weeklyStreak.postValue(streak)
        }
    }

    fun addWasteItemWithImage(context: Context, name: String, co2e: Int, imageUri: Uri, uploadedBy: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.addWasteItemWithImage(context, name, co2e, imageUri, uploadedBy)
                onSuccess()
            } catch (e: Exception) {
                Log.e("WasteViewModel", "Upload error: ${e.message}", e)
            } finally {
                _isUploading.value = false
            }
        }
    }

}
