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

/**
 * [HomeFragment] is the fragment host
 * This class checks the user camera permissions and
 * Go to Camera fragment
 */
class HomeFragment : Fragment(), View.OnClickListener {

    // NavController allows to navigate between fragments
    private var navController: NavController? = null

    // A flag is permissions are checked
    private var isPermissionsChecked: Boolean = false

    /**
     * [onCreateView] checks the userPermissions
     * and return the UI
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create a Dexter objects with Camera Permission
        Dexter.withActivity(this.activity)
            .withPermissions(
                    Manifest.permission.CAMERA,
            )
            .withListener(object: MultiplePermissionsListener {
                // If you allow the permissions
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

                // If you reject some permission
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Call permissions dialog
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check() // Run on the main thread

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * When the view is already created
     * Set navController
     * Set btn_take_photo onClickListener
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<Button>(R.id.btn_take_photo).setOnClickListener(this)
    }

    /**
     * This function controls all
     * OnClickListeners
     */
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_take_photo -> {
                goToCameraFragment()
            }
        }
    }

    /**
     * Show a Dialog in case you reject some permission
     * because it's necessary to continue using the App
     */
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

    /**
     * This function first of all
     * checks if we have permissions and then
     * go to Camera Fragment
     */
    private fun goToCameraFragment() {
        if (isPermissionsChecked){
            val bundle = bundleOf("permissions" to isPermissionsChecked)
            navController!!.navigate(
                R.id.action_homeFragment_to_cameraFragment,
                bundle
            )
        }
    }

}