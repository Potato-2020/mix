package com.potato.asmmix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.potato.asmmix.dialog.GuideOneDialog
import com.potato.mix.MixExcludeMethod

class MainActivity : AppCompatActivity(), GuideOneDialog.GuideOneListener {

    private var dialog: GuideOneDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @MixExcludeMethod
    fun preText() {
        dialog = GuideOneDialog(this)
        dialog?.listener = this
        dialog?.show()
    }

    override fun guideOneListener() {
    }
}
