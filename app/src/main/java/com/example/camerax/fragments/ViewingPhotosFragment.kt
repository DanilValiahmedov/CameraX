package com.example.camerax.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.camerax.R
import com.example.camerax.database.InformMedia
import com.example.camerax.database.MainDB
import com.example.camerax.databinding.FragmentViewingPhotosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewingPhotosFragment : Fragment() {

    private lateinit var viewBinding: FragmentViewingPhotosBinding

    private val db: MainDB by lazy { MainDB.getDB(requireContext()) }
    private var uri: String = ""
    private var firstOpening: Boolean = true
    private var uris: List<InformMedia> = emptyList()
    private var currentIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentViewingPhotosBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            uri = it.getString("uri") ?: ""
            firstOpening = it.getBoolean("firstOpening")
        }
        Glide.with(requireContext()).load(uri).into(viewBinding.photo)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            uris = db.getDao().getAllUri()
            currentIndex = uris.indexOfFirst { it.uri == uri }
            withContext(Dispatchers.Main) {
                updatePhoto()
            }
        }

        if(firstOpening) changViewMode(View.GONE, R.color.black)

        viewBinding.leftButton.setOnClickListener { changePhoto(1) }
        viewBinding.rightButton.setOnClickListener { changePhoto(-1)  }
        viewBinding.deleteButton.setOnClickListener { deletePhoto(uri) }
        viewBinding.photo.setOnClickListener{
            if(viewBinding.deleteButton.visibility == View.GONE) {
                changViewMode(View.VISIBLE, R.color.white)
            }
            else changViewMode(View.GONE, R.color.black)
        }
    }

    private fun changViewMode(view: Int, resColor: Int){
        listOf(viewBinding.deleteButton, viewBinding.leftButton, viewBinding.rightButton).forEach {
            it.visibility = view
        }
        val color = ContextCompat.getColor(requireContext(), resColor)
        viewBinding.back.setBackgroundColor(color)
    }

    private fun deletePhoto(uri: String) {
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
                        requireContext(), getString(R.string.delete_photo), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun changePhoto(orientation: Int) {
        val newIndex = currentIndex + orientation
        if (newIndex in uris.indices) {
            currentIndex = newIndex
            updatePhoto()
        }
    }

    private fun updatePhoto() {
        uri = uris[currentIndex].uri
        Glide.with(requireContext()).load(uri).into(viewBinding.photo)
    }

    companion object {
        @JvmStatic
        fun newInstance(uri: String, firstOpening: Boolean) = ViewingPhotosFragment().apply {
            arguments = Bundle().apply {
                putString("uri", uri)
                putBoolean("firstOpening", firstOpening)
            }
        }
    }

}