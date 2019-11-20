package com.mathgeniusguide.project8.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.PlaceAdapter
import kotlinx.android.synthetic.main.list_view.*

class ListFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.list_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((activity as MainActivity).placeList.value != null) {
            listViewRV.layoutManager = LinearLayoutManager(context)
            listViewRV.adapter = PlaceAdapter((activity as MainActivity).placeList.value!!.sortedBy{it.distance}, context!!, (activity as MainActivity).chosenRestaurantList, findNavController())
        }
    }
}