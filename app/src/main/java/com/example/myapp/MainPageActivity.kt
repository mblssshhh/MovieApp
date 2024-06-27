package com.example.myapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPageActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainpage)

        Log.d("MainPageActivity", "onCreate: Initializing views and adapters")

        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        if (username == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewPager = findViewById(R.id.viewPager)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    Log.d("MainPageActivity", "Selected Home item")
                    viewPager.currentItem = 0
                }
                R.id.profile -> {
                    Log.d("MainPageActivity", "Selected Profile item")
                    viewPager.currentItem = 1
                }
                R.id.history -> {
                    Log.d("MainPageActivity", "Selected History item")
                    viewPager.currentItem = 2
                }
                R.id.settings -> {
                    Log.d("MainPageActivity", "Selected Settings item")
                    AlertDialog.Builder(this)
                        .setTitle("Exit from 2Movie")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes") { dialog, which ->
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("MainPageActivity", "ViewPager page selected: $position")
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        Log.d("MainPageActivity", "onCreate: Initialization complete")
    }
}

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> FriendFragment()
            2 -> HistoryFragment()
            else -> HomeFragment()
        }
    }
}
