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
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.NearbyPlace
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.map_fragment.*

class MapFragment: Fragment(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    var latitude = 91.0
    var longitude = 181.0
    lateinit var act: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        act = activity as MainActivity
        act.toolbar.visibility = View.VISIBLE
        act.toolbar.setNavigationIcon(R.drawable.drawer)
        act.autocompleteFragment.view?.visibility = View.GONE
        if (map != null) {
            map.onCreate(null)
            map.onResume()
            map.getMapAsync(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        act.latitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && latitude == 91.0 && it != null) {
                latitude = it
                setLocation(latitude, longitude)
                if (act.placeList.value != null) {
                    getMarkers(act.placeList.value!!)
                }
            }
        })
        act.longitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && longitude == 181.0 && it != null) {
                longitude = it
                setLocation(latitude, longitude)
                if (act.placeList.value != null) {
                    getMarkers(act.placeList.value!!)
                }
            }
        })
        act.placeList.observe(viewLifecycleOwner, Observer {
            if (googleMap != null) {
                getMarkers(it)
            }
        })
        act.viewModel.isDataLoading.observe(viewLifecycleOwner, Observer {
            progressScreen.visibility = if(it) View.VISIBLE else View.GONE
        })
        act.viewModel.detailsProgress.observe(viewLifecycleOwner, Observer {
            progressCounter.text = "${it}/${act.viewModel.detailsCount.value}"
        })
    }

    fun getMarkers(list: List<NearbyPlace>) {
        var coord: LatLng? = null
        var title = ""
        var pos: CameraPosition? = null
        for (place in list) {
            coord = LatLng(place.latitude, place.longitude)
            title = place.name
            googleMap!!.addMarker(MarkerOptions().icon(bitmapDescriptorFromVector(context!!, R.drawable.your_lunch))
                .position(coord).title(title))
        }
    }

    private fun  bitmapDescriptorFromVector(context: Context, vectorResId:Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        val bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        val canvas =  Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = p0
        googleMap!!.setOnInfoWindowClickListener {marker->
            val placeList = act.placeList.value
            if (placeList != null && placeList.any{it.latitude == marker.position.latitude && it.longitude == marker.position.longitude}) {
                act.chosenPlace = placeList.first{it.latitude == marker.position.latitude && it.longitude == marker.position.longitude}
                findNavController().navigate(R.id.load_page_from_map)
            }
        }
    }

    fun setLocation(lat: Double, lng: Double) {
        if (googleMap != null && lat != 91.0 && lng != 181.0) {
            val coord = LatLng(lat, lng)
            googleMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap!!.addMarker(MarkerOptions().position(coord).title(resources.getString(R.string.you_are_here)))
            val pos = CameraPosition.builder().target(coord).zoom(16.toFloat()).bearing(0.toFloat()).tilt(45.toFloat()).build()
            googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        }
    }
}