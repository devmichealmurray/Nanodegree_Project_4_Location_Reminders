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
import com.udacity.project4.ui.RemindersActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

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
        viewModel.apply {
            saveCompleted.observe(viewLifecycleOwner, saveCompletedObserver)
            sendGeoFenceRequest.observe(viewLifecycleOwner, sendGeoFenceRequestObserver)
        }
    }

    private val saveCompletedObserver = Observer<Boolean> { isSaveCompleted ->
        if (isSaveCompleted) {
            viewModel.onClear()
            findNavController().navigate(R.id.action_saveReminderFragment_to_reminderListFragment)
        }
    }

    fun navigateToSelectLocation() {
        findNavController().navigate(R.id.action_saveReminderFragment_to_selectLocationFragment)
    }

    private val sendGeoFenceRequestObserver = Observer<GeofencingRequest> { geofenceRequest ->
        (activity as RemindersActivity).addGeoFences(geofenceRequest)
        viewModel.clearGeofenceRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        viewModel.onClear()
    }
}
