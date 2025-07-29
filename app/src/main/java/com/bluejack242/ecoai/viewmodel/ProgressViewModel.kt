package com.bluejack242.ecoai.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluejack242.ecoai.data.WasteRepository
import com.bluejack242.ecoai.model.WasteHistoryItem
import kotlinx.coroutines.launch

class ProgressViewModel(
    private val repository: WasteRepository = WasteRepository()
) : ViewModel() {

    private val _recentlyUploadedWaste = MutableLiveData<List<WasteHistoryItem>>()
    val recentlyUploadedWaste: LiveData<List<WasteHistoryItem>> get() = _recentlyUploadedWaste

    private val _carbonTrack = MutableLiveData<Int>()
    val carbonTrack: LiveData<Int> get() = _carbonTrack

    private val _weeklyStreak = MutableLiveData<Int>()
    val weeklyStreak: LiveData<Int> get() = _weeklyStreak

    fun fetchRecentlyUploadedWaste(userId: String, limit: Int = 5) {
        viewModelScope.launch {
            val items = repository.getRecentlyUploadedWaste(userId, limit)
            _recentlyUploadedWaste.postValue(items)
        }
    }


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

    fun addWasteItem(context: Context, name: String, co2e: Int, imageUri: Uri, uploadedBy: String) {
        viewModelScope.launch {
            try {
                repository.addWasteItemWithImage(context, name, co2e, imageUri, uploadedBy)
                fetchRecentlyUploadedWaste(uploadedBy)
                fetchCarbonTrack()
                fetchWeeklyStreak()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
