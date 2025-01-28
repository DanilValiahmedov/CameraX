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
    @Query("DELETE FROM InformMedias WHERE id = :mediaId")
    suspend fun deleteById(mediaId: Int)
    @Query("SELECT * FROM InformMedias WHERE mediaType = :type")
    fun getMediaByType(type: MediaType): List<InformMedia>
}