package com.mathgeniusguide.project8.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.mathgeniusguide.go4lunch.database.RestaurantDao
import com.mathgeniusguide.go4lunch.database.RestaurantDatabase
import com.mathgeniusguide.go4lunch.database.RestaurantItem
import com.mathgeniusguide.project8.api.Api
import com.mathgeniusguide.project8.connectivity.ConnectivityInterceptor
import com.mathgeniusguide.project8.connectivity.NoConnectivityException
import com.mathgeniusguide.project8.connectivity.toRestaurantItem
import com.mathgeniusguide.project8.responses.details.DetailsResponse
import com.mathgeniusguide.project8.responses.place.PlaceResponse
import com.mathgeniusguide.project8.util.Constants
import com.mathgeniusguide.project8.util.Functions.nearbyPlaceDetails
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    // declare MutableLiveData variables for use in this class
    private val placeList = mutableListOf<String>()
    private val _detailsCount = MutableLiveData<Int>()
    private val _detailsProgress = MutableLiveData<Int>()
    private val _oneDetail: MutableLiveData<DetailsResponse?>? = MutableLiveData()
    private val _isDataLoading = MutableLiveData<Boolean>()
    private val _isAutocompleteDataLoading = MutableLiveData<Boolean>()
    private val _isDataLoadingError = MutableLiveData<Boolean>()

    // Room Database
    private val _savedRestaurants = MutableLiveData<List<RestaurantItem>>()
    private var savedIds = emptyList<String>()
    private var db: RestaurantDatabase? = null
    private var dao: RestaurantDao? = null

    // declare LiveData variables for observing in other classes
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
    val savedRestaurants: LiveData<List<RestaurantItem>>
        get() = _savedRestaurants

    // fetch nearby places
    fun fetchPlaces(latitude: Double, longitude: Double, radius: Int, context: Context) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.postValue(true)
        viewModelScope.launch {
            try {
                var loadedPlaces = Api.invoke(connectivityInterceptor)
                    .getPlaces("${latitude},${longitude}", radius, "restaurant").body()
                placeList.addAll(loadedPlaces!!.results.map { it.place_id })
                var token: String? = loadedPlaces.next_page_token
                while (token != null) {
                    loadedPlaces = Api.invoke(connectivityInterceptor).getNextPage(token).body()
                    placeList.addAll(loadedPlaces!!.results.map { it.place_id })
                    token = loadedPlaces.next_page_token
                }
                db = RestaurantDatabase.getDataBase(context)
                dao = db?.restaurantDao()
                dao!!.selectIds().observeForever( {
                    if (it != null) {
                        savedIds = it
                        fetchDetails(
                            placeList.filter { !savedIds.contains(it) },
                            latitude,
                            longitude
                        )
                    }
                    _isDataLoadingError.postValue(false)
                })
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    // fetch details from place id
    fun fetchDetails(placeId: List<String>, latitude: Double, longitude: Double) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.postValue(true)
        viewModelScope.launch {
            try {
                _detailsCount.postValue(placeId.size)
                _detailsProgress.postValue(0)
                for (i in placeId) {
                    val detailsLoaded =
                        Api.invoke(connectivityInterceptor).getDetails(i, Constants.FIELDS).body()
                    if (detailsLoaded != null) {
                        val nearbyPlace = nearbyPlaceDetails(
                            detailsLoaded.result,
                            latitude,
                            longitude,
                            getApplication<Application>().resources
                        )
                        insertRestaurantItemIfNotExists(
                            nearbyPlace.toRestaurantItem(),
                            getApplication<Application>().applicationContext
                        )
                    }
                    _detailsProgress.postValue(_detailsProgress.value!! + 1)
                }
                fetchSavedRestaurants(getApplication<Application>().applicationContext)
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    fun fetchOneDetail(placeId: String) {
        _isAutocompleteDataLoading.postValue(true)
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        viewModelScope.launch {
            try {
                _oneDetail?.postValue(
                    Api.invoke(connectivityInterceptor).getDetails(
                        placeId,
                        Constants.FIELDS
                    ).body()
                )
                _isAutocompleteDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isAutocompleteDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    fun insertRestaurantItemIfNotExists(restaurantItem: RestaurantItem, context: Context) {
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            dao = db?.restaurantDao()
            Observable.fromCallable({
                dao?.insertRestaurantItemIfNotExists(restaurantItem)
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    fun fetchSavedRestaurants(context: Context) {
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            dao = db?.restaurantDao()
            dao?.selectAll()?.observeForever({
                _savedRestaurants.postValue(it)
            })
        }
    }
}