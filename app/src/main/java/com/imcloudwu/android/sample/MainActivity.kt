package com.imcloudwu.android.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.imcloudwu.android.component.VerticalStepView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val verticalStepView = findViewById<VerticalStepView>(R.id.verticalStepView)
        val resetBtn = findViewById<Button>(R.id.reset_btn)
        val nextBtn = findViewById<Button>(R.id.next_btn)

        val data = arrayListOf<String>()

        repeat(100) {
            data.add("Test step $it")
        }

        verticalStepView.setSteps(data)

        var index = -1

        resetBtn.setOnClickListener {
            index = -1
            verticalStepView.move(index)
        }

        nextBtn.setOnClickListener {
            index += 20
            verticalStepView.move(index)
        }
    }
}