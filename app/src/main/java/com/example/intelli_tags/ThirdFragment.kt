package com.example.intelli_tags

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import kotlinx.android.synthetic.main.fragment_second.view.*
import kotlinx.android.synthetic.main.fragment_third.view.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */

class ThirdFragment : Fragment() {
    private lateinit var viewOfLayout3rd: View
    private var videoUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout3rd = inflater.inflate(R.layout.fragment_third, container, false)
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(requireContext())
            Log.i("MyAmplifyApp", "Initialized Amplify")
//            uploadFile()
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
        viewOfLayout3rd.button2.setOnClickListener {
            Log.i("hello", "hello")
            launchGallery()

        }
        return viewOfLayout3rd
    }


    private fun launchGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            videoUri = data?.data
            Log.i("hogaya", "andar")
            Log.i("hogaya", videoUri.toString())
            uploadVideoToS3(videoUri)
        }
    }

    private fun uploadVideoToS3(videoUri: Uri?) {
        val stream = videoUri?.let { requireContext().contentResolver.openInputStream(it) }
        if (stream != null) {
            Amplify.Storage.uploadInputStream("Video.mp4", stream, {
                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")
            }, {
                Log.e("MyAmplifyApp", "Upload failed", it)
            })
        }
    }
}