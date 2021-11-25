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
import android.widget.ProgressBar

class FirstFragment : Fragment(R.layout.fragment_first) {

    lateinit var viewOfLayout: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_first, container, false)
        viewOfLayout.button.setOnClickListener {
            val text = viewOfLayout.TextInputEditText.text.toString()
            processText(text)
        }
        return viewOfLayout
    }

    //function to copy the text to clipboard
    private fun copy_to_clipboard(topics: String) {
        val textToCopy = topics
        val clipboard =
            getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager
        val clip = ClipData.newPlainText("label", textToCopy)
        clipboard!!.setPrimaryClip(clip)
    }

    fun processText(text: String) {

        //prgress bar
        val progressBar: ProgressBar = viewOfLayout.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

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
        var response = ""
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                response = it.getString("jobIdentifier")
                Handler().postDelayed({ getTopics(response) }, 3000)
                Log.i("identifier", response)
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
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
                var output = StringBuilder()
                for (i in 0 until topics.length())
                    output.append("#").append(topics[i]).append("\n")
                Log.i("topics", output.toString())
                viewOfLayout.findViewById<TextView>(R.id.resultTopics).text = output
                viewOfLayout.copy.setOnClickListener {
                    copy_to_clipboard(output.toString())
                }
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed second", Toast.LENGTH_SHORT)
                    .show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue2.add(req)
    }
}