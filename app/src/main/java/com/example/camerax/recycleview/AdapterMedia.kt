package com.example.camerax.recycleview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.camerax.R
import com.example.camerax.fragments.ViewingPhotosFragment

class AdapterPhoto(private val context: Context,
                   private val fragmentManager: FragmentManager,
                   private val dataList: List<RecyclerMedia>): RecyclerView.Adapter<AdapterPhoto.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = dataList[position]

        launchingPhoto(item.image1, holder.image1)
        launchingPhoto(item.image2, holder.image2)
        launchingPhoto(item.image3, holder.image3)
    }

    private fun launchingPhoto(itemUri: String, holderImage: ImageView){
        Glide.with(context).load(itemUri).into(holderImage)

        holderImage.setOnClickListener {
            fragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentСontainer,
                    ViewingPhotosFragment.newInstance(itemUri, true)
                )
                .addToBackStack(null)
                .commit()
        }

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