package com.example.intelli_tags

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_second.view.*
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream

/**
 * A simple [Fragment] subclass.
 */
class SecondFragment : Fragment() {

    private lateinit var viewOfLayout2nd: View

    @SuppressLint("WrongThread")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout2nd = inflater.inflate(R.layout.fragment_second, container, false)

        viewOfLayout2nd.getFiles.setOnClickListener {
//            launchGallery
            val byteArrayOutputStream = ByteArrayOutputStream()
            val bitMap = BitmapFactory.decodeResource(resources, R.drawable.test)
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
            val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            Log.i("base64", imageString)
        }
        return viewOfLayout2nd
    }


    // for accessing photos from gallery
//    private fun launchGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, 999)
//    }

}

