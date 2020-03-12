package com.mathgeniusguide.project8.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.PlaceAdapter
import com.mathgeniusguide.project8.database.RestaurantItem
import com.mathgeniusguide.project8.util.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_fragment.*
import java.util.*

class ListFragment : Fragment() {
    lateinit var act: MainActivity
    lateinit var sortedList: List<RestaurantItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.list_fragment, container, false)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (act.placeList.value != null) {
            // get already-loaded list of nearby restaurants
            val placeList = act.placeList.value!!
            // calculate number of workmates for each restaurant
            for (i in placeList) {
                i.workmates = act.firebaseCoworkerList.filter { it.restaurant == i.id }.size
            }
            listViewRV.layoutManager = LinearLayoutManager(context)

            // load sort order from SharedPreferences
            val pref = context?.getSharedPreferences(Constants.PREF_LOCATION, 0)
            val orderBy = pref?.getInt("orderBy", Constants.BY_DISTANCE) ?: Constants.BY_DISTANCE

            // sort restaurants by order chosen in settings, with liked restaurants first
            sortedList = when (orderBy) {
                Constants.BY_RATING -> placeList.sortedByDescending { it.rating }
                Constants.BY_WORKMATES -> placeList.sortedByDescending { it.workmates }
                Constants.BY_NAME -> placeList.sortedBy { it.name }
                else -> placeList.sortedBy { it.distance }
            }.sortedBy { !act.restaurantsLiked.contains(it.id) }

            // set up RecyclerView
            listViewRV.adapter = PlaceAdapter(sortedList, context!!, findNavController())
        }

        // filter list when searched
        act.autocompleteText.observe(viewLifecycleOwner, Observer {
            // if search string empty, show all
            // if search string not empty, show only those with matching names
            listViewRV.adapter =
                PlaceAdapter(if (it.isEmpty()) sortedList else sortedList.filter { item ->
                    item.name.toLowerCase(
                        Locale.getDefault()
                    ).contains(
                        it.toLowerCase(
                            Locale.getDefault()
                        )
                    )
                }, context!!, findNavController())
        })
    }
}