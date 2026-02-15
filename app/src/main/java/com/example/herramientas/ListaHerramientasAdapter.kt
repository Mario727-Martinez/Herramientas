package com.example.herramientas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.herramientas.MysqLite.HerramientaInfo
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ListaHerramientasAdapter(private var herramientas: List<HerramientaInfo>) : RecyclerView.Adapter<ListaHerramientasAdapter.ViewHolder>(), Filterable {

    private var herramientasFiltradas: List<HerramientaInfo> = herramientas

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_herramienta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val herramienta = herramientasFiltradas[position]
        holder.bind(herramienta)
    }

    override fun getItemCount(): Int = herramientasFiltradas.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.nombre_text_view)
        private val estadoTextView: TextView = itemView.findViewById(R.id.estado_text_view)
        private val fechaFinTextView: TextView = itemView.findViewById(R.id.fecha_fin_text_view)
        private val tecnicoTextView: TextView = itemView.findViewById(R.id.tecnico_text_view)

        fun bind(herramienta: HerramientaInfo) {
            nombreTextView.text = herramienta.nombre
            estadoTextView.text = "Estado: ${herramienta.estado}"
            fechaFinTextView.text = if (herramienta.fechaFin != null) "Fecha de entrega: ${herramienta.fechaFin}" else ""
            tecnicoTextView.text = if (herramienta.tecnicoNombre != null) "Técnico: ${herramienta.tecnicoNombre}" else ""

            val color = when {
                herramienta.fechaDevolucion != null -> Color.GREEN
                herramienta.estado == "ASIGNADA" && isVencida(herramienta.fechaFin) -> Color.RED
                herramienta.estado == "ASIGNADA" && isProximaEntrega(herramienta.fechaFin) -> Color.YELLOW
                else -> Color.GRAY
            }
            itemView.setBackgroundColor(color)
        }

        private fun isVencida(fechaFin: String?): Boolean {
            if (fechaFin == null) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaFinDate = sdf.parse(fechaFin)
            return Date().after(fechaFinDate)
        }

        private fun isProximaEntrega(fechaFin: String?): Boolean {
            if (fechaFin == null) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaFinDate = sdf.parse(fechaFin)
            val diff = fechaFinDate.time - Date().time
            return TimeUnit.MILLISECONDS.toHours(diff) <= 48
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().toLowerCase(Locale.getDefault())
                val filteredList = if (query.isEmpty()) {
                    herramientas
                } else {
                    herramientas.filter {
                        it.nombre.toLowerCase(Locale.getDefault()).contains(query) ||
                        (it.tecnicoNombre?.toLowerCase(Locale.getDefault())?.contains(query) ?: false) ||
                        it.especificaciones.toLowerCase(Locale.getDefault()).contains(query)
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                herramientasFiltradas = (results?.values as? List<HerramientaInfo>) ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}