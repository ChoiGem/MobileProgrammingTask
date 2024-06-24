package com.example.task

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.task.databinding.ActivityToDoBinding
import com.google.android.material.navigation.NavigationView
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ToDoActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var binding: ActivityToDoBinding
    private val sharedPreferences by lazy { getSharedPreferences("ToDoPrefs", MODE_PRIVATE) }
    // 내용 저장을 위한 sharedPreferences 함수, ToDoPrefs를 통해 값 불러오기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToDoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "할 일"

        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val menu = navigationView.menu
        var menuItem = menu.findItem(R.id.toDo)
        menuItem.isEnabled = false // 메뉴 항목 비활성화

        binding.mainDrawerView.setNavigationItemSelectedListener {  // 사이드 메뉴 선택 시
            val intent = Intent(this, MemoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            when (it.itemId){
                R.id.draw_finish -> finishAffinity()  // 종료 메뉴
                R.id.memo -> {  // 메모 메뉴
                    binding.drawer.closeDrawer(binding.mainDrawerView)
                    startActivity(intent)
                }
            }
            true
        }


        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var selectedDate: String = dateFormat.format(Date())  // 날짜 설정

        selectedDate = dateFormat.format(binding.calenderView.date)  // 기본값으로 오늘 날짜
        loadTodoForDate(selectedDate)

        binding.calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->  // 날짜 선택 리스너
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            loadTodoForDate(selectedDate)
        }

        binding.saveBtn.setOnClickListener {  // 저장 버튼 눌렀을 때
            saveTodoForDate(selectedDate, binding.todoEditText.text.toString())  // 저장 함수
            Toast.makeText(applicationContext, "저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTodoForDate(date: String) {  // 내용 불러오기
        val todo = sharedPreferences.getString(date, "")
        binding.todoEditText.setText(todo)
    }

    private fun saveTodoForDate(date: String, todo: String) {  // 내용 저장하기
        sharedPreferences.edit().putString(date, todo).apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  // 홈 메뉴 추가
        menuInflater.inflate(R.menu.munu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        when (item.itemId){  // 홈 메뉴 눌렀을 때 홈으로
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                binding.drawer.closeDrawer(binding.mainDrawerView)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {  // 뒤로가기 버튼 눌렀을 때 홈으로
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
}