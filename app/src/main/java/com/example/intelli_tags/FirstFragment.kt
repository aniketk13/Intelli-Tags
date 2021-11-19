package com.example.intelli_tags

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.intelli_tags.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass.
 */
class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("textshow","hello")
        val button:Button=
        binding.button.setOnClickListener {
            val text=binding.TextInputEditText.text
            Log.i("textshow",text.toString())
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }
}