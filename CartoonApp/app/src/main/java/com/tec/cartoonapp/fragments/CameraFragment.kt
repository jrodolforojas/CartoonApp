package com.tec.cartoonapp.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.tec.cartoonapp.R
import com.tec.cartoonapp.models.ImageModel
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * [CameraFragment] controls all camera logistics
 * like startCamera, show a preview, take the photo
 */
class CameraFragment : Fragment(), View.OnClickListener {

    private var imageCapture: ImageCapture? = null

    private lateinit var safeContext: Context

    private lateinit var outputDirectory: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    /**
     * When the view is created init the camera
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
        view.findViewById<Button>(R.id.btn_camera_capture).setOnClickListener(this)

    }

    /**
     * Controls all OnClickListeners
     * by id
     */
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_camera_capture -> takePhoto()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    /**
     * This functions is used to let the user preview the photo
     * they will be taking
     */
    private fun startCamera() {
        // Create an instance of ProcessCameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(safeContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        val viewFinder = view?.findViewById<PreviewView>(R.id.viewFinder)
                        it.setSurfaceProvider(viewFinder!!.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(safeContext))
    }

    /**
     * Save the photo in Uri format
     */
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        outputDirectory = getOutputDirectory()

        // Create time-stamped output file to hold the image
        // Add in a time stamp so the file name will be unique.
        val photoFile = File(outputDirectory, SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US)
                .format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(safeContext),
                object : ImageCapture.OnImageSavedCallback {

                    /**
                     * If the capture doesn't fail, the photo was taken successfully!
                     * Save the photo to the file you created earlier, present a
                     * toast to let the user know it was successful, and print a log statement.
                     */
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(safeContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraFragment", "Route image: $msg")

                    val imageModel = ImageModel(uri = savedUri)
                    val bundle = bundleOf("image" to imageModel)
                    view!!.findNavController().navigate(R.id.cartoonFragment, bundle)
                }

                    /**
                     * In the case that the image capture fails or saving the
                     * image capture fails, add in an error case to log that it failed.
                     */
                    override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraFragment", "Photo capture failed: ${exception.message}",
                            exception)
                }

            }
        )
    }

    /**
     * Get where photo will saved
     */
    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }


}