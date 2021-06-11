package com.tec.cartoonapp.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.tec.cartoonapp.R
import com.tec.cartoonapp.ml.WhiteboxCartoonGanDr
import com.tec.cartoonapp.ml.WhiteboxCartoonGanFp16
import com.tec.cartoonapp.ml.WhiteboxCartoonGanInt8
import com.tec.cartoonapp.models.ImageModel
import kotlinx.coroutines.*
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model


class CartoonFragment : Fragment() {

    private lateinit var imageModel: ImageModel
    private var mProgressDialog: Dialog? = null

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + parentJob
    )

    private var modelType: Int = 0

    private fun getOutputAsync(bitmap: Bitmap): Deferred<Pair<Bitmap, Long>> =
        // use async() to create a coroutine in an IO optimized Dispatcher for model inference
        coroutineScope.async(Dispatchers.IO) {

            // GPU delegate
            val options = Model.Options.Builder()
                .setDevice(Model.Device.GPU)
                .setNumThreads(4)
                .build()

            // Input
            val sourceImage = TensorImage.fromBitmap(bitmap)

            // Output
            val cartoonizedImage: TensorImage
            val startTime = SystemClock.uptimeMillis()
            when (modelType) {
                0 -> cartoonizedImage = inferenceWithDrModel(sourceImage)               // DR
                1 -> cartoonizedImage = inferenceWithFp16Model(sourceImage)             // Fp16
                2 -> cartoonizedImage = inferenceWithInt8Model(sourceImage, options)    // Int8
                else -> cartoonizedImage = inferenceWithDrModel(sourceImage)

            }

            // Note this inference time includes pre-processing and post-processing
            val inferenceTime = SystemClock.uptimeMillis() - startTime
            val cartoonizedImageBitmap = cartoonizedImage.bitmap

            return@async Pair(cartoonizedImageBitmap, inferenceTime)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        imageModel = arguments?.getParcelable("image")!!
        // Inflate the layout for this fragment
        showCustomProgressDialog()
        return inflater.inflate(R.layout.fragment_cartoon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoBitmap = convertToBitmap(imageModel)
        coroutineScope.launch(Dispatchers.Main) {
            val (outputBitmap, inferenceTime) = getOutputAsync(photoBitmap).await()
            Log.i("CartoonFragment", "Time: $inferenceTime")
            updateUI(photoBitmap, outputBitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
    }


    private fun convertToBitmap(imageModel: ImageModel): Bitmap{
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
            requireContext().contentResolver, imageModel.uri)
        Log.i("Cartoon", "Bitmap: $bitmap")
        return bitmap
    }

    /**
     * Run inference with the dynamic range tflite model
     */
    private fun inferenceWithDrModel(sourceImage: TensorImage): TensorImage {
        val model = WhiteboxCartoonGanDr.newInstance(requireContext())

        // Runs model inference and gets result.
        val outputs = model.process(sourceImage)
        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage

        // Releases model resources if no longer used.
        model.close()

        return cartoonizedImage
    }

    /**
     * Run inference with the fp16 tflite model
     */
    private fun inferenceWithFp16Model(sourceImage: TensorImage): TensorImage {
        val model = WhiteboxCartoonGanFp16.newInstance(requireContext())

        // Runs model inference and gets result.
        val outputs = model.process(sourceImage)
        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage

        // Releases model resources if no longer used.
        model.close()

        return cartoonizedImage
    }

    /**
     * Run inference with the int8 tflite model
     */
    private fun inferenceWithInt8Model(
        sourceImage: TensorImage,
        options: Model.Options
    ): TensorImage {
        val model = WhiteboxCartoonGanInt8.newInstance(requireContext(), options)

//        val model = WhiteboxCartoonGanInt8.newInstance(requireContext())
        // Runs model inference and gets result.
        val outputs = model.process(sourceImage)
        val cartoonizedImage = outputs.cartoonizedImageAsTensorImage

        // Releases model resources if no longer used.
        model.close()

        return cartoonizedImage
    }

    private fun updateUI(source: Bitmap, cartoon: Bitmap) {
        hideProgressDialog()
        val ivPhoto = requireView().findViewById<ImageView>(R.id.iv_photo)
        ivPhoto.setImageBitmap(source)

        val ivCartoon = requireView().findViewById<ImageView>(R.id.iv_cartoon)
        ivCartoon.setImageBitmap(cartoon)
    }

    /**
     * Shows my customProgressDialog on the current Activity
     */
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireContext())

        /*
         * Set the screen content from a layout resource.
         * The resource will inflated, adding all top-level views
         * to the screen.
         */
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)

        // Start the dialog and display it on screen.
        mProgressDialog!!.show()
    }

    /**
     * Hide my custom progress dialog
     */
    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }


}