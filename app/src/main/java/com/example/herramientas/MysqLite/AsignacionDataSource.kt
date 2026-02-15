package com.example.herramientas.MysqLite

import android.content.ContentValues
import android.content.Context

data class Asignacion(
    val id: Long,
    val herramientaId: Long,
    val tecnicoId: Long,
    val fechaInicio: String,
    val fechaFin: String
)

class AsignacionDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun guardarAsignacion(herramientaId: Long, tecnicoId: Long, fechaInicio: String, fechaFin: String): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ASIGNACION_HERRAMIENTA_ID, herramientaId)
            put(DatabaseHelper.COLUMN_ASIGNACION_TECNICO_ID, tecnicoId)
            put(DatabaseHelper.COLUMN_ASIGNACION_FECHA_INICIO, fechaInicio)
            put(DatabaseHelper.COLUMN_ASIGNACION_FECHA_FIN, fechaFin)
        }

        return db.insert(DatabaseHelper.TABLE_ASIGNACIONES, null, values)
    }
}