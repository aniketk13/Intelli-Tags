package com.example.intelli_tags

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.options.StorageUploadInputStreamOptions
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_third.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder

/**
 * A simple [Fragment] subclass.
 */

class ThirdFragment : Fragment() {
    private lateinit var viewOfLayout3rd: View
    lateinit var progressBar: ProgressBar
    private var videoUri: Uri? = null
    lateinit var ai: ApplicationInfo
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout3rd = inflater.inflate(R.layout.fragment_third, container, false)
        ai = viewOfLayout3rd.context.packageManager
            .getApplicationInfo(viewOfLayout3rd.context.packageName, PackageManager.GET_META_DATA)
        progressBar = viewOfLayout3rd.findViewById(R.id.progressBar3rd)
        progressBar.visibility = View.GONE
        try {
//            Amplify startup
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(requireContext())
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }

//        checking if upload button is clicked to launch gallery
        viewOfLayout3rd.button2.setOnClickListener {
//            launchGallery()
            getConversationId()
        }
        return viewOfLayout3rd
    }

    private fun getConversationId() {
        val url = "https://api.symbl.ai/v1/process/video/url"
        val body = JSONObject()
        body.put("url", "https://modzybucket35738-dev.s3.us-east-2.amazonaws.com/public/Video.mp4")
        body.put("name", "General")

        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
//        var response=JSONArray()
//        var ans=StringBuilder()
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                val response = it.getString("conversationId")
                Log.i("Conversation_id", response)
                Handler().postDelayed({ getMessage(response) }, 20000)
//                Log.i("Video-Captioning-JobId", ans.toString())
//                processText(ans.toString() )
//                Calling getStatus to check if job is completed
//                getStatus(response)
            },
            {
//                If network call fails
                Log.i("Video-Captioning API Call Failed", it.message.toString())
            }
        ) {
            //            Writing the required Headers for API call
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
//                headerMap["Authorization"] = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlFVUTRNemhDUVVWQk1rTkJNemszUTBNMlFVVTRRekkyUmpWQ056VTJRelUxUTBVeE5EZzFNUSJ9.eyJodHRwczovL3BsYXRmb3JtLnN5bWJsLmFpL3VzZXJJZCI6IjYzNzcyOTEwMjMxMjI0MzIiLCJpc3MiOiJodHRwczovL2RpcmVjdC1wbGF0Zm9ybS5hdXRoMC5jb20vIiwic3ViIjoicG9lcGNQV3Z1dHZFd2Q0TXQ0Z0VTZExTdHpNV1d5UVZAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vcGxhdGZvcm0ucmFtbWVyLmFpIiwiaWF0IjoxNjM3OTA1OTc1LCJleHAiOjE2Mzc5OTIzNzUsImF6cCI6InBvZXBjUFd2dXR2RXdkNE10NGdFU2RMU3R6TVdXeVFWIiwiZ3R5IjoiY2xpZW50LWNyZWRlbnRpYWxzIn0.0jl7QY628ofEDaWSurRO2Us_CQjw4hE6R4C4fxUxfvL6akkHNWMaZZQSfW3XTrtdQxoK6Fe_CMspqpHINyrDUFOYzKAQ0BfwlsfxyB4Cenw20jmuLHyQkfjMQx2FgoSpqtS1Ok0To9JHqNKD4YJ6vLy9TRw5w-p_NSD_ThXg6FXCRSy5oUlOVoSKpZGBYiBDDCrNDDC6pYgAxZwj0XzLTE7FOUAL_Tku6wlHT0HzTHIAP3Qnu35RF7VSeHgMF1SwP0zlLd5CObE8Oh-PMOSjuVpvm2Vfx4a_i5buxMKR98bIfm6f8L5C3Cu13i4bKas9mQ417eDBP3REJ3Y3z4optQ"
                headerMap["Authorization"] = "Bearer ${ai.metaData["Symbl_Token"]}"
//                headerMap["Content-Type"] = "application/json"
//                headerMap["Accept"] = "application/json"
//                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
    }

    //    Gallery launch function to select a video file
    private fun launchGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    //    This runs just after the video is selected to check if it was a success
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        progressBar.visibility = View.VISIBLE
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            videoUri = data?.data
            val returnCursor: Cursor? =
                videoUri?.let { requireContext().contentResolver.query(it, null, null, null, null) }
            try {
                val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                val fileName = returnCursor.getString(nameIndex)
                Log.i("hello", "file name : $fileName")
            } catch (e: Exception) {
                Log.i(ContentValues.TAG, "error: ", e)
                //handle the failure cases here
            } finally {
                if (returnCursor != null) {
                    returnCursor.close()
                }
            }
            Log.i("Video Uri", videoUri.toString())

//            Calling this function to upload video to AWS
            uploadVideoToS3(videoUri)
        }
    }

    //    Function to upload the selected video to AWS
    private fun uploadVideoToS3(videoUri: Uri?) {
        val stream = videoUri?.let { requireContext().contentResolver.openInputStream(it) }
        val options =
            StorageUploadInputStreamOptions.builder().accessLevel(StorageAccessLevel.PUBLIC)
                .build()
//    if video uri is not null then uploading the video
        if (stream != null) {
            Amplify.Storage.uploadInputStream("Video.mp4", stream, options, {
                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")
                getConversationId()
//                Amplify.Storage.getUrl("public/Video.mp4",
//                    {
//                        Log.i("Object Url", "${it.url}")
//                        getConversationId()},
//                    },
//                    {
//                    Log.i("Video not found","Error")
//                })
//                After completion, starting the API network call to get the job id for video captioning
//                getJobId()
//                Amplify.Storage.getUrl("public/Video.mp4")
//                getConversationId(${it.url}.toString())
            }, {
                Log.e("MyAmplifyApp", "Upload failed", it)
            })
        }
    }


    fun getMessage(convId: String) {
        val url = "https://api.symbl.ai/v1/conversations/$convId/messages"
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        var response = JSONArray()
        var ans = StringBuilder()
        val req = object : JsonObjectRequest(
            Method.GET, url, null,
            {
                response = it.getJSONArray("messages")
                for (i in 0 until response.length() step 1)
//                    Log.i("Response$i",response.getJSONObject(i).getString("text"))
                    ans.append(response.getJSONObject(i).getString("text")).append(" ")

                Log.i("Video-Captioning-JobId", ans.toString())
                processText(ans.toString())
//                Calling getStatus to check if job is completed
//                getStatus(response)
            },
            {
//                If network call fails
                Log.i("Video-Captioning API Call Failed", it.message.toString())
            }
        ) {
            //            Writing the required Headers for API call
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "Bearer ${ai.metaData["Symbl_Token"]}"
//                headerMap["Content-Type"] = "application/json"
//                headerMap["Accept"] = "application/json"
//                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
    }


    //    Sending extracted caption to Text-Topic Modelling API Call for jobId generation
    private fun processText(text: String) {

        val url = "https://app.modzy.com/api/jobs"
        val body = JSONObject()
        val body2 = JSONObject()

        body2.put("identifier", "m8z2mwe3pt")
        body2.put("version", "0.0.1")
        body.put("model", body2)

        val body4 = JSONObject()
        body4.put("input.txt", text)

        val body3 = JSONObject()
        body3.put("my-input", body4)

        val body5 = JSONObject()
        body5.put("type", "text")
        body5.put("sources", body3)

        //final body
        body.put("input", body5)

        Log.i("Request Body", body.toString())

//    Calling Text-Topic Modelling API Call for JobId generation
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        var response = ""
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                response = it.getString("jobIdentifier")
                Log.i("Text-Topic Modelling JobId", response)
//                Handler().postDelayed({ getTopics(response) }, 3000)
                getStatus2(response)
            }, {
                Log.i("Text-Topic Modelling Network Call Failed", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
    }

    //    Function to check if job is completed
    private fun getStatus2(response: String) {
        var outputText = ""
        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/jobs/$response", null,
            {
                outputText = it.getString("status")

//                Checking job status every 10 sec
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
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue2.add(req)
    }

    //    Extracting the topics from Text-Topic Modelling Job-Id
    fun getTopics(response: String) {
        Log.i("status", response)

        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
                    .getJSONArray("results.json")
                Log.i("topics", topics.toString())

//                Constructing Tagged Response
                var output = StringBuilder()
                for (i in 0 until topics.length())
                    output.append("#").append(topics[i]).append(" ")

                Log.i("Tagged Response", output.toString())
//progress bar stops
                progressBar.visibility = View.GONE
//                Printing the Tagged Response on Screen
                viewOfLayout3rd.findViewById<TextView>(R.id.textView2).text = output

//                Copy-to-Clipboard button for ease
                viewOfLayout3rd.button4.setOnClickListener {
                    copy_to_clipboard(output.toString())
                }
            }, {
                Log.i(
                    "Result Extraction failed from Text-Topic Modelling JobId",
                    it.message.toString()
                )
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue2.add(req)
    }

    //    Function to copy tagged response to clipboard
    private fun copy_to_clipboard(topics: String) {
        val clipboard =
            ContextCompat.getSystemService(
                requireContext(),
                ClipboardManager::class.java
            ) as ClipboardManager
        val clip = ClipData.newPlainText("label", topics)
        clipboard!!.setPrimaryClip(clip)
    }

}