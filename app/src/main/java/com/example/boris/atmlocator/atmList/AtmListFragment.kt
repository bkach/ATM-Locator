package com.example.boris.atmlocator.atmList

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.boris.atmlocator.AtmViewModel
import com.example.boris.atmlocator.R
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

        atmViewModel.isLoading.observe(this, Observer { isLoading ->
            (view as SwipeRefreshLayout).isRefreshing = isLoading!!
        })

        (view as SwipeRefreshLayout).isEnabled = false
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