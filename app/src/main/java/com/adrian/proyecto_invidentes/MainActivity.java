package com.adrian.proyecto_invidentes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adrian.proyecto_invidentes.bluetooth.bluetooth_conexion;
import com.adrian.proyecto_invidentes.voz.emitir_voz;
import com.adrian.proyecto_invidentes.voz.recibir_voz;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {


    public static final Locale spanish = new Locale("es", "ES");

    private bluetooth_conexion btn_con;

    private recibir_voz re;

    private emitir_voz em;

    private int id_recibir_1 = 1;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    private String numero_celular = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_con = new bluetooth_conexion("HC-05", this);
        em = new emitir_voz(this, spanish);
        re = new recibir_voz();

        ConstraintLayout vista = findViewById(R.id.vista);
        startTimer();

        vista.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    em.hablar("Elija Accion");

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            re.invocar(MainActivity.this, id_recibir_1, spanish);
                        }
                    }, 1200);


                    // do your work here
                    return true;
                } else {
                    return false;
                }

            }


        });


    }


    @Override
    public void onDestroy() {
        em.onDestroy();
        stopTimer();


        try {
            if (btn_con.status()) {
                btn_con.desconectar();
            }
        } catch (Exception ex) {
            Log.i("test", "Error al desconectar");
        }


        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> matches = re.resultados(requestCode, resultCode, data, id_recibir_1);

        if (matches != null && !matches.isEmpty()) {

            Boolean valor=false;

            for (String data_text : matches) {
                Log.i("test", data_text.toLowerCase());

                valor = acciones(data_text.toLowerCase());

                if(valor)
                {
                    break;
                }


            }

            if(!valor)
            {
                em.hablar("Orden incorrecta");
            }

        } else {
            em.hablar("Intente Nuevamente");
        }
    }

    public boolean acciones(String data) {
        boolean es_correcto = false;

        Log.i("test", data);

        switch (data) {
            case "conectar": {
                encender();
                es_correcto = true;
                break;
            }
            case "desconectar": {
                apagar();
                es_correcto = true;
                break;
            }
            case "ayuda": {
                String ayuda = "Comandos conectar , desconectar, ubicar, enviar ubicacion";
                em.hablar(ayuda);
                es_correcto = true;
                break;
            }
            case "enviar ubicacion": {

                if(TextUtils.isEmpty(numero_celular))
                {
                    em.hablar("Telefono no ingresado");
                }
                else
                {
                    sendLongSMS(numero_celular,"Te envio mi ubicacion, he tenido un problema : https://www.google.com/maps/search/40.7127837,-74.0059413");
                    em.hablar("Ubicacion Enviada al " + numero_celular);
                }



                es_correcto = true;
                break;


            }
            default: {

                String celular = data.replaceAll("\\s","");
                Log.i("cell",celular);


                if(isValid(celular))
                {
                    em.hablar("Numero Correcto y Guardado");
                    numero_celular = celular;

                    es_correcto = true;
                }


                break;
            }
        }

        return es_correcto;
    }


    public static boolean isValid(String s)
    {


        if (s.matches("^(?=(?:[8-9]){1})(?=[0-9]{8}).*")) {
            return true;
        }
        else {
            return false;
        }
    }

    public void encender() {
        btn_con.iniciar();
        btn_con.buscar_host("HC-05");

        try {
            btn_con.conectar();
            em.hablar("Conectado");
        } catch (Exception ex) {
            em.hablar("Error al conectar");
        }
    }

    public void apagar() {
        try {
            btn_con.desconectar();
            em.hablar("Desconectado");
        } catch (Exception ex) {
            em.hablar("Error al desconectar");
        }
    }


    //To stop timer
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    //To start timer
    private void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {


                        if (btn_con.status()) {
                            String data_string = btn_con.get_data();
                            if (data_string != null && !data_string.isEmpty()) {

                                data_string = data_string.replaceAll("[^0-9]+", "");
                                data_string = data_string.trim();

                                try {
                                    int data = Integer.parseInt(data_string);

                                    if (data < 30) {
                                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        vib.vibrate(400);

                                        if (!em.repeatTTS.isSpeaking()) {
                                            em.hablar("Objeto colisionable");
                                        }
                                    }

                                    //Log.i("test", data + "");
                                } catch (Exception ex) {

                                    //Log.i("test", "Error int");
                                }

                            }
                        }

                    }
                });
            }
        };
        timer.schedule(timerTask, 500, 500);
    }


    public void sendLongSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            /*Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();*/
        } catch (Exception ex) {
            /*Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();*/
            ex.printStackTrace();
        }
    }

}
