package com.mathgeniusguide.project8.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.WorkmatesAdapter
import kotlinx.android.synthetic.main.workmates.*

class WorkmatesFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.workmates, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((activity as MainActivity).chosenRestaurantList != null) {
            workmatesRV.layoutManager = LinearLayoutManager(context)
            workmatesRV.adapter = WorkmatesAdapter((activity as MainActivity).chosenRestaurantList.sortedByDescending{it.restaurant}, context!!, (activity as MainActivity).placeList.value)
        }
    }
}