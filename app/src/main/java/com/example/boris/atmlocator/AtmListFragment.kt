package com.example.boris.atmlocator

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_atm_list.*

class AtmListFragment : Fragment() {

    val linearLayoutManager = LinearLayoutManager(context)
    var atmListAdapter: AtmListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_atm_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        val atmViewModel = ViewModelProviders.of(activity!!).get(AtmViewModel::class.java)
        atmViewModel.atmsFinalLiveData.observe(this, Observer { atmListAdapter?.updateAtms(it) })

        atmListAdapter?.onClickCallback?.observe(this, Observer { atm ->
            atmViewModel.onAtmSelected(atm)
        })
    }

    private fun setupRecyclerView() {
        if (atmListAdapter == null) {
            atmListAdapter = AtmListAdapter(resources)
        }
        atm_list_recyclerview.apply {
            layoutManager = linearLayoutManager
            adapter = atmListAdapter
        }
    }

}