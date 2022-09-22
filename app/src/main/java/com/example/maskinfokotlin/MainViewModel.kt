package com.example.maskinfokotlin

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.maskinfokotlin.model.Store
import com.example.maskinfokotlin.repository.MaskService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val service: MaskService,
    private val locationProvider: FusedLocationProviderClient,
    @Assisted private val savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {
    val itemLiveData = MutableLiveData<List<Store>>()
    val loadingLiveData = MutableLiveData<Boolean>()

    @SuppressLint("MissingPermission")
    fun fetchStoreInfo() {
        // 로딩 시작
        loadingLiveData.value = true

        locationProvider.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    startLocationUpdates(locationProvider)
                } else { //location?.let {
                    viewModelScope.launch {
                        val storeInfo = service.fetchStoreInfo(location.latitude, location.longitude)
                        itemLiveData.value = storeInfo.stores.filter { store ->
                            store.remain_stat != null
                        }

                        // 로딩 끝
                        loadingLiveData.value = false
                    }
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationProvider: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.create()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    viewModelScope.launch {
                        val storeInfo = service.fetchStoreInfo(location.latitude, location.longitude)
                        itemLiveData.value = storeInfo.stores.filter { store ->
                            store.remain_stat != null
                        }

                        // 로딩 끝
                        loadingLiveData.value = false
                    }
                }
            }
        }

        // 위치 업데이트 요청
        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

}