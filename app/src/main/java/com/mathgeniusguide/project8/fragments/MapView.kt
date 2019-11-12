package com.mathgeniusguide.project8.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import kotlinx.android.synthetic.main.map_view.*

class MapView: Fragment(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    var latitude = 91.0
    var longitude = 181.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.map_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (map != null) {
            map.onCreate(null)
            map.onResume()
            map.getMapAsync(this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).latitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && latitude == 91.0 && it != null) {
                latitude = it
                setLocation(latitude, longitude)
            }
        })
        (activity as MainActivity).longitude.observe(viewLifecycleOwner, Observer {
            if (googleMap != null && longitude == 181.0 && it != null) {
                longitude = it
                setLocation(latitude, longitude)
            }
        })
        (activity as MainActivity).placeList.observe(viewLifecycleOwner, Observer {
            var coord: LatLng? = null
            var title = ""
            var pos: CameraPosition? = null
            for (place in it) {
                coord = LatLng(place.latitude, place.longitude)
                title = place.name
                googleMap!!.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.your_lunch))
                    .position(coord).title(title))
            }
        })
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = p0
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