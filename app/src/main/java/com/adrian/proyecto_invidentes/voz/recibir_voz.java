package com.adrian.proyecto_invidentes.voz;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class recibir_voz {

    public recibir_voz()
    {
    }

    public void invocar(Activity view,int id, Locale idioma)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, idioma);
        view.startActivityForResult(intent, id);
    }


    public ArrayList<String>  resultados(int requestCode, int resultCode, @Nullable Intent data, int id) {

        ArrayList<String> matches = null;

        if (resultCode == RESULT_OK && data != null) {
            if(requestCode==id) {
                matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            }

        } else {
            Log.i("test", "Intentelo nuevamente");
        }

        return matches;
    }
}
