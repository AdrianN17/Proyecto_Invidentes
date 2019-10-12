package com.adrian.proyecto_invidentes.voz;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.util.Locale;

public class emitir_voz {

    TextToSpeech repeatTTS;

    public emitir_voz(Activity view,final Locale idioma)
    {

        repeatTTS = new TextToSpeech(view, new TextToSpeech.OnInitListener() {

            public void onInit(int status) {
                repeatTTS.setLanguage(idioma);
                if (status == TextToSpeech.SUCCESS) {
                    hablar("Bienvenido");
                }
            }
        });
    }

    public void hablar(String text ) {
        if (text.length() == 0) {
            repeatTTS.speak("Error", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            repeatTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        }
    }

    public void onDestroy() {
        if (repeatTTS != null) {
            repeatTTS.stop();
            repeatTTS.shutdown();
        }
    }



}
