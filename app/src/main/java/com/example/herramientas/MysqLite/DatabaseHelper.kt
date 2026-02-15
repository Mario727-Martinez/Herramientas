package com.example.herramientas.MysqLite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Herramientas.db"
        private const val DATABASE_VERSION = 1

        // Tabla Herramientas
        const val TABLE_HERRAMIENTAS = "Herramientas"
        const val COLUMN_HERRAMIENTA_ID = "id"
        const val COLUMN_HERRAMIENTA_NOMBRE = "nombre"
        const val COLUMN_HERRAMIENTA_DESCRIPCION = "descripcion"
        const val COLUMN_HERRAMIENTA_ESPECIFICACIONES = "especificaciones"
        const val COLUMN_HERRAMIENTA_FOTO_URI = "foto_uri"
        const val COLUMN_HERRAMIENTA_ESTADO = "estado"

        // Tabla Tecnicos
        const val TABLE_TECNICOS = "Tecnicos"
        const val COLUMN_TECNICO_ID = "id"
        const val COLUMN_TECNICO_NOMBRE = "nombre"
        const val COLUMN_TECNICO_TELEFONO = "telefono"
        const val COLUMN_TECNICO_ESPECIALIDAD = "especialidad"

        // Tabla Asignaciones
        const val TABLE_ASIGNACIONES = "Asignaciones"
        const val COLUMN_ASIGNACION_ID = "id"
        const val COLUMN_ASIGNACION_HERRAMIENTA_ID = "herramienta_id"
        const val COLUMN_ASIGNACION_TECNICO_ID = "tecnico_id"
        const val COLUMN_ASIGNACION_FECHA_INICIO = "fecha_inicio"
        const val COLUMN_ASIGNACION_FECHA_FIN = "fecha_fin"
        const val COLUMN_ASIGNACION_FECHA_DEVOLUCION = "fecha_devolucion"
        const val COLUMN_ASIGNACION_NOTAS_ENTREGA = "notas_entrega"
        const val COLUMN_ASIGNACION_FOTO_ENTREGA_URI = "foto_entrega_uri"
        const val COLUMN_ASIGNACION_FOTO_DEVOLUCION_URI = "foto_devolucion_uri"

        private const val CREATE_TABLE_HERRAMIENTAS = """
            CREATE TABLE IF NOT EXISTS $TABLE_HERRAMIENTAS (
                $COLUMN_HERRAMIENTA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HERRAMIENTA_NOMBRE TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_DESCRIPCION TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_ESPECIFICACIONES TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_FOTO_URI TEXT,
                $COLUMN_HERRAMIENTA_ESTADO TEXT NOT NULL DEFAULT 'DISPONIBLE'
            );
            """

        private const val CREATE_TABLE_TECNICOS = """
            CREATE TABLE IF NOT EXISTS $TABLE_TECNICOS (
                $COLUMN_TECNICO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TECNICO_NOMBRE TEXT NOT NULL,
                $COLUMN_TECNICO_TELEFONO TEXT,
                $COLUMN_TECNICO_ESPECIALIDAD TEXT
            );
            """

        private const val CREATE_TABLE_ASIGNACIONES = """
            CREATE TABLE IF NOT EXISTS $TABLE_ASIGNACIONES (
                $COLUMN_ASIGNACION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ASIGNACION_HERRAMIENTA_ID INTEGER NOT NULL,
                $COLUMN_ASIGNACION_TECNICO_ID INTEGER NOT NULL,
                $COLUMN_ASIGNACION_FECHA_INICIO TEXT NOT NULL,
                $COLUMN_ASIGNACION_FECHA_FIN TEXT NOT NULL,
                $COLUMN_ASIGNACION_FECHA_DEVOLUCION TEXT,
                $COLUMN_ASIGNACION_NOTAS_ENTREGA TEXT,
                $COLUMN_ASIGNACION_FOTO_ENTREGA_URI TEXT,
                $COLUMN_ASIGNACION_FOTO_DEVOLUCION_URI TEXT,
                FOREIGN KEY($COLUMN_ASIGNACION_HERRAMIENTA_ID) REFERENCES $TABLE_HERRAMIENTAS($COLUMN_HERRAMIENTA_ID),
                FOREIGN KEY($COLUMN_ASIGNACION_TECNICO_ID) REFERENCES $TABLE_TECNICOS($COLUMN_TECNICO_ID)
            );
            """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_HERRAMIENTAS)
        db?.execSQL(CREATE_TABLE_TECNICOS)
        db?.execSQL(CREATE_TABLE_ASIGNACIONES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ASIGNACIONES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HERRAMIENTAS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TECNICOS")
        onCreate(db)
    }
}