package com.adrian.proyecto_invidentes;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.adrian.proyecto_invidentes.bluetooth.bluetooth_conexion;
import com.adrian.proyecto_invidentes.sqlite_controlador.controlador_sqlite;
import com.adrian.proyecto_invidentes.sqlite_modelo.configuracion;
import com.adrian.proyecto_invidentes.voz.emitir_voz;
import com.adrian.proyecto_invidentes.voz.recibir_voz;



public class MainActivity extends AppCompatActivity {


    public static final Locale spanish = new Locale("es", "ES");

    private bluetooth_conexion btn_con;

    private recibir_voz re;

    private emitir_voz em;

    private int id_recibir_1 = 1;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    private String mi_numero_emergencia = "";
    private String mi_numero_celular = "";

    public float latitud = 0;
    public float longitud = 0;
    public float max_distancia = 0;

    public controlador_sqlite controlador ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlador = new controlador_sqlite(MainActivity.this);

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


        jalar_data();
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
            case "mi distancia":
            {
                em.hablar("mi distancia es de " + max_distancia + " centimetros");
                es_correcto = true;
                break;
            }
            case "mi teléfono personal":
            {
                if(!mi_numero_celular.equals(""))
                {
                    em.hablar("Mi telefono es " + mi_numero_celular);
                }
                else
                {
                    em.hablar("telefono personal vacio");
                }


                es_correcto = true;
                break;
            }
            case "mi teléfono de emergencia":
            {
                if(!mi_numero_emergencia.equals(""))
                {
                    em.hablar("mi telefono de emergencia es " + mi_numero_emergencia);
                }
                else
                {
                    em.hablar("telefono de emergencia vacio");
                }

                es_correcto = true;

                break;
            }
            case "enviar ubicacion":
            {

                if(TextUtils.isEmpty(mi_numero_emergencia))
                {
                    em.hablar("Telefono no ingresado");
                }
                else
                {
                    String cadena =  String.format("Te envio mi ubicacion, he tenido un problema : https://www.google.com/maps/search/%f,%f",latitud,longitud);

                    sendLongSMS(mi_numero_emergencia,cadena);
                    em.hablar("Ubicacion Enviada al " + mi_numero_emergencia);
                }

                es_correcto = true;
                break;

            }
            default: {


                es_correcto = comandos_con_datos(data);


                break;
            }
        }

        return es_correcto;
    }

    public boolean comandos_con_datos(String data)
    {
        if(data.contains("distancia "))
        {
            String data_procesada = data.replaceAll("distancia ", "");
            data_procesada = data_procesada.trim();
            data_procesada = data_procesada.replaceAll("\\s","");

            try
            {
                max_distancia = Float.parseFloat(data_procesada);
                controlador.actualizar_mi_distancia(max_distancia);
                em.hablar("distancia actualizada");


                return true;
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else if(data.contains("teléfono de emergencia "))
        {
            String data_procesada = data.replaceAll("teléfono de emergencia ", "");
            data_procesada = data_procesada.trim();
            data_procesada = data_procesada.replaceAll("\\s","");

            Log.i("validado",data_procesada);


            if(isValid(data_procesada))
            {

                mi_numero_emergencia = data_procesada;
                controlador.actualizar_mi_contacto(mi_numero_emergencia);
                em.hablar("Numero Correcto y Guardado");
                return true;
            }
            else
            {
                Log.i("validado","data no guardada");
                return false;
            }
        }
        else if(data.contains("teléfono personal "))
        {
            String data_procesada = data.replaceAll("teléfono personal ", "");
            data_procesada = data_procesada.trim();
            data_procesada = data_procesada.replaceAll("\\s","");

            Log.i("validado",data_procesada);

            if(isValid(data_procesada))
            {

                mi_numero_celular = data_procesada;
                controlador.actualizar_mi_telefono(mi_numero_celular);
                em.hablar("Numero Correcto y Guardado");

                return true;
            }
            else
            {
                Log.i("validado","data no guardada");
                return false;
            }
        }
        return false;
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

                                procesar_data(data_string);

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

    public void procesar_data(String data_recibida)
    {

        Log.i("texto recibido", data_recibida);

        String data_nueva =  data_recibida.trim();

        String[] parts = data_nueva.split("_");

        try {

            float distancia = Float.parseFloat(parts[0]);
            latitud = Float.parseFloat(parts[1]);
            longitud = Float.parseFloat(parts[2]);


            if (distancia < max_distancia) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(400);

                if (!em.repeatTTS.isSpeaking()) {
                    em.hablar("Objeto colisionable");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void jalar_data()
    {
        configuracion conf  = controlador.getData();

        mi_numero_celular = conf.getMi_telefono();
        mi_numero_emergencia = conf.getMi_contacto();
        max_distancia = conf.getMi_distancia();
    }
}