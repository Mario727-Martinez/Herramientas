package com.example.herramientas.MysqLite

import android.content.Context
import android.database.Cursor
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_TECNICO_ID
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.COLUMN_TECNICO_NOMBRE
import com.example.herramientas.MysqLite.DatabaseHelper.Companion.TABLE_TECNICOS

data class Tecnico(
    val id: Long,
    val nombre: String
)

class TecnicoDataSource(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun getTecnicos(): List<Tecnico> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_TECNICOS,
            null,
            null,
            null,
            null,
            null,
            null
        )
        return cursorToTecnicoList(cursor)
    }

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