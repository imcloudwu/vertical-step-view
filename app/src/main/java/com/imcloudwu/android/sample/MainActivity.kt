package com.imcloudwu.android.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imcloudwu.android.component.VerticalStepView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        VerticalStepView().test()
    }
}