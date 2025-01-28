package com.example.camerax.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "InformMedias")
data class InformMedia(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "uri")
    var uri: String,
    @ColumnInfo(name = "mediaType")
    var mediaType: MediaType,
)