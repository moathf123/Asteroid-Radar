package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repo.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : ViewModel() {

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidRepository(database)

    init {
        refreshAsteroid()
    }

    val asteroid = asteroidsRepository.asteroid
    val picOfDay = asteroidsRepository.pictureOfDay

    private fun refreshAsteroid() {
        viewModelScope.launch {
            asteroidsRepository.refreshAsteroids()
        }
    }

    fun onFilterSelect(filter: AsteroidRepository.Query) {
        asteroidsRepository.applyFilter(filter)
    }

}