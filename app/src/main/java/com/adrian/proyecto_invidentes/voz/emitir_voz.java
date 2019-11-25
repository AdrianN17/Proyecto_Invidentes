package com.adrian.proyecto_invidentes.voz;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class emitir_voz {

    public TextToSpeech repeatTTS;

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
            modulo_hablar("Error");
        } else {
            modulo_hablar(text);
        }
    }

    private void modulo_hablar(String text)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            repeatTTS.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
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
