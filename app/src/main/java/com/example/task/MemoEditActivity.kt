package com.example.task

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.task.databinding.ActivityMemoEditBinding

class MemoEditActivity : AppCompatActivity() {
    private var memoPosition: Int = -1  // 메모 위치 기본값

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMemoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 새 메모가 아닌 기존 메모라면 내용 불러옴
        val memoContent = intent.getStringExtra(MemoActivity.EXTRA_MEMO_CONTENT)  // 내용 불러오기
        memoPosition = intent.getIntExtra(MemoActivity.EXTRA_MEMO_POSITION, -1)  // 위치 불러오기

        binding.editText.setText(memoContent)  // 가져온 내용

        binding.save.setOnClickListener {  // 저장 버튼
            val resultIntent = Intent()
            resultIntent.putExtra(MemoActivity.EXTRA_MEMO_CONTENT, binding.editText.text.toString())
            resultIntent.putExtra(MemoActivity.EXTRA_MEMO_POSITION, memoPosition)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.delete.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(MemoActivity.EXTRA_MEMO_POSITION, memoPosition)
            setResult(MemoActivity.RESULT_DELETE, resultIntent)
            finish()
        }

        binding.back.setOnClickListener{  // 뒤로가기 버튼
            finish()
        }
    }
}
