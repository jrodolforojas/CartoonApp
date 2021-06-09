package com.tec.cartoonapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.tec.cartoonapp.R
import java.io.File
import java.util.concurrent.ExecutorService
typealias LumaListener = (luma: Double) -> Unit

class HomeFragment : Fragment(), View.OnClickListener {

    private var navController: NavController? = null
    private var imageCapture: ImageCapture? = null
    private var isPermissionsChecked: Boolean = false

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Dexter.withActivity(this.activity)
            .withPermissions(
                Manifest.permission.CAMERA
            )
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        isPermissionsChecked = true
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(
                            context,
                            "You have denied location permission. " +
                                    "Please enable them as it is mandatory " +
                                    "for the app to work.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.btn_fragment_home).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_fragment_home -> {
                goToCameraFragment()
            }
        }
    }

    private fun showRationalDialogForPermissions() {
        activity?.let {
            AlertDialog.Builder(it)
                .setMessage("It look like you have turned off permissions " +
                        "required for this feature. It can be enabled under Aplication Settings")
                .setPositiveButton(
                    "GO TO SETTINGS"
                ) {_,_ -> // When you click on that button (GO TO SETTINGS)
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        // it's necessary to go to DETAIL SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            activity?.packageName,
                            null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, _ ->
                    run {
                        dialog.dismiss()
                    }
                }.show()
        }
    }

    private fun goToCameraFragment() {
        if (isPermissionsChecked){
            navController!!.navigate(R.id.action_homeFragment_to_cameraFragment)
        }
    }

}