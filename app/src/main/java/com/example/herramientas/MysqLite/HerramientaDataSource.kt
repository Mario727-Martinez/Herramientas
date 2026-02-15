package com.example.herramientas.MysqLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_FECHA_FIN
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_FECHA_INICIO
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_ASIGNACION_HERRAMIENTA_ID
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_ASIGNACIONES
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_HERRAMIENTAS

/**
 * Data class que representa el modelo de una Herramienta básica.
 */
data class Herramienta(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val especificaciones: String,
    val fotoUri: String?
)

/**
 * Data class que representa un modelo enriquecido de Herramienta.
 * Se usa en la lista para mostrar información combinada de varias tablas (Herramientas, Asignaciones, Tecnicos).
 */
data class HerramientaInfo(
    val id: Long,
    val nombre: String,
    val especificaciones: String,
    val estado: String,
    val fechaFin: String?,
    val fechaDevolucion: String?,
    val tecnicoNombre: String?
)

/**
 * Clase que encapsula todas las operaciones de acceso a datos para la tabla de Herramientas.
 * Actúa como un intermediario entre la lógica de la app y la base de datos.
 */
class HerramientaDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Guarda una nueva herramienta en la base de datos.
     * @return El ID de la fila recién insertada, o -1 si ocurrió un error.
     */
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

    /**
     * Actualiza el estado de una herramienta específica (ej: de "DISPONIBLE" a "ASIGNADA").
     */
    fun updateHerramientaEstado(id: Long, estado: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_HERRAMIENTA_ESTADO, estado)
        }
        db.update(TABLE_HERRAMIENTAS, values, "id = ?", arrayOf(id.toString()))
    }

    /**
     * Obtiene una lista de todas las herramientas que están marcadas como "DISPONIBLE".
     * Se usa en el spinner de la pantalla de asignación.
     */
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

    /**
     * Comprueba si una herramienta ya tiene una asignación activa que se solapa con un nuevo rango de fechas.
     * @return `true` si hay un traslape, `false` en caso contrario.
     */
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

    /**
     * Obtiene una lista enriquecida con información combinada de las tablas Herramientas, Asignaciones y Tecnicos.
     * Se usa para poblar la lista principal de herramientas.
     * La consulta usa LEFT JOIN para incluir asignaciones y técnicos solo si existen.
     * Ordena los resultados para mostrar primero las herramientas con fecha de fin más próxima.
     */
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

    /**
     * Elimina una herramienta de la base de datos, así como todas sus asignaciones asociadas.
     * @return `true` si la eliminación fue exitosa, `false` en caso contrario.
     */
    fun eliminarHerramienta(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(TABLE_HERRAMIENTAS, "id = ?", arrayOf(id.toString()))
        if (rowsAffected > 0) {
            // También elimina en cascada las asignaciones para mantener la integridad de los datos.
            db.delete(TABLE_ASIGNACIONES, "$COLUMN_ASIGNACION_HERRAMIENTA_ID = ?", arrayOf(id.toString()))
            return true
        }
        return false
    }

    /**
     * Convierte un objeto Cursor en una lista de objetos Herramienta.
     */
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

    /**
     * Convierte un objeto Cursor (resultado de un JOIN) en una lista de objetos HerramientaInfo.
     */
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