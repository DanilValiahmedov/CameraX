package com.example.camerax.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DAO {
    @Insert
    fun insertUri(uri: InformMedia)
    @Query("SELECT * FROM InformMedias")
    fun getAllUri(): List<InformMedia>
    @Query("DELETE FROM InformMedias WHERE id = :photoId")
    suspend fun deleteById(photoId: Int)
}