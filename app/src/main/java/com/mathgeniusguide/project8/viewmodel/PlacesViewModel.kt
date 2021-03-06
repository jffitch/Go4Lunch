package com.mathgeniusguide.project8.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mathgeniusguide.go4lunch.database.RestaurantDao
import com.mathgeniusguide.go4lunch.database.RestaurantDatabase
import com.mathgeniusguide.go4lunch.database.RestaurantRoomdbItem
import com.mathgeniusguide.project8.api.Api
import com.mathgeniusguide.project8.connectivity.ConnectivityInterceptor
import com.mathgeniusguide.project8.connectivity.NoConnectivityException
import com.mathgeniusguide.project8.database.CoworkerDao
import com.mathgeniusguide.project8.database.CoworkerRoomdbItem
import com.mathgeniusguide.project8.responses.autocomplete.AutocompleteResponse
import com.mathgeniusguide.project8.util.toRestaurantRoomdbItem
import com.mathgeniusguide.project8.responses.details.DetailsResponse
import com.mathgeniusguide.project8.util.Constants
import com.mathgeniusguide.project8.util.Functions.restaurantItemDetails
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    // declare MutableLiveData variables for use in this class
    private val placeList = mutableListOf<String>()
    private val _autocompleteList: MutableLiveData<AutocompleteResponse?>? = MutableLiveData()
    private val _detailsCount = MutableLiveData<Int>()
    private val _detailsProgress = MutableLiveData<Int>()
    private val _oneDetail: MutableLiveData<DetailsResponse?>? = MutableLiveData()
    private val _isDataLoading = MutableLiveData<Boolean>()
    private val _isAutocompleteDataLoading = MutableLiveData<Boolean>()
    private val _isDataLoadingError = MutableLiveData<Boolean>()

    // Room Database
    private val _savedRestaurants = MutableLiveData<List<RestaurantRoomdbItem>>()
    private val _savedCoworkers = MutableLiveData<List<CoworkerRoomdbItem>>()
    private var db: RestaurantDatabase? = null
    private var restaurantDao: RestaurantDao? = null
    private var coworkerDao: CoworkerDao? = null

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
    val autocompleteList: LiveData<AutocompleteResponse?>?
        get() = _autocompleteList
    val isDataLoadingError: LiveData<Boolean>
        get() = _isDataLoadingError
    val savedRestaurants: LiveData<List<RestaurantRoomdbItem>>
        get() = _savedRestaurants
    val savedCoworkers: LiveData<List<CoworkerRoomdbItem>>
        get() = _savedCoworkers

    // fetch nearby places
    fun fetchPlaces(latitude: Double, longitude: Double, radius: Int, recentIds: List<String>, expiredIds: List<String>, context: Context) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.postValue(true)
        viewModelScope.launch {
            try {
                // load first page of results
                var loadedPlaces = Api.invoke(connectivityInterceptor)
                    .getPlaces("${latitude},${longitude}", radius, "restaurant").body()
                placeList.addAll(loadedPlaces!!.results.map { it.place_id })
                // if next page exists, load next page until next page no longer exists
                var token: String? = loadedPlaces.next_page_token
                while (token != null) {
                    loadedPlaces = Api.invoke(connectivityInterceptor).getNextPage(token).body()
                    placeList.addAll(loadedPlaces!!.results.map { it.place_id })
                    token = loadedPlaces.next_page_token
                }
                // fetch details for each place ID from the API and for each expired saved ID, but not for unexpired saved IDs
                fetchDetails(
                    (placeList + expiredIds).distinct().filter { !recentIds.contains(it) },
                    latitude,
                    longitude
                )
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
                // set count to number of places to show progress
                // initialize progress at 0
                _detailsCount.postValue(placeId.size)
                _detailsProgress.postValue(0)
                for (i in placeId) {
                    val detailsLoaded =
                        Api.invoke(connectivityInterceptor).getDetails(i, Constants.FIELDS).body()
                    if (detailsLoaded != null) {
                        // for each place ID, create a Restaurant Item and add it to the database
                        val nearbyPlace = restaurantItemDetails(
                            detailsLoaded.result,
                            latitude,
                            longitude,
                            getApplication<Application>().resources
                        )
                        insertRestaurantItem(
                            nearbyPlace.toRestaurantRoomdbItem(getApplication<Application>().resources),
                            getApplication<Application>().applicationContext
                        )
                    }
                    // increment loading progress by 1 after loading details for each place ID
                    _detailsProgress.postValue(_detailsProgress.value!! + 1)
                }
                // after adding each item to database, fetch all data from database
                fetchSavedRestaurants(getApplication<Application>().applicationContext)
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    // fetch autocomplete list
    fun fetchAutocompleteList(input: String, latitude: Double, longitude: Double, radius: Int) {
        val connectivityInterceptor = ConnectivityInterceptor(getApplication())
        _isDataLoading.value = true
        viewModelScope.launch {
            try {
                _autocompleteList?.postValue(Api.invoke(connectivityInterceptor).getAutocompleteList("${latitude},${longitude}", radius, input).body())
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(false)
            } catch (e: NoConnectivityException) {
                _isDataLoading.postValue(false)
                _isDataLoadingError.postValue(true)
            }
        }
    }

    fun fetchOneDetail(placeId: String) {
        // for fetching details for a single place ID
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

    fun insertRestaurantItemIfNotExists(restaurantItem: RestaurantRoomdbItem, context: Context) {
        // run function from DAO
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            restaurantDao = db?.restaurantDao()
            Observable.fromCallable({
                restaurantDao?.insertRestaurantItemIfNotExists(restaurantItem)
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    fun insertRestaurantItem(restaurantItem: RestaurantRoomdbItem, context: Context) {
        // run function from DAO
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            restaurantDao = db?.restaurantDao()
            Observable.fromCallable({
                restaurantDao?.insertRestaurantItem(restaurantItem)
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    fun fetchSavedRestaurants(context: Context) {
        // fetch all data from database
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            restaurantDao = db?.restaurantDao()
            restaurantDao?.selectAll()?.observeForever({
                _savedRestaurants.postValue(it)
            })
        }
    }

    fun fetchSavedCoworkers(context: Context) {
        // fetch all data from database
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            coworkerDao = db?.coworkerDao()
            coworkerDao?.loadCoworkers()?.observeForever({
                _savedCoworkers.postValue(it)
            })
        }
    }

    fun insertCoworkerItem(coworkerItem: CoworkerRoomdbItem, context: Context) {
        // run function from DAO
        viewModelScope.launch {
            db = RestaurantDatabase.getDataBase(context)
            coworkerDao = db?.coworkerDao()
            Observable.fromCallable({
                coworkerDao?.insertCoworkerItem(coworkerItem)
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }
}