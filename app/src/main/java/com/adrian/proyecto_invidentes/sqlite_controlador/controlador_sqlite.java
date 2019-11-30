package com.adrian.proyecto_invidentes.sqlite_controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adrian.proyecto_invidentes.sqlite_modelo.configuracion;

public class controlador_sqlite extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "configuraciones.db";



    public controlador_sqlite(Context context) {

        super(context, DATABASE_NAME , null, 8);

        Log.i("creacion","objeto ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS datos\n" +
                "(\n" +
                "    mi_telefono varchar(9),\n" +
                "    mi_distancia int,\n" +
                "    mi_contacto varchar(9)\n" +
                ");\n" +
                "\n");

        db.execSQL("insert into datos values('',30,'')");



        Log.i("creacion","objeto creado");

    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS datos");
        onCreate(db);
    }



    public boolean actualizar_mi_telefono(String mi_telefono)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mi_telefono", mi_telefono);

        db.update("datos", contentValues,null,null);

        return true;
    }

    public boolean actualizar_mi_distancia(String mi_distancia)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mi_distancia", mi_distancia);
        db.update("datos", contentValues,null,null);
        return true;
    }

    public boolean actualizar_mi_contacto(String mi_contacto)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mi_contacto", mi_contacto);
        db.update("datos", contentValues,null,null);

        return true;
    }

    public void crear_data()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("mi_telefono", "");
        contentValues.put("mi_distancia",30);
        contentValues.put("mi_contacto", "");
        db.insert("datos", null, contentValues);
    }

    public configuracion getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from datos", null );


        String mi_telefono = res.getString(0);
        float mi_distancia = res.getFloat(1);
        String mi_contacto = res.getString(2);

        Log.i("midatos",mi_telefono);

        configuracion conf = new configuracion(mi_telefono,mi_distancia,mi_contacto);

        return conf;
    }


}
