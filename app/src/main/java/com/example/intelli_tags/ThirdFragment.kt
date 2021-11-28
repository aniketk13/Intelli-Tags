package com.example.intelli_tags

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.android.synthetic.main.fragment_second.view.*
import kotlinx.android.synthetic.main.fragment_third.*
import kotlinx.android.synthetic.main.fragment_third.view.*
import org.json.JSONObject
import java.lang.StringBuilder

class ThirdFragment : Fragment() {
    private lateinit var viewOfLayout3rd: View
    lateinit var urlOfVideo: String
    lateinit var nameOfVideo: String
    lateinit var conv: String
    lateinit var jobId: String
    lateinit var jobIdModzy: String
    lateinit var gettingVideoTextUrlSymbl: String
    private lateinit var outputTopicsModzy: StringBuilder

    lateinit var accessToken: String
    lateinit var filepath: String
    lateinit var progressBar: ProgressBar
    private var videoUri: Uri? = null
    lateinit var ai: ApplicationInfo
    private val tokenGenerateUrl = "https://api.symbl.ai/oauth2/token:generate"
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

        viewOfLayout3rd.searchTags3rd.setOnClickListener {
            if (viewOfLayout3rd.editTextVideoUrl == null || viewOfLayout3rd.editTextVideoName3rd == null)
                Toast.makeText(
                    viewOfLayout3rd.context,
                    "Please enter a valid url",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else {
                viewOfLayout3rd.searchTags3rd.isEnabled = false
                viewOfLayout3rd.copyButton2nd.isEnabled = false
                viewOfLayout3rd.shareButton2nd.isEnabled = false
                progressBar.visibility = View.VISIBLE

                urlOfVideo = viewOfLayout3rd.editTextVideoUrl.text.toString()
                nameOfVideo = viewOfLayout3rd.editTextVideoName3rd.text.toString()

                sendAppIdSymbl()
                //processText(text)
            }
        }

        //        If copy to clipboard button is clicked
        viewOfLayout3rd.copyButton3rd.setOnClickListener {
            val vidUrl = viewOfLayout3rd.editTextVideoUrl.text.toString()
            val vidName = viewOfLayout3rd.editTextVideoName3rd.text.toString()
            if (vidUrl == "" || vidName == "")
                Toast.makeText(
                    viewOfLayout3rd.context,
                    "Please put a valid input",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else if (textView3rd.text == "")
                Toast.makeText(viewOfLayout3rd.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                copy_to_clipboard(outputTopicsModzy.toString())
        }

//        If share button is clicked
        viewOfLayout3rd.shareButton3rd.setOnClickListener {
            val vidUrl = viewOfLayout3rd.editTextVideoUrl.text.toString()
            val vidName = viewOfLayout3rd.editTextVideoName3rd.text.toString()
            if (vidUrl == "" || vidName == "")
                Toast.makeText(
                    viewOfLayout3rd.context,
                    "Please put a valid input",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else if (textView3rd.text == "")
                Toast.makeText(viewOfLayout3rd.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                shareText(outputTopicsModzy.toString())
        }

        return viewOfLayout3rd
    }

    //sending app id to symbl request
    private fun sendAppIdSymbl() {
        val parameters = JSONObject()
        parameters.put("type", "application")
        parameters.put("appId", "433071396e786a467845724a556455696a6b6756343767556e5667425a4b7136")
        parameters.put(
            "appSecret",
            "6f5f5141456d564c5276335549517876424d494b42624334336b633234735935363652764c774372566e5949305f4c6e6d587672366879367673703636676d4b"
        )

        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = JsonObjectRequest(Request.Method.POST, tokenGenerateUrl, parameters,
            {
                accessToken = it.getString("accessToken")
                getConvIDSymbl()

            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error", Toast.LENGTH_SHORT).show()
            })
        queue.add(req)
    }

    private fun getConvIDSymbl() {
        val body = JSONObject()
        body.put("url", urlOfVideo)
        body.put("name", nameOfVideo)
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.POST, "https://api.symbl.ai/v1/process/video/url", body,
            {

                //getting the conversation id
                conv = it.getString("conversationId")
                jobId = it.getString("jobId")

                Log.i("VideoProcess", conv)
                Log.i("VideoProcess", jobId)

                gettingVideoTextUrlSymbl =
                    "https://api.symbl.ai/v1/conversations/$conv/messages?sentiment=true"

                Log.i("VideoProcess", gettingVideoTextUrlSymbl)

                getStatusSymblResponse(jobId, gettingVideoTextUrlSymbl)
            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error in video", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $accessToken")
                headers.put("Content-Type", "application/json")
                return headers
            }
        }
        queue.add(req)
    }

    private fun sendAppId() {
        val parameters = JSONObject()
        parameters.put("type", "application")
        parameters.put("appId", "${ai.metaData["SymblAppId"]}")
        parameters.put(
            "appSecret",
            "${ai.metaData["SymblAppSecret"]}"
        )

        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = JsonObjectRequest(
            Request.Method.POST, "https://api.symbl.ai/oauth2/token:generate", parameters,
            {
                accessToken = it.getString("accessToken")
                getConversationId()

            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error", Toast.LENGTH_SHORT).show()
            })
        queue.add(req)
    }

    private fun getConversationId() {
        val url = "https://api.symbl.ai/v1/process/video/url"
        val body = JSONObject()
        body.put("url", "https://modzybucket35738-dev.s3.us-east-2.amazonaws.com/public/Video.mp4")
        body.put("name", "General")
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                val response = it.getString("conversationId")
                val jobId = it.getString("jobId")
                Log.i("Conversation_id", response)
//                Webhook to be implemented

                getStatusSymblResponse(jobId, response)
//                Handler().postDelayed({ getMessage(response) }, 30000)
            },
            {
//                If network call fails
                Log.i("Video-Captioning API Call Failed", it.message.toString())
            }
        ) {
            //            Writing the required Headers for API call
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "Bearer $accessToken"
                return headerMap
            }
        }
        queue.add(req)
    }

    //webhook implemented for Symbl.ai
    private fun getStatusSymblResponse(jobId: String, symblUrlProcessVideo: String) {
        val endPt = "https://api.symbl.ai/v1/job/$jobId"
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val request = object : JsonObjectRequest(
            Method.GET, endPt, null, {
                val status = it.getString("status")
                if (status == "completed") {
                    Handler().postDelayed({ getResponseFromSymbl(symblUrlProcessVideo) }, 30000)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    Handler().postDelayed({
                        getStatusSymblResponse(jobId, symblUrlProcessVideo)
                    }, 500)
                }
            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $accessToken")
                return headers
            }
        }
        queue.add(request)
    }

    private fun getResponseFromSymbl(symblUrlProcessVideo: String) {
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, symblUrlProcessVideo, null,
            {
                Log.i("VideoProcess", "Response generated")
                Log.i("VideoProcess", it.toString())
                textOutputSymbl(it)
            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error in video", Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $accessToken")
                return headers
            }
        }
        queue.add(req)
    }

    private fun textOutputSymbl(response: JSONObject?) {
        Log.i("response", response.toString())
        val videoTextSymbl: String = ""
        val messages = response?.getJSONArray("messages")
        if (messages != null) {
            for (i in 0 until messages.length()) {
                val obj = messages.getJSONObject(i)
                val text = obj.getString("text")
                videoTextSymbl + text
            }
        }
        Log.i("Final Body Symbl", videoTextSymbl)
        processTextModzy(videoTextSymbl)
    }

    fun processTextModzy(text: String) {

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

        Log.i("body", body.toString())

        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                jobIdModzy = it.getString("jobIdentifier")
                getStatusModzy()

                Log.i("Text-Topic Modelling Job-Id", jobIdModzy)
            },
            {
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
    private fun getStatusModzy() {
        var outputText = ""
        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/jobs/$jobIdModzy", null,
            {
                outputText = it.getString("status")

//                Checking job status every 0.5 sec
                Handler().postDelayed({
                    if (outputText == "COMPLETED") {
//                        sending job id to extract the topics
                        getTopics(jobIdModzy)
                    } else
                        getStatusModzy()
                }, 500)

            }, {
                Toast.makeText(requireContext(), "Operation Failed", Toast.LENGTH_SHORT).show()
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

    //getting the topics of the text
    fun getTopics(response: String) {
        Log.i("status", response)

        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
                    .getJSONArray("results.json")
                Log.i("topics", topics.toString())

                outputTopicsModzy = StringBuilder()
                for (i in 0 until topics.length())
                    outputTopicsModzy.append("#").append(topics[i]).append(" ")

                //progress bar stops
                progressBar.visibility = View.GONE
                viewOfLayout3rd.searchTags3rd.isEnabled = true
                viewOfLayout3rd.copyButton3rd.isEnabled = true
                viewOfLayout3rd.shareButton3rd.isEnabled = true
                Log.i("topics", outputTopicsModzy.toString())
                viewOfLayout3rd.findViewById<TextView>(R.id.textView3rd).text = outputTopicsModzy

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

    //share button implementation
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


//    //    This runs just after the video is selected to check if it was a success
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        progressBar.visibility = View.VISIBLE
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
//            videoUri = data?.data
//            val returnCursor: Cursor? =
//                videoUri?.let { requireContext().contentResolver.query(it, null, null, null, null) }
//            try {
//                val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                returnCursor.moveToFirst()
//                val fileName = returnCursor.getString(nameIndex)
//                Log.i("hello", "file name : $fileName")
//            } catch (e: Exception) {
//                Log.i(ContentValues.TAG, "error: ", e)
//                //handle the failure cases here
//            } finally {
//                returnCursor?.close()
////                if (returnCursor != null) {
////                    returnCursor.close()
////                }
//            }
//            Log.i("Video Uri", videoUri.toString())
//            val uriPathHelper = URIPathHelper()
//            filepath = uriPathHelper.getPath(viewOfLayout3rd.context, videoUri).toString()
//            Log.i("path", filepath.toString())
////            Calling this function to upload video to AWS
//            uploadVideoToS3(videoUri)
//        }
//    }
//
//    //    Function to upload the selected video to AWS
//    private fun uploadVideoToS3(videoUri: Uri?) {
//        val stream = videoUri?.let { requireContext().contentResolver.openInputStream(it) }
//        val options =
//            StorageUploadInputStreamOptions.builder().accessLevel(StorageAccessLevel.PUBLIC)
//                .build()
////    if video uri is not null then uploading the video
//        if (stream != null) {
//            Amplify.Storage.uploadInputStream("Video.mp4", stream, options, {
//                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")
//                sendAppId()
////                Amplify.Storage.getUrl("public/Video.mp4",
////                    {
////                        Log.i("Object Url", "${it.url}")
////                        getConversationId()},
////                    },
////                    {
////                    Log.i("Video not found","Error")
////                })
////                After completion, starting the API network call to get the job id for video captioning
////                getJobId()
////                Amplify.Storage.getUrl("public/Video.mp4")
////                getConversationId(${it.url}.toString())
//            }, {
//                Log.e("MyAmplifyApp", "Upload failed", it)
//            })
//        }
//    }
//
//
//    fun getMessage(convId: String) {
//        val url = "https://api.symbl.ai/v1/conversations/$convId/messages"
//        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
//        var response = JSONArray()
//        var ans = StringBuilder()
//        val req = object : JsonObjectRequest(
//            Method.GET, url, null,
//            {
//                response = it.getJSONArray("messages")
//                for (i in 0 until response.length() step 1)
////                    Log.i("Response$i",response.getJSONObject(i).getString("text"))
//                    ans.append(response.getJSONObject(i).getString("text")).append(" ")
//
//                Log.i("Video-Captioning-JobId", ans.toString())
//                processText(ans.toString())
////                Calling getStatus to check if job is completed
////                getStatus(response)
//            },
//            {
////                If network call fails
//                Log.i("Video-Captioning API Call Failed", it.message.toString())
//            }
//        ) {
//            //            Writing the required Headers for API call
//            override fun getHeaders(): MutableMap<String, String> {
//                val headerMap = mutableMapOf<String, String>()
//                headerMap["Authorization"] = "Bearer $accessToken"
////                headerMap["Content-Type"] = "application/json"
////                headerMap["Accept"] = "application/json"
////                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
//                return headerMap
//            }
//        }
//        queue.add(req)
//    }
//
//
//    //    Sending extracted caption to Text-Topic Modelling API Call for jobId generation
//    private fun processText(text: String) {
//
//        val url = "https://app.modzy.com/api/jobs"
//        val body = JSONObject()
//        val body2 = JSONObject()
//
//        body2.put("identifier", "m8z2mwe3pt")
//        body2.put("version", "0.0.1")
//        body.put("model", body2)
//
//        val body4 = JSONObject()
//        body4.put("input.txt", text)
//
//        val body3 = JSONObject()
//        body3.put("my-input", body4)
//
//        val body5 = JSONObject()
//        body5.put("type", "text")
//        body5.put("sources", body3)
//
//        //final body
//        body.put("input", body5)
//
//        Log.i("Request Body", body.toString())
//
////    Calling Text-Topic Modelling API Call for JobId generation
//        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
//        var response = ""
//        val req = object : JsonObjectRequest(
//            Method.POST, url, body,
//            {
//                response = it.getString("jobIdentifier")
//                Log.i("Text-Topic Modelling JobId", response)
////                Handler().postDelayed({ getTopics(response) }, 3000)
//                getStatus2(response)
//            }, {
//                Log.i("Text-Topic Modelling Network Call Failed", it.message.toString())
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val headerMap = mutableMapOf<String, String>()
//                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
//                headerMap["Content-Type"] = "application/json"
//                headerMap["Accept"] = "application/json"
//                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
//                return headerMap
//            }
//        }
//        queue.add(req)
//    }
//
//    //    Function to check if job is completed
//    private fun getStatus2(response: String) {
//        var outputText = ""
//        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
//        val req = object : JsonObjectRequest(
//            Method.GET, "https://app.modzy.com/api/jobs/$response", null,
//            {
//                outputText = it.getString("status")
//
////                Checking job status every 10 sec
//                Handler().postDelayed({
//                    if (outputText == "COMPLETED")
////                        sending job id to extract the caption
//                        getTopics(response)
//                    else
//                        getStatus2(response)
//                }, 500)
//
//            }, {
//                Log.i("Job Status Failed", it.message.toString())
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val headerMap = mutableMapOf<String, String>()
//                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
//                headerMap["Content-Type"] = "application/json"
//                headerMap["Accept"] = "application/json"
//                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
//                return headerMap
//            }
//        }
//        queue2.add(req)
//    }
//
//    //    Extracting the topics from Text-Topic Modelling Job-Id
//    fun getTopics(response: String) {
//        Log.i("status", response)
//
//        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
//        val req = object : JsonObjectRequest(
//            Method.GET, "https://app.modzy.com/api/results/$response", null,
//            {
//                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
//                    .getJSONArray("results.json")
//                Log.i("topics", topics.toString())
//
////                Constructing Tagged Response
//                var output = StringBuilder()
//                for (i in 0 until topics.length())
//                    output.append("#").append(topics[i]).append(" ")
//
//                Log.i("Tagged Response", output.toString())
////progress bar stops
//                progressBar.visibility = View.GONE
////                Printing the Tagged Response on Screen
//                viewOfLayout3rd.findViewById<TextView>(R.id.textView3rd).text = output
//
////                Copy-to-Clipboard button for ease
//                viewOfLayout3rd.copyButton3rd.setOnClickListener {
//                    copy_to_clipboard(output.toString())
//                }
//
//                //share button
//                viewOfLayout3rd.shareButton3rd.setOnClickListener {
//                    shareText(output.toString())
//                }
//
//            }, {
//                Log.i(
//                    "Result Extraction failed from Text-Topic Modelling JobId",
//                    it.message.toString()
//                )
//            }) {
//            override fun getHeaders(): MutableMap<String, String> {
//                val headerMap = mutableMapOf<String, String>()
//                headerMap["Authorization"] = "ApiKey ${ai.metaData["ModzyAPIKey"]}"
//                headerMap["Content-Type"] = "application/json"
//                headerMap["Accept"] = "application/json"
//                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
//                return headerMap
//            }
//        }
//        queue2.add(req)
//    }
}