package com.example.intelli_tags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            getResult(text)
        }
        return viewOfLayout
    }

    private fun getResult(text: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://app.modzy.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create Service
        val service = retrofit.create(APIService::class.java)
        val body= JSONObject()
        val body2= JSONObject()
        body2.put("identifier","m8z2mwe3pt")
        body2.put("version","0.0.1")
        body.put("model",body2)
        val body4=JSONObject()
//        body2.remove("identifier")
//        body2.remove("version")
        body4.put("input.txt", text)
        val body3=JSONObject()
        body3.put("my-input",body4)
        val body5=JSONObject()
//        body2.remove("input.txt")
        body5.put("type","text")
        body5.put("sources",body3)

        body.put("input",body5)
        Log.i("body",body.toString())


        CoroutineScope(Dispatchers.IO).launch {
            Log.i("hello","inside IO dispatcher")
            val jobId=service.getJobId(getHeaderMap(),body)
            withContext(Dispatchers.Main) {
                if (jobId.isSuccessful) {
                    Log.i("happening","successful")
                    val ans=jobId.body()
                    Log.i("jobid",ans.toString())
                }
                else{
                    Log.i("error",jobId.body().toString())
                    Log.i("error",jobId.raw().toString())
                }
            }
        }

    }
    private fun getHeaderMap():Map<String,String>{
        val headerMap= mutableMapOf<String,String>()
        headerMap["Authorization"]="ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
        headerMap["Content-Type"]="application/json"
        headerMap["Accept"]="application/json"
        headerMap["User-Agent"]="PostmanRuntime/7.28.4"
        return headerMap
    }
}