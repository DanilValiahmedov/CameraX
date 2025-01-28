package com.example.camerax.recycleview

import com.example.camerax.database.MediaType

data class RecyclerMediasThree (
    val media1: RecyclerMediaOne?,
    val media2: RecyclerMediaOne?,
    val media3: RecyclerMediaOne?,
)

data class RecyclerMediaOne (
    val uri: String?,
    val type: MediaType?,
)