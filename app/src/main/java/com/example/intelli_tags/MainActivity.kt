package com.example.intelli_tags

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.intelli_tags.ui.main.SectionsPagerAdapter
import com.example.intelli_tags.databinding.ActivityMainBinding
import android.R
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater







class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        this.actionBar!!.setDisplayShowCustomEnabled(true)
//        this.actionBar!!.setDisplayShowTitleEnabled(false)
//
//        val inflator = LayoutInflater.from(this)
//        val v: View = inflator.inflate(R.layout.titleview, null)
//
////if you need to customize anything else about the text, do it here.
////I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
//
////if you need to customize anything else about the text, do it here.
////I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
//        (v.findViewById<View>(R.id.title) as TextView).text = this.title
//
////assign the view to the actionbar
//
////assign the view to the actionbar
//        this.actionBar!!.customView = v
//        supportActionBar?.apply {
//            // show custom title in action bar
//            customView = actionBarCustomTitle()
//            displayOptions = DISPLAY_SHOW_CUSTOM
//
//            setDisplayShowHomeEnabled(true)
//            setDisplayUseLogoEnabled(true)
//        }
        
        try {
//            Amplify startup
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(this)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

    }

//    private fun actionBarCustomTitle(): TextView {
//        return TextView(this).apply {
//            text = "Intelli-Tags"
//
//            val params = ActionBar.LayoutParams(
//                ActionBar.LayoutParams.WRAP_CONTENT,
//                ActionBar.LayoutParams.WRAP_CONTENT
//            )
//            // center align the text view/ action bar title
//            params.gravity = Gravity.CENTER_HORIZONTAL
//            layoutParams = params
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                setTextAppearance(
//                    android.R.style.TextAppearance_Material_Widget_ActionBar_Title
//                )
//            }else{
//                // define your own text style
//                val typeface = resources.getFont()
//                //or to support all versions use
//                //or to support all versions use
//                val typeface = ResourcesCompat.getFont(context, R.font.lobster)
//                textView.setTypeface(typeface)
//            }
//        }
//    }
}