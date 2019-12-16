package com.mathgeniusguide.project8.ui

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
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_view.*

class ListFragment: Fragment() {
    lateinit var act: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        act = activity as MainActivity
        act.toolbar.visibility = View.VISIBLE
        act.autocompleteFragment.view?.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (act.placeList.value != null) {
            val placeList = act.placeList.value!!
            for (i in placeList) {
                i.workmates = act.chosenRestaurantList.filter { it.restaurant == i.id }.size
            }
            listViewRV.layoutManager = LinearLayoutManager(context)
            val pref = context?.getSharedPreferences(Constants.PREF_LOCATION, 0)
            val orderBy = pref?.getInt("orderBy", Constants.BY_DISTANCE) ?: Constants.BY_DISTANCE

            val sortedList = when (orderBy) {
                Constants.BY_RATING -> placeList.sortedByDescending { it.rating }
                Constants.BY_WORKMATES -> placeList.sortedByDescending { it.workmates }
                Constants.BY_NAME -> placeList.sortedBy { it.name }
                else -> placeList.sortedBy { it.distance }
            }.sortedBy { !act.restaurantsLiked.contains(it.id) }

            listViewRV.adapter = PlaceAdapter(sortedList, context!!, findNavController())
        }
    }
}