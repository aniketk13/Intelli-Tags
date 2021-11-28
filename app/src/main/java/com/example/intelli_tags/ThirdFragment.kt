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

        //                Generating the auth token
        sendAppIdSymbl()
        viewOfLayout3rd.searchTags3rd.setOnClickListener {
            urlOfVideo = viewOfLayout3rd.editTextVideoUrl.text.toString()
            if (urlOfVideo=="")
                Toast.makeText(
                    viewOfLayout3rd.context,
                    "Please enter a valid url",
                    Toast.LENGTH_SHORT
                )
                    .show()
            else {
                viewOfLayout3rd.searchTags3rd.isEnabled = false
                viewOfLayout3rd.copyButton3rd.isEnabled = false
                viewOfLayout3rd.shareButton3rd.isEnabled = false
                progressBar.visibility = View.VISIBLE
                getConvIDSymbl()

                //processText(text)
            }
        }

        //        If copy to clipboard button is clicked
        viewOfLayout3rd.copyButton3rd.setOnClickListener {
            val vidUrl = viewOfLayout3rd.editTextVideoUrl.text.toString()
            if (vidUrl == "")
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
            if (vidUrl == "")
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
        parameters.put("appId", "${ai.metaData["SymblAppId"]}")
        parameters.put(
            "appSecret",
            "${ai.metaData["SymblAppSecret"]}")


        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = JsonObjectRequest(Request.Method.POST, tokenGenerateUrl, parameters,
            {
                accessToken = it.getString("accessToken")
                Log.i("accessToken",accessToken)
//                getConvIDSymbl()

            }, {
                Toast.makeText(viewOfLayout3rd.context, "Error", Toast.LENGTH_SHORT).show()
            })
        queue.add(req)
    }

    private fun getConvIDSymbl() {
        val body = JSONObject()
        body.put("url", urlOfVideo)
        body.put("name", "General")
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
                    "https://api.symbl.ai/v1/conversations/$conv/messages"

                Log.i("VideoProcess", gettingVideoTextUrlSymbl)

                getStatusSymblResponse()
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

    //webhook implemented for Symbl.ai
    private fun getStatusSymblResponse() {
        val endPt = "https://api.symbl.ai/v1/job/$jobId"
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val request = object : JsonObjectRequest(
            Method.GET, endPt, null, {
                val status = it.getString("status")
                if (status == "completed") {
//                    Handler().postDelayed({ getResponseFromSymbl(gettingVideoTextUrlSymbl) }, 30000)
                    getResponseFromSymbl()
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    Handler().postDelayed({
                        getStatusSymblResponse()
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

    private fun getResponseFromSymbl() {
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, gettingVideoTextUrlSymbl, null,
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
        var videoTextSymbl: String = ""
        val messages = response?.getJSONArray("messages")
        if (messages != null) {
            for (i in 0 until messages.length()) {
                val obj = messages.getJSONObject(i)
                val text = obj.getString("text")
                videoTextSymbl=videoTextSymbl+text+" "
            }
        }
        Log.i("Final Body Symbl", videoTextSymbl)
        processTextModzy(videoTextSymbl)
    }

//    Function to process the text and generate a job-id
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
        Toast.makeText(viewOfLayout3rd.context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }

}