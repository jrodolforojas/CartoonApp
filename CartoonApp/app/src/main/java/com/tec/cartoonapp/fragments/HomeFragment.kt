package com.tec.cartoonapp.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tec.cartoonapp.R
import com.tec.cartoonapp.models.ImageModel


class HomeFragment : Fragment(), View.OnClickListener {

    private var navController: NavController? = null
    private var isPermissionsChecked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Dexter.withActivity(this.activity)
            .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        view.findViewById<Button>(R.id.btn_take_photo).setOnClickListener(this)
        view.findViewById<Button>(R.id.btn_choose_gallery).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_take_photo -> {
                goToCameraFragment()
            }
            R.id.btn_choose_gallery -> {
                if (isPermissionsChecked) {
                    pickImageFromGallery()
                }
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
            val bundle = bundleOf("permissions" to isPermissionsChecked)
            navController!!.navigate(
                R.id.action_homeFragment_to_cameraFragment,
                bundle
            )
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap = MediaStore.Images.Media.
                getBitmap(context?.contentResolver, data?.data)
            Log.i("HomeFragment", "Bitmap gallery $bitmap")
        }
    }

}