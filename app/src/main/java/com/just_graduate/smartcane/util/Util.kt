package com.just_graduate.smartcane.util

import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.just_graduate.smartcane.BaseApplication
import java.util.*

object Util {
    fun showToast(msg: String) {
        Toast.makeText(BaseApplication.applicationContext(), msg, Toast.LENGTH_LONG).show()
    }

    fun textToSpeech(text: String) {
        var tts: TextToSpeech? = null
        tts =
            TextToSpeech(BaseApplication.applicationContext(), TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result: Int? = tts?.setLanguage(Locale.KOREA)

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(
                            BaseApplication.applicationContext(),
                            "지원하지 않는 언어입니다",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
                    }
                }
            })
    }

}