package com.example.herramientas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.herramientas.MysqLite.HerramientaInfo
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Adaptador para el RecyclerView que muestra la lista de herramientas.
 * Implementa Filterable para permitir la búsqueda.
 */
class ListaHerramientasAdapter(private var herramientas: List<HerramientaInfo>) : RecyclerView.Adapter<ListaHerramientasAdapter.ViewHolder>(), Filterable {

    // Lista que contiene las herramientas después de aplicar un filtro. La lista original se mantiene intacta.
    private var herramientasFiltradas: List<HerramientaInfo> = herramientas

    // --- Lambdas para manejar los eventos de clic ---
    // Se definen como variables que pueden contener una función. El Fragmento les dará un valor.
    var onItemDeleteClicked: ((HerramientaInfo) -> Unit)? = null
    var onItemDevolverClicked: ((HerramientaInfo) -> Unit)? = null
    var onItemCompartirClicked: ((HerramientaInfo) -> Unit)? = null

    /**
     * Crea un nuevo ViewHolder inflando el layout del ítem.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_herramienta, parent, false)
        return ViewHolder(view)
    }

    /**
     * Vincula los datos de una herramienta específica con un ViewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val herramienta = herramientasFiltradas[position]
        holder.bind(herramienta)
    }

    /**
     * Devuelve el número total de ítems en la lista filtrada.
     */
    override fun getItemCount(): Int = herramientasFiltradas.size

    /**
     * Actualiza la lista de herramientas del adaptador y notifica al RecyclerView para que se redibuje.
     */
    fun updateList(newList: List<HerramientaInfo>) {
        herramientas = newList
        herramientasFiltradas = newList
        notifyDataSetChanged()
    }

    /**
     * Clase interna que representa la vista de un solo ítem en la lista.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Referencias a las vistas dentro del layout del ítem.
        private val nombreTextView: TextView = itemView.findViewById(R.id.nombre_text_view)
        private val estadoTextView: TextView = itemView.findViewById(R.id.estado_text_view)
        private val fechaFinTextView: TextView = itemView.findViewById(R.id.fecha_fin_text_view)
        private val tecnicoTextView: TextView = itemView.findViewById(R.id.tecnico_text_view)
        private val eliminarButton: Button = itemView.findViewById(R.id.eliminar_button)
        private val devolverButton: Button = itemView.findViewById(R.id.devolver_button)
        private val compartirButton: Button = itemView.findViewById(R.id.compartir_button)

        /**
         * Vincula los datos de un objeto HerramientaInfo con las vistas del ViewHolder.
         */
        fun bind(herramienta: HerramientaInfo) {
            nombreTextView.text = herramienta.nombre
            estadoTextView.text = "Estado: ${herramienta.estado}"
            fechaFinTextView.text = if (herramienta.fechaFin != null) "Fecha de entrega: ${herramienta.fechaFin}" else ""
            tecnicoTextView.text = if (herramienta.tecnicoNombre != null) "Técnico: ${herramienta.tecnicoNombre}" else ""

            // --- Lógica de colores ---
            val color = when {
                herramienta.fechaDevolucion != null -> Color.GREEN // Verde si ya fue devuelta.
                herramienta.estado == "ASIGNADA" && isVencida(herramienta.fechaFin) -> Color.RED // Rojo si está asignada y vencida.
                herramienta.estado == "ASIGNADA" && isProximaEntrega(herramienta.fechaFin) -> Color.YELLOW // Ámbar si faltan 48h o menos.
                else -> Color.GRAY // Gris para las disponibles.
            }
            itemView.setBackgroundColor(color)

            // Asigna los listeners a los botones, invocando las lambdas correspondientes.
            eliminarButton.setOnClickListener { onItemDeleteClicked?.invoke(herramienta) }
            devolverButton.setOnClickListener { onItemDevolverClicked?.invoke(herramienta) }
            compartirButton.setOnClickListener { onItemCompartirClicked?.invoke(herramienta) }

            // --- Lógica de visibilidad de botones ---
            // El botón "Devolver" solo es visible si la herramienta está asignada y no ha sido devuelta.
            if (herramienta.estado == "ASIGNADA" && herramienta.fechaDevolucion == null) {
                devolverButton.visibility = View.VISIBLE
            } else {
                devolverButton.visibility = View.GONE
            }
            // El botón de compartir siempre es visible.
            compartirButton.visibility = View.VISIBLE
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

    /**
     * Devuelve el objeto Filter que se usa para filtrar la lista.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            /**
             * Realiza la lógica de filtrado en un hilo de fondo.
             */
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().lowercase(Locale.getDefault())
                val filteredList = if (query.isEmpty()) {
                    herramientas // Si no hay búsqueda, devuelve la lista completa.
                } else {
                    // Filtra la lista por nombre de herramienta, nombre de técnico o especificaciones.
                    herramientas.filter {
                        it.nombre.lowercase(Locale.getDefault()).contains(query) ||
                        (it.tecnicoNombre?.lowercase(Locale.getDefault())?.contains(query) ?: false) ||
                                it.especificaciones.lowercase(Locale.getDefault()).contains(query)
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            /**
             * Publica los resultados del filtrado en el hilo principal.
             */
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                herramientasFiltradas = (results?.values as? List<HerramientaInfo>) ?: emptyList()
                notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado.
            }
        }
    }
}