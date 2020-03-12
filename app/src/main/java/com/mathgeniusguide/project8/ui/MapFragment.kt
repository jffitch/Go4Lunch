package com.mathgeniusguide.project8.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.RestaurantItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.map_fragment.*
import java.util.*

class MapFragment: Fragment(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    // set latitude and longitude to impossible values as placeholders
    var latitude = 91.0
    var longitude = 181.0
    lateinit var act: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // declare activity shorthand variable
        act = activity as MainActivity
        // toolbar visible and back arrow as drawer icon
        act.toolbar.visibility = View.VISIBLE
        act.toolbar.setNavigationIcon(R.drawable.drawer)
        // hide autocomplete until search button clicked
        act.autocomplete.visibility = View.GONE
        act.autocompleteRV.visibility = View.GONE
        // set up Google map
        map?.onCreate(null)
        map?.onResume()
        map?.getMapAsync(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        act.latitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && latitude == 91.0 && it != null) {
                latitude = it
                // will only run if latitude and longitude are both set
                setLocation(latitude, longitude)
                // create markers for each restaurant in list
                if (act.placeList.value != null) {
                    getMarkers(act.placeList.value!!)
                }
            }
        })
        act.longitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && longitude == 181.0 && it != null) {
                longitude = it
                // will only run if latitude and longitude are both set
                setLocation(latitude, longitude)
                // create markers for each restaurant in list
                if (act.placeList.value != null) {
                    getMarkers(act.placeList.value!!)
                }
            }
        })
        // create markers in case restaurant list loads after location is retrieved
        act.placeList.observe(viewLifecycleOwner, Observer {
            if (googleMap != null) {
                getMarkers(it)
            }
        })

        // if restaurant list is loading, display loading screen
        act.viewModel.isDataLoading.observe(viewLifecycleOwner, Observer {
            progressScreen.visibility = if(it) View.VISIBLE else View.GONE
        })

        // if restaurant list is loading, display loading progress
        act.viewModel.detailsProgress.observe(viewLifecycleOwner, Observer {
            progressCounter.text = "${it}/${act.viewModel.detailsCount.value}"
        })

        // filter markers when searched
        act.autocompleteText.observe(viewLifecycleOwner, Observer {
            // if search string empty, show all
            // if search string not empty, show only those with matching names
            for (i in act.markerList) {
                if (i != null) {
                    i.isVisible = it.isEmpty() || i.title.toLowerCase(Locale.getDefault()).contains(it.toLowerCase(Locale.getDefault()))
                }
            }
        })
    }

    // create markers for Google map
    fun getMarkers(list: List<RestaurantItem>) {
        var coord: LatLng? = null
        var title = ""
        var pos: CameraPosition? = null
        var marker: Marker?
        for (place in list) {
            coord = LatLng(place.latitude, place.longitude)
            title = place.name
            marker = googleMap?.addMarker(MarkerOptions().icon(bitmapDescriptorFromVector(context!!, R.drawable.your_lunch))
                .position(coord).title(title))
            act.markerList.add(marker)
        }
    }

    // create icon for Google map markers
    private fun  bitmapDescriptorFromVector(context: Context, vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas =  Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // run when Google map is ready
    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = p0
        // when marker is clicked, go to restaurant page for that restaurant
        googleMap?.setOnInfoWindowClickListener {marker->
            val placeList = act.placeList.value
            // to determine which restaurant corresponds to each marker, find restaurant with latitude and longitude that match the marker
            if (placeList != null && placeList.any{it.latitude == marker.position.latitude && it.longitude == marker.position.longitude}) {
                act.chosenPlace = placeList.first{it.latitude == marker.position.latitude && it.longitude == marker.position.longitude}
                findNavController().navigate(R.id.load_page_from_map)
            }
        }
    }

    // if location sensor has retrieved both latitude and longitude values, go to location on map
    fun setLocation(lat: Double, lng: Double) {
        if (lat != 91.0 && lng != 181.0) {
            val coord = LatLng(lat, lng)
            googleMap?.apply {
                this.mapType = GoogleMap.MAP_TYPE_NORMAL
                this.addMarker(MarkerOptions().position(coord).title(resources.getString(R.string.you_are_here)))
                val pos =
                    CameraPosition.builder().target(coord).zoom(16.toFloat()).bearing(0.toFloat())
                        .tilt(45.toFloat()).build()
                this.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
            }
        }
    }
}