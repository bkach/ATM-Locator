package com.example.boris.atmlocator

import android.arch.lifecycle.MutableLiveData
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AtmListAdapter(val resources: Resources) : RecyclerView.Adapter<AtmListAdapter.ViewHolder>() {

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
        if (holder.atm.distance != null) {
            holder.atmDistanceTextView.text = resources.getQuantityString(
                    R.plurals.numberOfMeters, holder.atm.distance!!.toInt(), holder.atm.distance!!.toInt())
        }

        // Callback for RecyclerView item click
        holder.clickListener(View.OnClickListener {
            onClickCallback.value = holder.atm
        })
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

        fun clickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }
    }
}