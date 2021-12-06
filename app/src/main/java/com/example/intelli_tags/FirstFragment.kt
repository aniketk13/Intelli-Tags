package com.example.intelli_tags

import android.content.ClipboardManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_first.view.*
import org.json.JSONObject
import java.lang.StringBuilder
import android.content.ClipData
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.fragment_first.*

class FirstFragment : Fragment(R.layout.fragment_first) {

    lateinit var viewOfLayout: View
    lateinit var progressBar: ProgressBar
    lateinit var ai: ApplicationInfo
    private lateinit var output: StringBuilder
    private lateinit var response: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_first, container, false)

//        Declaring Application Info to retrieve and use the API Keys
        ai = viewOfLayout.context.packageManager
            .getApplicationInfo(viewOfLayout.context.packageName, PackageManager.GET_META_DATA)

        progressBar = viewOfLayout.findViewById(R.id.progressBar)

//        If search button is clicked
        viewOfLayout.searchTags.setOnClickListener {
            val text = viewOfLayout.TextInput.text.toString()
            if (text == "")
                Toast.makeText(viewOfLayout.context, "Please put a valid input", Toast.LENGTH_SHORT)
                    .show()
            else {
                progressBar.visibility = View.VISIBLE
                viewOfLayout.searchTags.isEnabled = false
                viewOfLayout.copyButton.isEnabled = false
                viewOfLayout.shareButton.isEnabled = false
                processText(text)
            }
        }

//        If copy to clipboard button is clicked
        viewOfLayout.copyButton.setOnClickListener {
            val text = viewOfLayout.TextInput.text.toString()
            if (text == "")
                Toast.makeText(viewOfLayout.context, "Please put a valid input", Toast.LENGTH_SHORT)
                    .show()
            else if (resultTopics.text == "")
                Toast.makeText(viewOfLayout.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                copy_to_clipboard(output.toString())
        }

//        If share button is clicked
        viewOfLayout.shareButton.setOnClickListener {
            val text = viewOfLayout.TextInput.text.toString()
            if (text == "")
                Toast.makeText(viewOfLayout.context, "Please put a valid input", Toast.LENGTH_SHORT)
                    .show()
            else if (resultTopics.text == "")
                Toast.makeText(viewOfLayout.context, "Generate the tags!", Toast.LENGTH_SHORT)
                    .show()
            else
                shareText(output.toString())
        }

        return viewOfLayout
    }

    //    Function to process the text and create a job-id
    fun processText(text: String) {

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

        val queue = Volley.newRequestQueue(viewOfLayout.context)
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                response = it.getString("jobIdentifier")
                getStatus()

                Log.i("Text-Topic Modelling Job-Id", response)
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
    private fun getStatus() {
        var outputText = ""
        val queue2 = Volley.newRequestQueue(viewOfLayout.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/jobs/$response", null,
            {
                outputText = it.getString("status")

//                Checking job status every 0.5 sec
                Handler().postDelayed({
                    if (outputText == "COMPLETED") {
                        if (it.getString("failed").equals("1"))
                        {
                            Toast.makeText(
                                viewOfLayout.context,
                                "Enter a valid text",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility=View.GONE
                            viewOfLayout.searchTags.isEnabled = true
                            viewOfLayout.copyButton.isEnabled = true
                            viewOfLayout.shareButton.isEnabled = true
                        }
                        else
//                        sending job id to extract the topics
                            getTopics(response)
                    } else
                        getStatus()
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

        val queue2 = Volley.newRequestQueue(viewOfLayout.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
                    .getJSONArray("results.json")
                Log.i("topics", topics.toString())

                output = StringBuilder()
                for (i in 0 until topics.length())
                    output.append("#").append(topics[i]).append(" ")

                //progress bar stops
                progressBar.visibility = View.GONE
                viewOfLayout.searchTags.isEnabled = true
                viewOfLayout.copyButton.isEnabled = true
                viewOfLayout.shareButton.isEnabled = true
                Log.i("topics", output.toString())
                viewOfLayout.findViewById<TextView>(R.id.resultTopics).text = output

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

    //function to copy the text to clipboard
    private fun copy_to_clipboard(topics: String) {
        val textToCopy = topics
        val clipboard =
            getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager
        val clip = ClipData.newPlainText("label", textToCopy)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(viewOfLayout.context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
    }


}