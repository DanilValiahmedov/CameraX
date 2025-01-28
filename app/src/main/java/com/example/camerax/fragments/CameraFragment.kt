package com.example.camerax.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.example.camerax.R
import com.example.camerax.database.InformMedia
import com.example.camerax.database.MainDB
import com.example.camerax.database.MediaType
import com.example.camerax.databinding.FragmentCameraBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var viewBinding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var cameraSelectorGlobal = CameraSelector.DEFAULT_BACK_CAMERA

    private lateinit var db: MainDB


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkingPermission()

        // Инициализация или вызов базы данных
        db = MainDB.getDB(requireContext())

        viewBinding.photoButton.setOnClickListener { takePhoto() }
        viewBinding.videoButton.setOnClickListener { captureVideo() }
        viewBinding.turnButton.setOnClickListener { choosingCamera() }
        viewBinding.galleryButton.setOnClickListener { openGalleryFragment() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Создаем имя файла с меткой времени
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Создаем файл в директории приложения
        val photoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "$name.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = getString(R.string.saved_photo)
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Сохранение URI в базу данных
                    val informMedia = InformMedia(null, savedUri.toString(), MediaType.PHOTO)

                    CoroutineScope(Dispatchers.IO).launch {
                        db.getDao().insertUri(informMedia)
                    }
                }
            }
        )
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            //Остановка текущего сеанса записи
            curRecording.stop()
            recording = null
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Путь для сохранения файла во внутреннее хранилище
        val videoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "$name.mp4"
        )
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        //Начинаем новую запись
        recording = videoCapture.output
            .prepareRecording(requireContext(), outputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        changUIForVideo(View.GONE, R.color.white, getString(R.string.stop_video))
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val savedUri = Uri.fromFile(videoFile)
                            Toast.makeText(requireContext(), getString(R.string.saved_video), Toast.LENGTH_SHORT).show()
                            // Сохранение URI в базу данных
                            val informMedia = InformMedia(null, savedUri.toString(), MediaType.VIDEO)

                            CoroutineScope(Dispatchers.IO).launch {
                                db.getDao().insertUri(informMedia)
                            }
                        } else {
                            recording?.close()
                            recording = null
                            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
                        }
                        changUIForVideo(View.VISIBLE, R.color.red_button, getString(R.string.start_video))
                    }
                }
            }
    }

    private fun changUIForVideo(view: Int, resColor: Int, text: String) {
        viewBinding.videoText.text = text
        viewBinding.videoButton.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), resColor))
            isEnabled = true
        }

        listOf(viewBinding.photoButton, viewBinding.turnButton, viewBinding.galleryButton,
            viewBinding.photoText, viewBinding.turnText, viewBinding.galleryText).forEach {
            it.visibility = view
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Привязка жизненного цикла камер к жизненному циклу активити
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            //Инициализация объекта предварительного просмотра камеры
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }


            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Выбор камеры
            val cameraSelector = cameraSelectorGlobal

            try {
                // Отмена уже имеющиеся привязки
                cameraProvider.unbindAll()

                // Привязка к камере
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Не удалось выполнить привязку к камере", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun openGalleryFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentСontainer, GalleryFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun choosingCamera() {
        cameraSelectorGlobal = if(cameraSelectorGlobal == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        checkingPermission()
    }

    private fun checkingPermission() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                        permissionGranted = false
                }
                if (!permissionGranted) {
                    Toast.makeText(requireContext(),
                        getString(R.string.no_permission),
                        Toast.LENGTH_SHORT).show()
                } else {
                    startCamera()
                }
            }

        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "MyAppCameraX"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}