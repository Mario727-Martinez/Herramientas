package com.example.herramientas.MysqLite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Clase auxiliar para gestionar la creación y actualización de la base de datos SQLite.
 * Define el esquema de las tablas y las operaciones iniciales.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Companion object para almacenar constantes relacionadas con la base de datos,
     * como el nombre, la versión y las definiciones de las tablas y columnas.
     */
    companion object {
        private const val DATABASE_NAME = "Herramientas.db"
        private const val DATABASE_VERSION = 1

        // --- Definiciones de la Tabla Herramientas ---
        const val TABLE_HERRAMIENTAS = "Herramientas"
        const val COLUMN_HERRAMIENTA_ID = "id"
        const val COLUMN_HERRAMIENTA_NOMBRE = "nombre"
        const val COLUMN_HERRAMIENTA_DESCRIPCION = "descripcion"
        const val COLUMN_HERRAMIENTA_ESPECIFICACIONES = "especificaciones"
        const val COLUMN_HERRAMIENTA_FOTO_URI = "foto_uri"
        const val COLUMN_HERRAMIENTA_ESTADO = "estado"

        // --- Definiciones de la Tabla Tecnicos ---
        const val TABLE_TECNICOS = "Tecnicos"
        const val COLUMN_TECNICO_ID = "id"
        const val COLUMN_TECNICO_NOMBRE = "nombre"
        const val COLUMN_TECNICO_TELEFONO = "telefono"
        const val COLUMN_TECNICO_ESPECIALIDAD = "especialidad"

        // --- Definiciones de la Tabla Asignaciones ---
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

        // Sentencias SQL para crear las tablas.
        private val CREATE_TABLE_HERRAMIENTAS = """
            CREATE TABLE IF NOT EXISTS $TABLE_HERRAMIENTAS (
                $COLUMN_HERRAMIENTA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HERRAMIENTA_NOMBRE TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_DESCRIPCION TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_ESPECIFICACIONES TEXT NOT NULL,
                $COLUMN_HERRAMIENTA_FOTO_URI TEXT,
                $COLUMN_HERRAMIENTA_ESTADO TEXT NOT NULL DEFAULT 'DISPONIBLE'
            );
            """

        private val CREATE_TABLE_TECNICOS = """
            CREATE TABLE IF NOT EXISTS $TABLE_TECNICOS (
                $COLUMN_TECNICO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TECNICO_NOMBRE TEXT NOT NULL,
                $COLUMN_TECNICO_TELEFONO TEXT,
                $COLUMN_TECNICO_ESPECIALIDAD TEXT
            );
            """

        private val CREATE_TABLE_ASIGNACIONES = """
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

    /**
     * Se llama cuando la base de datos se crea por primera vez.
     * Ejecuta las sentencias para crear las tablas e inserta los datos iniciales.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_HERRAMIENTAS)
        db?.execSQL(CREATE_TABLE_TECNICOS)
        db?.execSQL(CREATE_TABLE_ASIGNACIONES)

        // Inserta técnicos de ejemplo para que la app tenga datos al iniciar por primera vez.
        addTecnico(db, "Juan Pérez", "12345678", "Electricista")
        addTecnico(db, "Maria Rodriguez", "87654321", "Plomería")
        addTecnico(db, "Carlos Gómez", "11223344", "Mecánica")
    }

    /**
     * Método auxiliar para añadir un técnico a la base de datos durante la creación.
     */
    private fun addTecnico(db: SQLiteDatabase?, nombre: String, telefono: String, especialidad: String) {
        val values = ContentValues().apply {
            put(COLUMN_TECNICO_NOMBRE, nombre)
            put(COLUMN_TECNICO_TELEFONO, telefono)
            put(COLUMN_TECNICO_ESPECIALIDAD, especialidad)
        }
        db?.insert(TABLE_TECNICOS, null, values)
    }

    /**
     * Se llama cuando la versión de la base de datos cambia.
     * Para este proyecto, la estrategia simple es borrar todas las tablas y volver a crearlas.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ASIGNACIONES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HERRAMIENTAS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TECNICOS")
        onCreate(db)
    }
}