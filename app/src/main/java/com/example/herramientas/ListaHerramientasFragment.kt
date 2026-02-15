package com.example.herramientas

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.herramientas.MysqLite.AsignacionDataSource
import com.example.herramientas.MysqLite.HerramientaDataSource
import com.example.herramientas.databinding.FragmentListaHerramientasBinding

/**
 * Fragmento que muestra una lista de todas las herramientas de la base de datos.
 * Permite buscar, eliminar, devolver y compartir herramientas.
 */
class ListaHerramientasFragment : Fragment() {

    // Objeto de vinculación de vistas (View Binding).
    private var _binding: FragmentListaHerramientasBinding? = null
    private val binding get() = _binding!!

    // Instancias de los DataSource para interactuar con la BD.
    private lateinit var herramientaDataSource: HerramientaDataSource
    private lateinit var asignacionDataSource: AsignacionDataSource
    // Adaptador para el RecyclerView.
    private lateinit var adapter: ListaHerramientasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout y prepara el View Binding y los DataSources.
        _binding = FragmentListaHerramientasBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        asignacionDataSource = AsignacionDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el RecyclerView con un LayoutManager y un adaptador vacío al inicio.
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ListaHerramientasAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        // --- Listeners para las acciones del adaptador ---

        // Listener para el clic en el botón "Eliminar" de un ítem.
        adapter.onItemDeleteClicked = { herramienta ->
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar '${herramienta.nombre}'?")
                .setPositiveButton("Eliminar") { _, _ ->
                    if (herramienta.estado == "ASIGNADA") {
                        Toast.makeText(requireContext(), "No se puede eliminar una herramienta asignada", Toast.LENGTH_SHORT).show()
                    } else {
                        val success = herramientaDataSource.eliminarHerramienta(herramienta.id)
                        if (success) {
                            Toast.makeText(requireContext(), "Herramienta eliminada", Toast.LENGTH_SHORT).show()
                            loadHerramientas() // Recarga la lista.
                        } else {
                            Toast.makeText(requireContext(), "Error al eliminar la herramienta", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Listener para el clic en el botón "Devolver" de un ítem.
        adapter.onItemDevolverClicked = { herramienta ->
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Devolución")
                .setMessage("¿Confirmar la devolución de '${herramienta.nombre}'?")
                .setPositiveButton("Confirmar") { _, _ ->
                    // Llama a los DataSources para actualizar la BD.
                    asignacionDataSource.marcarDevolucion(herramienta.id)
                    herramientaDataSource.updateHerramientaEstado(herramienta.id, "DISPONIBLE")
                    Toast.makeText(requireContext(), "Herramienta devuelta", Toast.LENGTH_SHORT).show()
                    loadHerramientas() // Recarga la lista para reflejar el cambio de estado y color.
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Listener para el clic en el botón "Compartir" de un ítem.
        adapter.onItemCompartirClicked = { herramienta ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            var shareText = "Ficha de Herramienta:\n"
            shareText += "Nombre: ${herramienta.nombre}\n"
            shareText += "Especificaciones: ${herramienta.especificaciones}"

            // Si la herramienta está asignada, añade los detalles de la asignación al texto.
            if (herramienta.estado == "ASIGNADA") {
                shareText += "\n\nAsignación Activa:"
                shareText += "\nTécnico: ${herramienta.tecnicoNombre}"
                shareText += "\nFecha de Entrega: ${herramienta.fechaFin}"
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            // Lanza un selector de aplicaciones para que el usuario elija dónde compartir.
            startActivity(Intent.createChooser(shareIntent, "Compartir Ficha de Herramienta"))
        }

        // Configura la barra de búsqueda para que filtre la lista en tiempo real.
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No se necesita acción al enviar.
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText) // Filtra el adaptador cada vez que el texto cambia.
                return false
            }
        })

        // Carga los datos iniciales en la lista.
        loadHerramientas()
    }

    /**
     * Obtiene la lista completa de información de herramientas desde la BD y la pasa al adaptador.
     */
    private fun loadHerramientas() {
        val herramientas = herramientaDataSource.getHerramientasInfo()
        adapter.updateList(herramientas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}