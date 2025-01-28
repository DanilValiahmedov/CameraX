package com.example.camerax.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.camerax.R
import com.example.camerax.database.InformMedia
import com.example.camerax.database.MainDB
import com.example.camerax.database.MediaType
import com.example.camerax.databinding.FragmentViewingVideosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewingVideosFragment : Fragment() {

    private lateinit var viewBinding: FragmentViewingVideosBinding

    private val db: MainDB by lazy { MainDB.getDB(requireContext()) }
    private var uri: String = ""
    private var uris: List<InformMedia> = emptyList()
    private var currentIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentViewingVideosBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            uri = it.getString("uri") ?: ""
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            uris = db.getDao().getMediaByType(MediaType.VIDEO)
            currentIndex = uris.indexOfFirst { it.uri == uri }
            withContext(Dispatchers.Main) {
                updateVideo()
            }
        }

        viewBinding.delete.setOnClickListener { deleteVideo() }

        viewBinding.left.setOnClickListener { changeVideo(1) }
        viewBinding.right.setOnClickListener { changeVideo(-1) }

    }

    private fun deleteVideo() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Удаление из базы данных
            val id = uris[currentIndex].id
            if (id != null) db.getDao().deleteById(id)

            // Удаление из хранилища
            val filePath = Uri.parse(uri).path
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                withContext(Dispatchers.Main) {
                    parentFragmentManager.popBackStack()
                    Toast.makeText(
                        requireContext(), getString(R.string.delete_video), Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changeVideo(orientation: Int) {
        val newIndex = currentIndex + orientation
        if (newIndex in uris.indices) {
            currentIndex = newIndex
            updateVideo()
        }
    }

    private fun updateVideo() {
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(viewBinding.videoView)

        val uriVideo = Uri.parse(uri)

        viewBinding.videoView.apply {
            setMediaController(mediaController)
            setVideoURI(uriVideo)
            requestFocus()
            start()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(uri: String) = ViewingVideosFragment().apply {
            arguments = Bundle().apply {
                putString("uri", uri)
            }
        }
    }
}