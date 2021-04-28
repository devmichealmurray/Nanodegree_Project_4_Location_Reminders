package com.udacity.project4.ui.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.ui.activities.RemindersActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveReminderBinding.inflate(inflater, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        binding.fragment = this
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sendGeoFenceRequest.observe(viewLifecycleOwner, sendGeoFenceRequestObserver)
    }

    fun navigateToSelectLocation() {
        findNavController().navigate(R.id.action_saveReminderFragment_to_selectLocationFragment)
    }

    private val sendGeoFenceRequestObserver = Observer<GeofencingRequest> { geofenceRequest ->
        if (geofenceRequest != null) {
            (activity as RemindersActivity).addGeoFences(geofenceRequest)
            viewModel.clearGeofenceRequest()
            viewModel.onClear()
            findNavController().navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
        }
    }
}
