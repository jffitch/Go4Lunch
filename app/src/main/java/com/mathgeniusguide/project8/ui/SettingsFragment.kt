package com.mathgeniusguide.project8.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.adapter.WorkmatesAdapter
import com.mathgeniusguide.project8.util.Constants
import com.mathgeniusguide.project8.util.Functions.setNotificationAlarm
import com.mathgeniusguide.project8.util.fixTime
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment: Fragment() {
    var pref: SharedPreferences? = null
    var radius = 3000
    var orderBy = Constants.BY_DISTANCE
    var notificationTime = "12:00:00"
    lateinit var act: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings_fragment, container, false)
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
        // load previously saved SharedPreferences
        // Radius 3000, Order By Distance, and Notification Time 12:00:00 are defaults
        pref = context?.getSharedPreferences(Constants.PREF_LOCATION, 0)
        radius = pref?.getInt("radius", 3000) ?: 3000
        orderBy = pref?.getInt("orderBy", Constants.BY_DISTANCE) ?: Constants.BY_DISTANCE
        notificationTime = pref?.getString("notificationTime", "12:00:00") ?: "12:00:00"
        // display saved SharedPreferences in views
        searchRadiusET.setText(radius.toString())
        notificationTimeET.setText(notificationTime)
        listViewOrderRG.check(when (orderBy) {
            Constants.BY_RATING -> R.id.byRating
            Constants.BY_WORKMATES -> R.id.byWorkmates
            Constants.BY_NAME -> R.id.byName
            else -> R.id.byDistance
        });

        saveButton.setOnClickListener {
            // get entered values for each view
            if (searchRadiusET.text.isNotEmpty()) {
                radius = searchRadiusET.text.toString().toInt()
            }
            orderBy = when(listViewOrderRG.checkedRadioButtonId) {
                R.id.byRating -> Constants.BY_RATING
                R.id.byWorkmates -> Constants.BY_WORKMATES
                R.id.byName -> Constants.BY_NAME
                else -> Constants.BY_DISTANCE
            }
            if (notificationTimeET.text.isNotEmpty()) {
                setNotificationAlarm(false, notificationTime, act.username, context!!)
                notificationTime = notificationTimeET.text.toString().fixTime()
                setNotificationAlarm(true, notificationTime, act.username, context!!)
                act.notificationTime = notificationTime
            }
            // save entered values to SharedPreferences
            val editor = pref?.edit()
            editor?.putInt("orderBy", orderBy)
            editor?.putInt("radius", radius)
            editor?.putString("notificationTime", notificationTime)
            editor?.apply()
        }
    }
}