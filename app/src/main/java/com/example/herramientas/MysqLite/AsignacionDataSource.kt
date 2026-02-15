package com.example.herramientas.MysqLite

import android.content.ContentValues
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class que representa el modelo de una Asignación.
 */
data class Asignacion(
    val id: Long,
    val herramientaId: Long,
    val tecnicoId: Long,
    val fechaInicio: String,
    val fechaFin: String
)

/**
 * Clase que encapsula todas las operaciones de acceso a datos para la tabla de Asignaciones.
 */
class AsignacionDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Guarda un nuevo registro de asignación en la base de datos.
     * @return El ID de la fila recién insertada, o -1 si ocurrió un error.
     */
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

    /**
     * Actualiza la asignación activa de una herramienta para registrar la fecha de devolución.
     * Busca la asignación por el ID de la herramienta que aún no tiene fecha de devolución.
     */
    fun marcarDevolucion(herramientaId: Long) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // Formatea la fecha actual como "YYYY-MM-DD".
            put(DatabaseHelper.COLUMN_ASIGNACION_FECHA_DEVOLUCION, SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        // Actualiza solo la fila donde el ID de la herramienta coincida y la fecha de devolución sea nula.
        db.update(DatabaseHelper.TABLE_ASIGNACIONES, values, "${DatabaseHelper.COLUMN_ASIGNACION_HERRAMIENTA_ID} = ? AND ${DatabaseHelper.COLUMN_ASIGNACION_FECHA_DEVOLUCION} IS NULL", arrayOf(herramientaId.toString()))
    }
}