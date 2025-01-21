package com.example.camerax.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camerax.database.MainDB
import com.example.camerax.databinding.FragmentGalleryBinding
import com.example.camerax.recycleview.AdapterPhoto
import com.example.camerax.recycleview.RecyclerMedia
import kotlinx.coroutines.CoroutineScope
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

        showMedia()
    }

    private fun showMedia()  {
        CoroutineScope(Dispatchers.IO).launch {
            val uris = db.getDao().getAllUri()

            // Преобразование списка в группы по 3 элемента
            val recyclerUris = uris.asReversed()
                .chunked(3)
                .map { chunk ->
                    RecyclerMedia(
                        image1 = chunk.getOrNull(0)?.uri ?: "",
                        image2 = chunk.getOrNull(1)?.uri ?: "",
                        image3 = chunk.getOrNull(2)?.uri ?: ""
                    )
                }
            withContext(Dispatchers.Main) {
                viewBinding.recycler.adapter = AdapterPhoto(
                    requireContext(), recyclerUris
                )
            }
        }
    }

}