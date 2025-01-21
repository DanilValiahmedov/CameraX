package com.example.camerax.recycleview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.camerax.R

class AdapterPhoto(private val context: Context,
                   private val dataList: List<RecyclerMedia>): RecyclerView.Adapter<AdapterPhoto.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = dataList[position]
        Glide.with(context).load(item.image1).into(holder.image1)
        Glide.with(context).load(item.image2).into(holder.image2)
        Glide.with(context).load(item.image3).into(holder.image3)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image1: ImageView = itemView.findViewById(R.id.image1)
        val image2: ImageView = itemView.findViewById(R.id.image2)
        val image3: ImageView = itemView.findViewById(R.id.image3)
    }
}