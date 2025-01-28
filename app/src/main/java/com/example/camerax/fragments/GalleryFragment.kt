package com.example.camerax.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerax.database.MainDB
import com.example.camerax.database.MediaType
import com.example.camerax.databinding.FragmentGalleryBinding
import com.example.camerax.recycleview.AdapterPhoto
import com.example.camerax.recycleview.RecyclerMediaOne
import com.example.camerax.recycleview.RecyclerMediasThree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryFragment : Fragment() {

    private lateinit var viewBinding: FragmentGalleryBinding

    private lateinit var db: MainDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentGalleryBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = MainDB.getDB(requireContext())

        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        showMedia(MediaType.PHOTO, viewBinding.photoShow, viewBinding.videoShow)

        viewBinding.photoShow.setOnClickListener {
            showMedia(MediaType.PHOTO, viewBinding.photoShow, viewBinding.videoShow)
        }

        viewBinding.videoShow.setOnClickListener {
            showMedia(MediaType.VIDEO, viewBinding.videoShow, viewBinding.photoShow)
        }
    }

    private fun showMedia(requiredMediaType: MediaType, activeButton: Button, disabledButton: Button)  {
        disabledButton.isEnabled = true
        activeButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val uris = db.getDao().getMediaByType(requiredMediaType)

            // Преобразование списка в группы по 3 элемента
            val recyclerUris = uris.asReversed()
                .chunked(3)
                .map { chunk ->
                    val mediaList = chunk.map { item ->
                        RecyclerMediaOne(
                            uri = item.uri,
                            type = item.mediaType
                        )
                    }
                    RecyclerMediasThree(
                        media1 = mediaList.getOrNull(0),
                        media2 = mediaList.getOrNull(1),
                        media3 = mediaList.getOrNull(2),
                    )
                }
            withContext(Dispatchers.Main) {
                val adapter = viewBinding.recycler.adapter as? AdapterPhoto
                if (adapter != null) {
                    adapter.updateList(recyclerUris)
                } else {
                    viewBinding.recycler.adapter = AdapterPhoto(
                        requireContext(),
                        parentFragmentManager,
                        recyclerUris
                    )
                }
            }
        }
    }

}