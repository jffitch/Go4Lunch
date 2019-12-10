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
import kotlinx.android.synthetic.main.settings.*
import kotlinx.android.synthetic.main.workmates.*

class SettingsFragment: Fragment() {
    var pref: SharedPreferences? = null
    var radius = 3000
    var orderBy = Constants.BY_DISTANCE
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = context?.getSharedPreferences(Constants.PREF_LOCATION, 0)
        radius = pref?.getInt("radius", 3000) ?: 3000
        orderBy = pref?.getInt("orderBy", Constants.BY_DISTANCE) ?: Constants.BY_DISTANCE
        searchRadiusET.setText(radius.toString())

        searchRadiusET.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    radius = s.toString().toInt()
                    val editor = pref?.edit()
                    editor?.putInt("radius", radius)
                    editor?.apply()
                }
            }
        })
        listViewOrderRG.check(when (orderBy) {
            Constants.BY_RATING -> R.id.byRating
            Constants.BY_WORKMATES -> R.id.byWorkmates
            Constants.BY_NAME -> R.id.byName
            else -> R.id.byDistance
        })
        listViewOrderRG.setOnCheckedChangeListener { radioGroup, i ->
            orderBy = when(i) {
                R.id.byRating -> Constants.BY_RATING
                R.id.byWorkmates -> Constants.BY_WORKMATES
                R.id.byName -> Constants.BY_NAME
                else -> Constants.BY_DISTANCE
            }
            val editor = pref?.edit()
            editor?.putInt("orderBy", orderBy)
            editor?.apply()
        }
    }
}