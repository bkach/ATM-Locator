/*
 * ATMLocator
 * Copyright (C) 2018 Boris Kachscovsky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.boris.atmlocator.atmList

import android.arch.lifecycle.MutableLiveData
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.boris.atmlocator.AtmViewModel
import com.example.boris.atmlocator.R
import com.example.boris.atmlocator.repository.Atm

/**
 * A List Adapter for the ATM List Recycler View.
 *
 * This class is responsible for managing the Recycler View and filling each View Holder
 */
class AtmListAdapter(private val resources: Resources, private val atmViewModel: AtmViewModel)
    : RecyclerView.Adapter<AtmListAdapter.ViewHolder>() {

    var atms: List<Atm>? = listOf()
    val onClickCallback: MutableLiveData<Atm> = MutableLiveData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return atms?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.atm = atms!![position]
        holder.atmNameTextView.text = holder.atm.name
        holder.atmAddressTextView.text = holder.atm.address.formatted
        setDistanceCalculatedFields(holder, position)
        setItemSelectedClickListener(holder)
    }

    private fun setItemSelectedClickListener(holder: ViewHolder) {
        holder.clickListener(View.OnClickListener {
            onClickCallback.value = holder.atm
        })
    }

    private fun setDistanceCalculatedFields(holder: ViewHolder, position: Int) {
        val isFirstItem = position == 0

        if (atmViewModel.distancesCalculated) {
            holder.atmDistanceTextView.text = getDistanceText(holder.atm.distance!!.toInt())

            if (isFirstItem) {
                holder.closestView.visibility = View.VISIBLE
            } else {
                holder.closestView.visibility = View.GONE
            }
        } else {
            holder.closestView.visibility = View.GONE
            holder.atmDistanceTextView.text = ""
        }
    }

    private fun getDistanceText(distance: Int): String {
        return resources.getQuantityString(R.plurals.numberOfMeters, distance, distance)
    }

    fun updateAtms(atms: List<Atm>?) {
        this.atms = atms
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var atm: Atm
        val atmNameTextView: TextView = itemView.findViewById(R.id.view_holder_item_name)
        val atmAddressTextView: TextView = itemView.findViewById(R.id.view_holder_item_address)
        val atmDistanceTextView: TextView = itemView.findViewById(R.id.view_holder_item_distance)
        val closestView: View = itemView.findViewById(R.id.view_holder_closest_view)

        fun clickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }
    }
}