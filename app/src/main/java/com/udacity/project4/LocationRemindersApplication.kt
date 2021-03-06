package com.udacity.project4

import android.app.Application
import com.udacity.project4.data.database.LocalDB
import com.udacity.project4.data.database.ReminderDataSource
import com.udacity.project4.data.repository.RemindersLocalRepository
import com.udacity.project4.ui.activities.ReminderDescriptionViewModel
import com.udacity.project4.ui.reminderslist.RemindersListViewModel
import com.udacity.project4.ui.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LocationRemindersApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                ReminderDescriptionViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@LocationRemindersApplication) }
        }

        startKoin {
            androidContext(this@LocationRemindersApplication)
            modules(listOf(myModule))
        }
    }
}