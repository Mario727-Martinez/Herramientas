package com.example.herramientas.MysqLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_FECHA_FIN
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_FECHA_INICIO
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_HERRAMIENTA_ID
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_ASIGNACIONES
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_HERRAMIENTAS

data class Herramienta(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val especificaciones: String,
    val fotoUri: String?
)

data class HerramientaInfo(
    val id: Long,
    val nombre: String,
    val especificaciones: String,
    val estado: String,
    val fechaFin: String?,
    val fechaDevolucion: String?,
    val tecnicoNombre: String?
)

class HerramientaDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun guardarHerramienta(nombre: String, descripcion: String, especificaciones: String, fotoUri: String?): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HERRAMIENTA_NOMBRE, nombre)
            put(DatabaseHelper.COLUMN_HERRAMIENTA_DESCRIPCION, descripcion)
            put(DatabaseHelper.COLUMN_HERRAMIENTA_ESPECIFICACIONES, especificaciones)
            put(DatabaseHelper.COLUMN_HERRAMIENTA_FOTO_URI, fotoUri)
        }

        return db.insert(TABLE_HERRAMIENTAS, null, values)
    }

    fun getHerramientasDisponibles(): List<Herramienta> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_HERRAMIENTAS,
            null,
            "${DatabaseHelper.COLUMN_HERRAMIENTA_ESTADO} = ?",
            arrayOf("DISPONIBLE"),
            null,
            null,
            null
        )
        return cursorToHerramientaList(cursor)
    }

    fun isHerramientaAsignada(herramientaId: Long, fechaInicio: String, fechaFin: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT COUNT(*) FROM $TABLE_ASIGNACIONES
            WHERE $COLUMN_ASIGNACION_HERRAMIENTA_ID = ? AND
            (
                ($COLUMN_ASIGNACION_FECHA_INICIO <= ? AND $COLUMN_ASIGNACION_FECHA_FIN >= ?) OR
                ($COLUMN_ASIGNACION_FECHA_INICIO <= ? AND $COLUMN_ASIGNACION_FECHA_FIN >= ?)
            )
        """
        val cursor = db.rawQuery(query, arrayOf(herramientaId.toString(), fechaInicio, fechaInicio, fechaFin, fechaFin))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count > 0
    }

    fun getHerramientasInfo(): List<HerramientaInfo> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT
                h.id, h.nombre, h.especificaciones, h.estado,
                a.fecha_fin, a.fecha_devolucion,
                t.nombre as tecnico_nombre
            FROM
                $TABLE_HERRAMIENTAS h
            LEFT JOIN
                $TABLE_ASIGNACIONES a ON h.id = a.herramienta_id
            LEFT JOIN
                ${DatabaseHelper.TABLE_TECNICOS} t ON a.tecnico_id = t.id
            ORDER BY
                CASE WHEN a.fecha_fin IS NULL THEN 1 ELSE 0 END, a.fecha_fin ASC
        """
        val cursor = db.rawQuery(query, null)
        return cursorToHerramientaInfoList(cursor)
    }

    private fun cursorToHerramientaList(cursor: Cursor): List<Herramienta> {
        val herramientas = mutableListOf<Herramienta>()
        while (cursor.moveToNext()) {
            val herramienta = Herramienta(
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HERRAMIENTA_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HERRAMIENTA_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HERRAMIENTA_DESCRIPCION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HERRAMIENTA_ESPECIFICACIONES)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HERRAMIENTA_FOTO_URI))
            )
            herramientas.add(herramienta)
        }
        cursor.close()
        return herramientas
    }

    private fun cursorToHerramientaInfoList(cursor: Cursor): List<HerramientaInfo> {
        val herramientas = mutableListOf<HerramientaInfo>()
        while (cursor.moveToNext()) {
            val herramienta = HerramientaInfo(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("especificaciones")),
                cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_devolucion")),
                cursor.getString(cursor.getColumnIndexOrThrow("tecnico_nombre"))
            )
            herramientas.add(herramienta)
        }
        cursor.close()
        return herramientas
    }
}
