package com.mathgeniusguide.project8.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mathgeniusguide.project8.api.Api
import com.mathgeniusguide.project8.connectivity.ConnectivityInterceptor
import com.mathgeniusguide.project8.connectivity.NoConnectivityException
import com.mathgeniusguide.project8.responses.details.DetailsResponse
import com.mathgeniusguide.project8.responses.place.PlaceResponse
import kotlinx.coroutines.launch

class MyViewModel(application: Application) : AndroidViewModel(application) {
    // declare MutableLiveData variables for use in this class
    private val _places: MutableLiveData<PlaceResponse?>? = MutableLiveData()
    private val _details: MutableLiveData<DetailsResponse?>? = MutableLiveData()
    private val _isDataLoading = MutableLiveData<Boolean>()
    private val _isDataLoadingError = MutableLiveData<Boolean>()

    // declare LiveData variables for observing in other classes
    val places: LiveData<PlaceResponse?>?
        get() = _places
    val details: LiveData<DetailsResponse?>?
        get() = _details
    val isDataLoading: LiveData<Boolean>
        get() = _isDataLoading
    val isDataLoadingError: LiveData<Boolean>
        get() = _isDataLoadingError

    // fetch nearby places
    fun fetchPlaces(latitude: Double, longitude: Double) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.value = true
        viewModelScope.launch {
            try {
                _places?.postValue(Api.invoke(connectivityInterceptor).getPlaces("${latitude},${longitude}").body())
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    // fetch details from place id
    fun fetchDetails(id: String) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.value = true
        viewModelScope.launch {
            try {
                _details?.postValue(Api.invoke(connectivityInterceptor).getDetails(id).body())
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }
}