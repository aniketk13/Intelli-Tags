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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.android.synthetic.main.fragment_second.view.searchTags
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder

class SecondFragment : Fragment() {

    private lateinit var viewOfLayout2nd: View
    lateinit var progressBar: ProgressBar
    lateinit var filepath1: String
    lateinit var ai: ApplicationInfo
    lateinit var exampleFile: File
    private var imageUri: Uri? = null
    lateinit var fileName: String
    private var output=StringBuilder()
    lateinit var response: String
    lateinit var outputText:String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout2nd = inflater.inflate(R.layout.fragment_second, container, false)
        ai = viewOfLayout2nd.context.packageManager
            .getApplicationInfo(viewOfLayout2nd.context.packageName, PackageManager.GET_META_DATA)
        progressBar = viewOfLayout2nd.findViewById(R.id.progressBar2nd)

        viewOfLayout2nd.getFiles.setOnClickListener {
            Log.i("hello", "hello")
            launchGallery()
        }


        //If search button is clicked
        viewOfLayout2nd.searchTags.setOnClickListener {
            if (imageUri == null)
                Toast.makeText(
                    viewOfLayout2nd.context,
                    "Please put a valid input",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else {
                viewOfLayout2nd.searchTags.isEnabled = false
                viewOfLayout2nd.copyButton2nd.isEnabled = false
                viewOfLayout2nd.shareButton2nd.isEnabled = false
                progressBar.visibility = View.VISIBLE

//                Disabled buttons and called function to generate the job-id
                getJobId()
            }
        }

        //        If copy to clipboard button is clicked
        viewOfLayout2nd.copyButton2nd.setOnClickListener {
            if (imageUri == null)
                Toast.makeText(
                    viewOfLayout2nd.context,
                    "Please put a valid input",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else if (textView2nd.text == "")
                Toast.makeText(viewOfLayout2nd.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                copy_to_clipboard(output.toString())
        }

//        If share button is clicked
        viewOfLayout2nd.shareButton2nd.setOnClickListener {
            if (imageUri == null)
                Toast.makeText(
                    viewOfLayout2nd.context,
                    "Please put a valid input",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else if (textView2nd.text == "")
                Toast.makeText(viewOfLayout2nd.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                shareText(output.toString())
        }
        return viewOfLayout2nd
    }

    //uploading the file to AWS
    private fun uploadFile() {
        exampleFile = File(requireContext().filesDir, "config.json")
        exampleFile.writeText("{\"languages\": [\"eng\", \"deu\", \"fra\", \"ita\", \"lat\", \"por\", \"spa\"] }")

        Amplify.Storage.uploadFile("config.json", exampleFile,
            { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },
            { Log.e("MyAmplifyApp", "Upload failed", it) }
        )
    }

    //launching the gallery of user
    private fun launchGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    //getting the image URI
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        progressBar.visibility = View.VISIBLE

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            viewOfLayout2nd.textView2nd.text=""
            output.delete(0,output.length)
            imageUri = data?.data
            val returnCursor: Cursor? =
                imageUri?.let { requireContext().contentResolver.query(it, null, null, null, null) }
            try {
                val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                fileName = returnCursor.getString(nameIndex)
                Log.i("hello", "file name : $fileName")
            } catch (e: Exception) {
                Log.i(ContentValues.TAG, "error: ", e)
                //handle the failure cases here
            } finally {
                if (returnCursor != null) {
                    returnCursor.close()
                }
            }
            Log.i("Image URI", imageUri.toString())

            //getting the file path
            val uriPathHelper = URIPathHelper()
            filepath1 = uriPathHelper.getPath(viewOfLayout2nd.context, imageUri).toString()
            Log.i("path", filepath1)

            viewOfLayout2nd.textViewImageName.text = fileName
            uploadFile()
            uploadPhotoToS3(imageUri)
        }
    }

    //uploading the photo to AWS S3 bucket
    private fun uploadPhotoToS3(imageUri: Uri?) {
        val stream = imageUri?.let { requireContext().contentResolver.openInputStream(it) }
        if (stream != null) {
            Amplify.Storage.uploadInputStream("Image.png", stream, {
                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")
                progressBar.visibility = View.GONE
            }, {
                Log.e("MyAmplifyApp", "Upload failed", it)
            })
        }
    }

    //getting the job ID
    private fun getJobId() {
        val url = "https://app.modzy.com/api/jobs"
        val finalBody = JSONObject()
        val temp1 = JSONObject()
        temp1.put("identifier", "c60c8dbd79")
        temp1.put("version", "0.0.2")

        finalBody.put("model", temp1)

        val temp2 = JSONObject()
        temp2.put("bucket", "modzybucket35738-dev")
        temp2.put("key", "public/config.json")

        val configBody = JSONObject()
        configBody.put("config.json", temp2)

        val temp3 = JSONObject()
        temp3.put("bucket", "modzybucket35738-dev")
        temp3.put("key", "public/Image.png")

        val inputBody = JSONObject()
        inputBody.put("input", temp3)
        inputBody.put("config.json", temp2)

        val sourcesBody = JSONObject()
        sourcesBody.put("0001", inputBody)

        val properInput = JSONObject()
        properInput.put("type", "aws-s3")
        properInput.put("accessKeyID", "${ai.metaData["AWSAccessId"]}")
        properInput.put("secretAccessKey", "${ai.metaData["AWSSecretKey"]}")
        properInput.put("region", "us-east-2")
        properInput.put("sources", sourcesBody)

        finalBody.put("input", properInput)
        Log.i("Final Body", finalBody.toString())

        val queue = Volley.newRequestQueue(viewOfLayout2nd.context)
        val req = object : JsonObjectRequest(
            Method.POST, url, finalBody,
            {
                response = it.getString("jobIdentifier")
//                Handler().postDelayed({ getTextOut(response) }, 8000)
                getStatus()
                Log.i("identifier", response)

            }, {
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue.add(req)
    }

    //    Function to check if job is completed
    private fun getStatus() {
        var outputText = ""
        val queue2 = Volley.newRequestQueue(viewOfLayout2nd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/jobs/$response", null,
            {
                outputText = it.getString("status")

//                Checking job status every 2 sec
                Handler().postDelayed({
                    if (outputText == "COMPLETED")
//                        sending job id to extract the caption
                        getTextOut()
                    else
                        getStatus()
                }, 2000)

            }, {
                Log.i("Job Status Failed", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue2.add(req)
    }

    //getting the text of the image
    private fun getTextOut() {
        Log.i("status", response)
        val queue2 = Volley.newRequestQueue(viewOfLayout2nd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                outputText = (it.getJSONObject("results")).getJSONObject("0001")
                    .getJSONObject("results.json").getString("text")

//                sending the text to process and generate tags
                processText()
                Log.i("Text", outputText)

            }, {
                Toast.makeText(requireContext(), "Operation Failed", Toast.LENGTH_SHORT)
                    .show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue2.add(req)
    }

    //processing the text to get the topics
    private fun processText() {

        val url = "https://app.modzy.com/api/jobs"
        val body = JSONObject()
        val body2 = JSONObject()
        body2.put("identifier", "m8z2mwe3pt")
        body2.put("version", "0.0.1")
        body.put("model", body2)
        val body4 = JSONObject()
        body4.put("input.txt", outputText)
        val body3 = JSONObject()
        body3.put("my-input", body4)
        val body5 = JSONObject()
        body5.put("type", "text")
        body5.put("sources", body3)

        //final body
        body.put("input", body5)
        Log.i("body", body.toString())

        val queue = Volley.newRequestQueue(viewOfLayout2nd.context)
        var response = ""
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                response = it.getString("jobIdentifier")
                getStatus2(response)
                Log.i("identifier", response)

            }, {
                Toast.makeText(requireContext(), "Operation Failed", Toast.LENGTH_SHORT).show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue.add(req)
    }

    //    Function to check if job is completed
    private fun getStatus2(response: String) {
        var outputText = ""
        val queue2 = Volley.newRequestQueue(viewOfLayout2nd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/jobs/$response", null,
            {
                outputText = it.getString("status")

//                Checking job status every 0.5 sec
                Handler().postDelayed({
                    if (outputText == "COMPLETED")
//                        sending job id to extract the caption
                        getTopics(response)
                    else
                        getStatus2(response)
                }, 500)

            }, {
                Log.i("Job Status Failed", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue2.add(req)
    }

    //getting topics of the text
    fun getTopics(response: String) {
        val queue2 = Volley.newRequestQueue(viewOfLayout2nd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
                    .getJSONArray("results.json")
                Log.i("topics", topics.toString())
//                output = StringBuilder()
                for (i in 0 until topics.length())
                    output.append("#").append(topics[i]).append(" ")
                Log.i("topics", output.toString())
                //progress bar stops
                progressBar.visibility = View.GONE
                viewOfLayout2nd.findViewById<TextView>(R.id.textView2nd).text = output

                //enabling all the buttons
                viewOfLayout2nd.getFiles.isEnabled = true
                viewOfLayout2nd.searchTags.isEnabled = true
                viewOfLayout2nd.copyButton2nd.isEnabled = true
                viewOfLayout2nd.shareButton2nd.isEnabled = true

            }, {
                Toast.makeText(requireContext(), "Operation Failed", Toast.LENGTH_SHORT)
                    .show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                return headerMap
            }
        }
        queue2.add(req)
    }

    private fun shareText(topicsToShare: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Tags are:\n$topicsToShare"
        )
        val chooser = Intent.createChooser(intent, "Share Via")
        startActivity(chooser)
    }

    //function to copy the text to the clipboard
    private fun copy_to_clipboard(topics: String) {
        val textToCopy = topics
        val clipboard =
            ContextCompat.getSystemService(
                requireContext(),
                ClipboardManager::class.java
            ) as ClipboardManager
        val clip = ClipData.newPlainText("label", textToCopy)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(viewOfLayout2nd.context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }
}