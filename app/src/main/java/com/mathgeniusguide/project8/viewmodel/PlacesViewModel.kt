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
import com.mathgeniusguide.project8.util.Constants
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    // declare MutableLiveData variables for use in this class
    private val placeList = mutableListOf<String>()
    private val _placeListLD = MutableLiveData<List<String>>()
    private val _details: MutableLiveData<List<DetailsResponse?>>? = MutableLiveData()
    private val _detailsCount = MutableLiveData<Int>()
    private val _detailsProgress = MutableLiveData<Int>()
    private val _oneDetail: MutableLiveData<DetailsResponse?>? = MutableLiveData()
    private val _isDataLoading = MutableLiveData<Boolean>()
    private val _isAutocompleteDataLoading = MutableLiveData<Boolean>()
    private val _isDataLoadingError = MutableLiveData<Boolean>()

    // declare LiveData variables for observing in other classes
    val placeListLD: LiveData<List<String>>
        get() = _placeListLD
    val details: LiveData<List<DetailsResponse?>>?
        get() = _details
    val detailsCount: LiveData<Int>
        get() = _detailsCount
    val detailsProgress: LiveData<Int>
        get() = _detailsProgress
    val oneDetail: LiveData<DetailsResponse?>?
        get() = _oneDetail
    val isDataLoading: LiveData<Boolean>
        get() = _isDataLoading
    val isAutocompleteDataLoading: LiveData<Boolean>
        get() = _isAutocompleteDataLoading
    val isDataLoadingError: LiveData<Boolean>
        get() = _isDataLoadingError

    // fetch nearby places
    fun fetchPlaces(latitude: Double, longitude: Double, radius: Int) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.postValue(true)
        viewModelScope.launch {
            try {
                var loadedPlaces = Api.invoke(connectivityInterceptor).getPlaces("${latitude},${longitude}", radius, "restaurant").body()
                placeList.addAll(loadedPlaces!!.results.map {it.place_id})
                var token: String? = loadedPlaces.next_page_token
                while (token != null) {
                    loadedPlaces = Api.invoke(connectivityInterceptor).getNextPage(token).body()
                    placeList.addAll(loadedPlaces!!.results.map {it.place_id})
                    token = loadedPlaces.next_page_token
                }
                fetchDetails(placeList)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    // fetch details from place id
    fun fetchDetails(placeId: List<String>) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.postValue(true)
        viewModelScope.launch {
            try {
                _detailsCount.postValue(placeId.size)
                _detailsProgress.postValue(0)
                _details?.postValue(placeId.map{detailsMap(connectivityInterceptor, it)})
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    suspend fun detailsMap (connectivityInterceptor: ConnectivityInterceptor, placeIdItem: String) : DetailsResponse? {
        val detailsLoaded = Api.invoke(connectivityInterceptor).getDetails(placeIdItem, Constants.FIELDS).body()
        _detailsProgress.postValue(_detailsProgress.value!! + 1)
        return detailsLoaded
    }

    fun fetchOneDetail(placeId: String) {
        _isAutocompleteDataLoading.postValue(true)
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        viewModelScope.launch {
            try {
               _oneDetail?.postValue(Api.invoke(connectivityInterceptor).getDetails(placeId, Constants.FIELDS).body())
                _isAutocompleteDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isAutocompleteDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }
}