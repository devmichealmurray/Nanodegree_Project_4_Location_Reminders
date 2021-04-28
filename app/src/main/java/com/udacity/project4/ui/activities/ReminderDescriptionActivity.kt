package com.udacity.project4.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.udacity.project4.R
import com.udacity.project4.data.model.local.ReminderDataItem
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.utils.Constants.EXTRA_ReminderDataItem
import org.jetbrains.anko.alert
import org.koin.android.ext.android.inject

class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private val viewModel: ReminderDescriptionViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.addReminderToLiveData(intent.extras)
    }

    override fun onStart() {
        super.onStart()
        viewModel.apply {
            errorMessage.observe(this@ReminderDescriptionActivity, errorMessageObserver)
            navigateToApp.observe(this@ReminderDescriptionActivity, navigateToAppObserver)
        }
    }

    private val errorMessageObserver = Observer<String> { errorMessage ->
        if (!errorMessage.isNullOrEmpty()) {
            alert {
                title = getString(R.string.error)
                message = errorMessage
                isCancelable = false
                positiveButton(getString(R.string.okay)) { dialog ->
                    viewModel.clearError()
                    dialog.dismiss()
                }
            }.show()
        }
    }

    private val navigateToAppObserver = Observer<Boolean> { navigateToApp ->
        if (navigateToApp) {
            startActivity(Intent(this, RemindersActivity::class.java))
        }
    }

    companion object {
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }
}
