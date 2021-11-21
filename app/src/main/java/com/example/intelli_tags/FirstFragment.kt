package com.example.intelli_tags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FirstFragment : Fragment(R.layout.fragment_first) {

    private lateinit var viewOfLayout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewOfLayout = inflater.inflate(R.layout.fragment_first, container, false)

        viewOfLayout.button.setOnClickListener {
            val text = viewOfLayout.TextInputEditText.text.toString()
            Log.i("text", text)
            processText(text)
        }
        return viewOfLayout
    }

    private fun processText(text: String) {

//        val text =
//            "API is the acronym for Application Programming Interface, which is a software intermediary that allows two applications to talk to each other. Each time you use an app like Facebook, send an instant message, or check the weather on your phone, you're using an API."
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
                Log.i("inside", "Inside Api Call")

                val response = it.getString("jobIdentifier")
                Log.i("identifier", response)
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()
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
}