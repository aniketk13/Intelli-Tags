package com.example.intelli_tags

import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_second.view.*
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import android.R.attr.path
import android.app.backup.FileBackupHelper
import android.graphics.Region
import android.net.Uri

import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import java.lang.Exception
import java.security.Security


/**
 * A simple [Fragment] subclass.
 */
class SecondFragment : Fragment() {

    private lateinit var viewOfLayout2nd: View

    //    val s3Client = S3Client { region = "us-east-2" }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout2nd = inflater.inflate(R.layout.fragment_second, container, false)
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(requireContext())
            Log.i("MyAmplifyApp", "Initialized Amplify")
            uploadFile()
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
        viewOfLayout2nd.getFiles.setOnClickListener {
            Log.i("hello", "hello")
//            launchGallery
//            val byteArrayOutputStream = ByteArrayOutputStream()
//            val bitMap = BitmapFactory.decodeResource(resources, R.drawable.test)
//            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
//            val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
//            Log.i("base64", imageString)


//            val ACCESS_KEY = "AKIATLNIEWDMBCT744GO"
//            val SECRET_KEY = "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"
//            val MY_BUCKET = "modzey-test"
//            val OBJECT_KEY = "unique_id"
//            val credentials: AWSCredentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
//            val s3: AmazonS3 = AmazonS3Client(credentials)
//            Security.setProperty("networkaddress.cache.ttl", "60")
//            s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1))
//            s3.setEndpoint("https://s3-ap-southeast-1.amazonaws.com/")
//            val buckets: List<Bucket> = s3.listBuckets()
//            for (bucket in buckets) {
//                Log.e(
//                    "Bucket ",
//                    "Name " + bucket.getName().toString() + " Owner " + bucket.getOwner()
//                        .toString() + " Date " + bucket.getCreationDate()
//                )
//            }
//            Log.e("Size ", "" + s3.listBuckets().size())
//            val transferUtility = TransferUtility(s3, ApplicationProvider.getApplicationContext())
//            UPLOADING_IMAGE = File(
//                Environment.getExternalStorageDirectory().getPath().toString() + "/Screenshot.png"
//            )
//            val observer: TransferObserver =
//                transferUtility.upload(MY_BUCKET, OBJECT_KEY, UPLOADING_IMAGE)
//            observer.setTransferListener(object : TransferListener() {
//                fun onStateChanged(id: Int, state: TransferState) {
//                    // do something
//                    progress.hide()
//                    path.setText(
//                        """
//                ID $id
//                State ${state.name()}
//                Image ID $OBJECT_KEY
//                """.trimIndent()
//                    )
//                }
//
//                fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
//                    val percentage = (bytesCurrent / bytesTotal * 100).toInt()
//                    progress.setProgress(percentage)
//                    //Display percentage transfered to user
//                }
//
//                fun onError(id: Int, ex: Exception) {
//                    // do something
//                    Log.e("Error  ", "" + ex)
//                }
//            })
        }
        return viewOfLayout2nd
    }

    private fun uploadFile() {
        val exampleFile = File(requireContext().filesDir, "ExampleKey")
        exampleFile.writeText("Example file contents")

        Amplify.Storage.uploadFile("ExampleKey", exampleFile,
            { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },
            { Log.e("MyAmplifyApp", "Upload failed", it) }
        )
        // for accessing photos from gallery
//    private fun launchGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, 999)
//    }

    }
}


