package com.adrian.proyecto_invidentes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.adrian.proyecto_invidentes.bluetooth.bluetooth_conexion;
import com.adrian.proyecto_invidentes.voz.emitir_voz;
import com.adrian.proyecto_invidentes.voz.recibir_voz;

public class MainActivity extends AppCompatActivity
{
    public static final Locale spanish = new Locale("es", "ES");
    private bluetooth_conexion btn_con;

    private recibir_voz re;

    private emitir_voz em;

    private int id_recibir_1 = 1;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_con = new bluetooth_conexion("HC-05",this);
        em = new emitir_voz(this,spanish);
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
                            re.invocar(MainActivity.this,id_recibir_1,spanish);
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
    public void onDestroy(){
        em.onDestroy();
        stopTimer();


        try {
            if(btn_con.status())
            {
                btn_con.desconectar();
            }
        }
        catch (Exception ex)
        {
            Log.i("test","Error al desconectar");
        }


        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<String> matches = re.resultados(requestCode,resultCode,data,id_recibir_1);

        if(matches!=null && !matches.isEmpty())
        {
            for (String data_text : matches) {
                Log.i("test", data_text.toLowerCase());
                if(acciones(data_text.toLowerCase()))
                {
                    break;
                }
            }
        }
        else
        {
            em.hablar("Intente Nuevamente");
        }
    }

    public boolean acciones(String data)
    {
        boolean es_correcto=false;
        switch(data)
        {
            case "conectar":
                encender();
                es_correcto=true;
                break;
            case "desconectar":
                apagar();
                es_correcto=true;
                break;
            case "ayuda":
                String ayuda="Comandos conectar , desconectar";
                em.hablar(ayuda);
                break;
            default :
                em.hablar("Orden incorrecta");
                break;
        }

        return es_correcto;
    }

    public void encender()
    {
        btn_con.iniciar();
        btn_con.buscar_host("HC-05");

        try {
            btn_con.conectar();
            em.hablar("Conectado");
        }
        catch (Exception ex)
        {
            em.hablar("Error al conectar");
        }
    }

    public void apagar()
    {
        try {
            btn_con.desconectar();
            em.hablar("Desconectado");
        }
        catch (Exception ex)
        {
            em.hablar("Error al desconectar");
        }
    }



    //To stop timer
    private void stopTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
    }

    //To start timer
    private void startTimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){


                        if(btn_con.status())
                        {
                            String data_string = btn_con.get_data();
                            if(data_string != null && !data_string.isEmpty())
                            {

                                data_string = data_string.replaceAll("[^0-9]+", "");
                                data_string = data_string.trim();

                                //Log.i("test", data_string.length()+"");

                                try
                                {
                                    int data = Integer.parseInt(data_string);

                                    if(data<30)
                                    {
                                        Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        vib.vibrate(400);
                                    }

                                    Log.i("test",data+"");
                                }
                                catch(Exception ex)
                                {
                                    Log.i("test","Error int");
                                }

                            }
                        }

                    }
                });
            }
        };
        timer.schedule(timerTask, 500, 500);
    }

}
