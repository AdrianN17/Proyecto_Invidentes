package com.adrian.proyecto_invidentes.sqlite_controlador;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.adrian.proyecto_invidentes.sqlite_conexion.conexion;
import com.adrian.proyecto_invidentes.sqlite_modelo.configuracion;

public class controlador_sqlite extends SQLiteOpenHelper {

    public controlador_sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public controlador_sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public controlador_sqlite(Context context, String name, int version, SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    public boolean actualizar_mi_telefono()
    {
        String sql = "update datos_guardados set mi_telefono = ?;";

        return false;
    }

    public boolean actualizar_mi_distancia()
    {
        String sql = "update datos_guardados set mi_distancia = ?;";

        return false;
    }

    public boolean actualizar_mi_contacto()
    {
        String sql = "update datos_guardados set mi_contacto = ?;";

        return false;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
