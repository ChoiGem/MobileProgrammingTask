package com.example.task

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.task.databinding.ActivityMemoBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray

class MemoActivity : AppCompatActivity() {
    lateinit var binding: ActivityMemoBinding
    lateinit var toggle: ActionBarDrawerToggle  // 사이드 메뉴
    private val memos = mutableListOf<String>()  // 메모 저장용 문자열 배열
    private lateinit var adapter: ArrayAdapter<String>
    lateinit var extendedFab: ExtendedFloatingActionButton  // 추가 버튼
    private val sharedPreferences by lazy { getSharedPreferences("MemoPrefs", MODE_PRIVATE) }
    // 내용 저장을 위한 sharedPreferences 함수, MemoPrefs를 통해 값 불러오기

    companion object {  // 상수로 미리 값 지정해놓기
        const val REQUEST_CODE_ADD = 1  // 추가 버튼 눌렀을 경우와 목록 눌러서 수정할 경우 분리
        const val REQUEST_CODE_EDIT = 2
        const val RESULT_DELETE = 3
        const val EXTRA_MEMO_CONTENT = "EXTRA_MEMO_CONTENT"
        const val EXTRA_MEMO_POSITION = "EXTRA_MEMO_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "메모"

        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.main_drawer_view)
        val menu = navigationView.menu
        var menuItem = menu.findItem(R.id.memo)
        menuItem.isEnabled = false  // 메뉴 항목 비활성화(자기 자신 메뉴인 경우)
        extendedFab = findViewById(R.id.extended_action_bar)
        extendedFab.shrink()  // 추가버튼 축소된채로 실행

        binding.mainDrawerView.setNavigationItemSelectedListener {  // 사이드 메뉴 선택 시
            val intent = Intent(this, ToDoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            when (it.itemId){
                R.id.draw_finish -> finishAffinity()  // 종료 메뉴
                R.id.toDo -> {  // 할 일 메뉴
                    startActivity(intent)
                    binding.drawer.closeDrawer(binding.mainDrawerView)
                    extendedFab.shrink()
                }
            }
            true
        }

        loadMemos()  // 메모 불러오기
        adapter = ArrayAdapter(this, R.layout.list_item, R.id.textView, memos)
        binding.listView.adapter = adapter  // listView 목록 작성을 위한 어뎁터

        binding.extendedActionBar.setOnClickListener {  // 추가 버튼 눌렀을 때
            val intent = Intent(this, MemoEditActivity::class.java)
            when (binding.extendedActionBar.isExtended){  // 버튼 열려있으면 액티비티 실행
                true -> {
                    startActivityForResult(intent, REQUEST_CODE_ADD)
                }
                false -> binding.extendedActionBar.extend()  // 버튼 축소된 경우면 열기
            }
        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->  // 목록 눌렀을 때
            val intent = Intent(this, MemoEditActivity::class.java)
            intent.putExtra(EXTRA_MEMO_CONTENT, memos[position])  // 내용
            intent.putExtra(EXTRA_MEMO_POSITION, position)  // 위치(id)
            startActivityForResult(intent, REQUEST_CODE_EDIT)  // 메모편집 액티비티 실행
        }
    }

    private fun loadMemos() {  // (저장장치에) 저장해놓은 메모 문자열로 불러오기
        val jsonArrayString = sharedPreferences.getString("memos", "[]")
        val jsonArray = JSONArray(jsonArrayString)
        for (i in 0 until jsonArray.length()) {
            memos.add(jsonArray.getString(i))  // 문자열에 추가
        }
    }

    private fun saveMemos() {  // 메모 (저장장치 안에) 저장하기
        val jsonArray = JSONArray()
        for (memo in memos) {
            jsonArray.put(memo)
        }
        sharedPreferences.edit().putString("memos", jsonArray.toString()).apply()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  // (우상단) 홈 메뉴 추가
        menuInflater.inflate(R.menu.munu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        when (item.itemId){  // (우상단) 홈 메뉴 눌렀을 때 홈으로
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                binding.drawer.closeDrawer(binding.mainDrawerView)
                extendedFab.shrink()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {  // 뒤로가기 버튼 눌렀을 때 홈으로
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
        binding.drawer.closeDrawer(binding.mainDrawerView)
        extendedFab.shrink()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val memoContent = data.getStringExtra(EXTRA_MEMO_CONTENT) ?: return
            when (requestCode) {
                REQUEST_CODE_ADD -> {  // 메모 추가일 때
                    memos.add(memoContent)
                    adapter.notifyDataSetChanged()
                    saveMemos()
                }
                REQUEST_CODE_EDIT -> {  // 기존 메모일 때
                    val position = data.getIntExtra(EXTRA_MEMO_POSITION, -1)
                    if (position != -1) {
                        memos[position] = memoContent
                        adapter.notifyDataSetChanged()
                        saveMemos()
                    }
                }
            }
        } else if (resultCode == MemoActivity.RESULT_DELETE && data != null) {
          // 메모 에딧 액티비티에서 삭제 버튼 눌렸을 때
            val position = data.getIntExtra(EXTRA_MEMO_POSITION, -1)
            if (position != -1) {
                memos.removeAt(position)
                adapter.notifyDataSetChanged()
                saveMemos()
            }
        }
    }
}
