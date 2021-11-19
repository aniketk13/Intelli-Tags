package com.example.intelli_tags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_first.view.*

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
        }
        return viewOfLayout
    }
}