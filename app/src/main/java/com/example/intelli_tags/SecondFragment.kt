package com.example.intelli_tags

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_second.view.*
import java.io.File
import android.app.Activity.RESULT_OK
import android.net.Uri
import android.provider.MediaStore
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class SecondFragment : Fragment() {

    private lateinit var viewOfLayout2nd: View
    lateinit var exampleFile: File
    private var imageUri: Uri? = null
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
            launchGallery()

        }
        return viewOfLayout2nd
    }

    private fun uploadFile() {
        exampleFile = File(requireContext().filesDir, "config.json")
        exampleFile.writeText("{\"languages\": [\"eng\", \"deu\", \"fra\", \"ita\", \"lat\", \"por\", \"spa\"] }")

        Amplify.Storage.uploadFile("config.json", exampleFile,
            { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },
            { Log.e("MyAmplifyApp", "Upload failed", it) }
        )
    }

    private fun launchGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            imageUri = data?.data
            Log.i("hogaya", "andar")
            Log.i("hogaya", imageUri.toString())
            uploadPhotoToS3(imageUri)
        }
    }

    private fun uploadPhotoToS3(imageUri: Uri?) {
        val stream = imageUri?.let { requireContext().contentResolver.openInputStream(it) }
        if (stream != null) {
            Amplify.Storage.uploadInputStream("Image.png", stream, {
                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")

                //api call to get the jobId

                //body
                val finalBody = JSONObject()
                val temp1 = JSONObject()
                temp1.put("identifier", "c60c8dbd79")
                temp1.put("version", "0.0.2")

                finalBody.put("model", temp1)

                val temp2 = JSONObject()
                temp2.put("bucket", "modzybucket35738-dev")
                temp2.put("key", "public/config.json")

                val confiWaliBody = JSONObject()

                //daalni hai
                confiWaliBody.put("config.json", temp2)

                val temp3 = JSONObject()
                temp3.put("bucket", "modzybucket35738-dev")
                temp3.put("key", "public/Image.png")

                val inputWaliBody = JSONObject()
                inputWaliBody.put("input",temp3)
                inputWaliBody.put("config.json", temp2)

                val body0001 = JSONObject()


//                body0001.put("0001",inputWaliBody)

                val sourcesWaliBaat = JSONObject()
                sourcesWaliBaat.put("0001", inputWaliBody)

                val properInput = JSONObject()

//                val tempo = JSONObject()
                properInput.put("type", "aws-s3")
                properInput.put("accessKeyID", "AKIATLNIEWDMNMKGF4EF")
                properInput.put("secretAccessKey", "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")
                properInput.put("region", "us-east-2")
                properInput.put("sources", sourcesWaliBaat)

                finalBody.put("input", properInput)
                Log.i("body",finalBody.toString())



//                val temp3 = JSONObject()
//                temp3.put("input", "public/Image.png")
//                temp3.put("config.json", exampleFile)
//
//                val temp2 = JSONObject()
//                temp2.put("my-input", temp3)
//
//                val temp4 = JSONObject()
//                temp4.put("type", "embedded")
//                temp4.put("sources", temp2)

                //final body
//                finalBody.put("input", temp4)
//                Log.i("finalBody", finalBody.toString())


            }, {
                Log.e("MyAmplifyApp", "Upload failed", it)
            })
        }
    }
}



