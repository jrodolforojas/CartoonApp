package com.tec.cartoonapp.fragments

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tec.cartoonapp.R
import com.tec.cartoonapp.ml.LiteModelCartoonganDr1
import com.tec.cartoonapp.models.ImageModel
import org.tensorflow.lite.support.image.TensorImage


class CartoonFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        image = arguments?.getString("image").toString()
//        Log.i("Cartoon", "Image: $image")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Log.i("Cartoon", "Here")
        val image = arguments?.getParcelable<ImageModel>("image")
        Log.i("Cartoon", "Image: ${image!!.uri}")
        return inflater.inflate(R.layout.fragment_cartoon, container, false)

    }
}