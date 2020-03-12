package com.mathgeniusguide.project8.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mathgeniusguide.project8.MainActivity
import com.mathgeniusguide.project8.R
import com.mathgeniusguide.project8.database.RestaurantItem
import com.mathgeniusguide.project8.responses.autocomplete.AutocompleteItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.autocomplete_item.view.*

class AutocompleteAdapter (private val items: ArrayList<AutocompleteItem>, val context: Context, val placeList: MutableList<RestaurantItem>, val act: MainActivity) : RecyclerView.Adapter<AutocompleteAdapter.ViewHolder> () {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.autocomplete_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = items[position]
        holder.autocompleteDescription.text = i.structured_formatting.secondary_text
        holder.autocompleteName.text = i.structured_formatting.main_text
        holder.parent.setOnClickListener {
            if (placeList.any {place -> place.id == i.place_id}) {
                act.chosenPlace = placeList.first {place -> place.id == i.place_id}
                act.navController.navigate(R.id.load_page_from_map)
            } else {
                act.autocompleteRV.visibility = View.GONE
                act.autocompleteProgressText.text = String.format(context.resources.getString(R.string.loading_info_for), i.structured_formatting.main_text)
                act.viewModel.fetchOneDetail(i.place_id)
            }
        }
    }

    class ViewHolder (view : View) : RecyclerView.ViewHolder(view) {
        val autocompleteDescription = view.autocompleteDescription
        val autocompleteName = view.autocompleteName
        val parent = view
    }
}