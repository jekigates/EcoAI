package edu.bluejack24_2.ecoai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack24_2.ecoai.data.WasteRepository
import edu.bluejack24_2.ecoai.model.WasteHistoryItem
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


    fun fetchCarbonTrack(userId: String) {
        viewModelScope.launch {
            val track = repository.calculateUserCarbonTrack(userId)
            _carbonTrack.postValue(track)
        }
    }

    fun fetchWeeklyStreak(userId: String) {
        viewModelScope.launch {
            val streak = repository.calculateUserWeeklyStreak(userId)
            _weeklyStreak.postValue(streak)
        }
    }

    suspend fun getWasteItemById(id: String): WasteHistoryItem? {
        return repository.getWasteHistoryItemById(id)
    }


}
