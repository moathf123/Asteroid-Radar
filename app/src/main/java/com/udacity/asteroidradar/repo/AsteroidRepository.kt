package com.udacity.asteroidradar.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.DatabasePicture
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    enum class Query() { SAVED, TODAY, WEEK }

    private val _queryType: MutableLiveData<Query> = MutableLiveData(Query.WEEK)
    val queryType: LiveData<Query>
        get() = _queryType

    val asteroid: LiveData<List<Asteroid>> =
        Transformations.switchMap(queryType) {
            when (it) {
                Query.SAVED -> Transformations.map(database.asteroidDao.getAsteroid()) {
                    it.asDomainModel()
                }
                Query.TODAY -> Transformations.map(database.asteroidDao.getByDate(getStartDateFormatted())) {
                    it.asDomainModel()
                }
                Query.WEEK -> Transformations.map(database.asteroidDao.getWeekAsteroids(getStartDateFormatted())) {
                    it.asDomainModel()
                }
                else -> throw IllegalArgumentException("incompatible query type")
            }
        }

    val pictureOfDay: LiveData<PictureOfDay> = Transformations.map(getPicture()) {
        it?.asDomainModel()
    }

    private fun getPicture(): LiveData<DatabasePicture> {
        return database.pictureDao.getPicture()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val resultNetwork = Network.retrofitService.getProperties(
                    getStartDateFormatted()
                ).await()
                val resultParsed = parseAsteroidsJsonResult(JSONObject(resultNetwork))

                val imageofDay = Network.retrofitService.getImageOfTheDay().await()
                if (imageofDay.mediaType == "image") {
                    database.pictureDao.clear()
                    database.pictureDao.insertAll(imageofDay.asDatabaseModel())
                }
                database.asteroidDao.clear()
                database.asteroidDao.insertAll(*resultParsed.asDatabaseModel())
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("Error with network", e.message!!)
                }
                e.printStackTrace()
            }
        }
    }

    fun getStartDateFormatted(): String {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    fun applyFilter(filter: Query) {
        _queryType.value = filter
    }
}