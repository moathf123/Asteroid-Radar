package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM asteroid_details_table ORDER BY date(closeApproachDate) ASC")
    fun getAsteroid(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * from asteroid_details_table where closeApproachDate >= :date ORDER BY date(closeApproachDate) ASC")
    fun getWeekAsteroids(date: String): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * from asteroid_details_table where closeApproachDate == :date")
    fun getByDate(date: String): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroid: DatabaseAsteroid)

    @Query("DELETE FROM asteroid_details_table")
    fun clear()

}

@Dao
interface PictureDao {
    @Query("SELECT * FROM image_details_table")
    fun getPicture(): LiveData<DatabasePicture>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg picture: DatabasePicture)

    @Query("DELETE FROM image_details_table")
    fun clear()
}

@Database(entities = [DatabaseAsteroid::class,DatabasePicture::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao

    abstract val pictureDao: PictureDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "Asteroids_database"
            ).build()
        }
    }
    return INSTANCE
}