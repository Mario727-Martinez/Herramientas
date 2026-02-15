package com.example.herramientas.MysqLite

import android.content.Context
import android.database.Cursor
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_TECNICO_ID
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_TECNICO_NOMBRE
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_TECNICOS

/**
 * Data class que representa el modelo de un Técnico.
 */
data class Tecnico(
    val id: Long,
    val nombre: String
)

/**
 * Clase que encapsula todas las operaciones de acceso a datos para la tabla de Tecnicos.
 */
class TecnicoDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Obtiene una lista de todos los técnicos registrados en la base de datos.
     * Se usa para poblar el spinner en la pantalla de asignación.
     */
    fun getTecnicos(): List<Tecnico> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_TECNICOS,
            null, // null para todas las columnas
            null, // sin cláusula WHERE
            null, // sin argumentos para WHERE
            null, // sin GROUP BY
            null, // sin HAVING
            null  // sin ORDER BY
        )
        return cursorToTecnicoList(cursor)
    }

    /**
     * Convierte un objeto Cursor en una lista de objetos Tecnico.
     */
    private fun cursorToTecnicoList(cursor: Cursor): List<Tecnico> {
        val tecnicos = mutableListOf<Tecnico>()
        while (cursor.moveToNext()) {
            val tecnico = Tecnico(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TECNICO_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TECNICO_NOMBRE))
            )
            tecnicos.add(tecnico)
        }
        cursor.close()
        return tecnicos
    }
}