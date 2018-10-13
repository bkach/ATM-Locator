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

/**
 * Fragment containing the list of ATMs.
 *
 * Controls the refresh animation and the Recycler View, along with subscribing to the View Model
 */
class AtmListFragment : Fragment() {

    private val linearLayoutManager = LinearLayoutManager(context)
    private var atmListAdapter: AtmListAdapter? = null
    private lateinit var atmViewModel: AtmViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_atm_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAtmViewModel()
        setupListItemClickListener()
        observeLoadingState()
        disableManualRefresh()
    }

    private fun disableManualRefresh() {
        (view as SwipeRefreshLayout).isEnabled = false
    }

    private fun observeLoadingState() {
        atmViewModel.isLoading.observe(this, Observer { isLoading ->
            (view as SwipeRefreshLayout).isRefreshing = isLoading!!
        })
    }

    private fun setAtmViewModel() {
        atmViewModel = ViewModelProviders.of(activity!!).get(AtmViewModel::class.java)
        setupRecyclerView()
        atmViewModel.atmsFinalLiveData.observe(this, Observer { atmListAdapter?.updateAtms(it) })
    }

    private fun setupListItemClickListener() {
        atmListAdapter?.onClickCallback?.observe(this, Observer { atm ->
            atmViewModel.onAtmSelected(atm)
        })
    }


    private fun setupRecyclerView() {
        if (atmListAdapter == null) {
            atmListAdapter = AtmListAdapter(resources, atmViewModel)
        }
        atm_list_recyclerview.apply {
            layoutManager = linearLayoutManager
            adapter = atmListAdapter
        }
    }

}