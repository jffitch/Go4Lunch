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
        act = activity as MainActivity
        act.toolbar.visibility = View.VISIBLE
        act.toolbar.setNavigationIcon(R.drawable.drawer)
        act.autocompleteFragment.view?.visibility = View.GONE
        pref = context?.getSharedPreferences(Constants.PREF_LOCATION, 0)
        radius = pref?.getInt("radius", 3000) ?: 3000
        orderBy = pref?.getInt("orderBy", Constants.BY_DISTANCE) ?: Constants.BY_DISTANCE
        notificationTime = pref?.getString("notificationTime", "12:00:00") ?: "12:00:00"
        searchRadiusET.setText(radius.toString())
        notificationTimeET.setText(notificationTime)

        saveButton.setOnClickListener {
            if (searchRadiusET.text.isNotEmpty()) {
                radius = searchRadiusET.text.toString().toInt()
                val editor = pref?.edit()

                editor?.apply()
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
                val editor = pref?.edit()

                editor?.apply()
            }
            val editor = pref?.edit()
            editor?.putInt("orderBy", orderBy)
            editor?.putInt("radius", radius)
            editor?.putString("notificationTime", notificationTime)
            editor?.apply()
        }
    }
}