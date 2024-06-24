package com.example.task

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.task.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle

    private val apiKey = "2cca3175b02f9f1954fa17863d3326f5"  // 내 api 키
    private val city = "Seoul"  // 도시 이름
    private val country = "KR"  // 국가 이름

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "🏠 HOME"

        toggle = ActionBarDrawerToggle(this, binding.drawer, R.string.drawer_open, R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()

        binding.mainDrawerView.setNavigationItemSelectedListener {  // 사이드 메뉴 선택 시
            val intent1 = Intent(this, MemoActivity::class.java)
            val intent2 = Intent(this, ToDoActivity::class.java)
            intent1.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT  // 액티비티 1개씩만 생성하도록
            intent2.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            when (it.itemId){
                R.id.draw_finish -> finishAffinity()  // 종료 메뉴
                R.id.memo -> {  // 메모 메뉴
                    startActivity(intent1)
                    binding.drawer.closeDrawer(binding.mainDrawerView)
                }
                R.id.toDo -> {  // 할 일 메뉴
                    startActivity(intent2)
                    binding.drawer.closeDrawer(binding.mainDrawerView)
                }
            }
            true
        }
        
        binding.refreshBtn.setOnClickListener {   // 새로고침 버튼 눌렀을 때
            fetchWeather()
        }

        fetchWeather()  // 액티비티 열렸을 때 처음 날씨 로딩
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchWeather() {  // 날씨 로딩 함수
        val urlString = "https://api.openweathermap.org/data/2.5/weather?q=$city,$country&appid=$apiKey&units=metric"  // url
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = URL(urlString).readText()
                val jsonObject = JSONObject(response)
                val main = jsonObject.getJSONObject("main")
                val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
                val wind = jsonObject.getJSONObject("wind")  // 바람 정보
                val rain = jsonObject.optJSONObject("rain")  // 비 정보
                val humidity = main.getInt("humidity")  // 습도 정보
                val temp = main.getDouble("temp")  // 온도 정보
                val tempMax = main.getDouble("temp_max")  // 최고 기온
                val tempMin = main.getDouble("temp_min")  // 최저 기온
                val windSpeed = wind.getDouble("speed")  // 바람에서 풍속 정보 추출
                val rainVolume = rain?.optDouble("1h") ?: 0.0  // 비 정보에서 강수량 추출

                launch(Dispatchers.Main) {  // 가져온 정보로 레이아웃 작성
                    binding.mainTemp.text = "$temp°C"
                    binding.maxMinTemp.text = "최고기온: $tempMax°C  최저기온: $tempMin°C"
                    binding.extraInfo.text = "풍속: ${windSpeed}m/s  강수량: ${rainVolume}mm  습도: $humidity%"

                    val iconCode = weather.getString("icon")  // 아이콘 가져오기
                    Glide.with(this@MainActivity)
                        .load("https://openweathermap.org/img/wn/$iconCode.png")
                        .into(binding.weatherIcon)  // 아이콘 주소에서 받아와 아이콘 레이아웃 그리기
                }
            } catch (e: Exception) {  // 예외처리
                e.printStackTrace()
            }
        }
    }
}