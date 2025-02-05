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
import com.example.camerax.database.MediaType
import com.example.camerax.fragments.ViewingPhotosFragment
import com.example.camerax.fragments.ViewingVideosFragment

class AdapterPhoto(private val context: Context,
                   private val fragmentManager: FragmentManager,
                   private var dataList: List<RecyclerMediasThree>): RecyclerView.Adapter<AdapterPhoto.MediaViewHolder>() {
    fun updateList(newList: List<RecyclerMediasThree>) {
        dataList = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = dataList[position]

        launching(item.media1?.uri, item.media1?.type, holder.image1, holder.playIcon1)
        launching(item.media2?.uri, item.media2?.type, holder.image2, holder.playIcon2)
        launching(item.media3?.uri, item.media3?.type, holder.image3, holder.playIcon3)
    }

    private fun launching(
        itemUri: String?, itemType: MediaType?, holderImage: ImageView, holderPlayIcon: ImageView
    ){
        //Очистка элемента при переключении с одного типа файлов на другой
        holderImage.setImageDrawable(null)
        holderPlayIcon.visibility = View.GONE

        if(itemUri != null && itemType != null) {
            Glide.with(context).load(itemUri).into(holderImage)
            if(itemType == MediaType.VIDEO)holderPlayIcon.visibility = View.VISIBLE

            val fragment = when(itemType) {
                MediaType.PHOTO -> ViewingPhotosFragment.newInstance(itemUri, true)
                MediaType.VIDEO -> ViewingVideosFragment.newInstance(itemUri)
            }


            holderImage.setOnClickListener {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentСontainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image1: ImageView = itemView.findViewById(R.id.image1)
        val image2: ImageView = itemView.findViewById(R.id.image2)
        val image3: ImageView = itemView.findViewById(R.id.image3)
        val playIcon1: ImageView = itemView.findViewById(R.id.playIcon1)
        val playIcon2: ImageView = itemView.findViewById(R.id.playIcon2)
        val playIcon3: ImageView = itemView.findViewById(R.id.playIcon3)
    }
}