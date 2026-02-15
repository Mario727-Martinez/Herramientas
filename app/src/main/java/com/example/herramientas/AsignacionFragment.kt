package com.example.herramientas

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.herramientas.MysqLite.*
import com.example.herramientas.databinding.FragmentAsignacionBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragmento para asignar una herramienta disponible a un técnico.
 */
class AsignacionFragment : Fragment() {

    // Objeto de vinculación de vistas (View Binding).
    private var _binding: FragmentAsignacionBinding? = null
    private val binding get() = _binding!!

    // Instancias de los DataSource para interactuar con las diferentes tablas de la BD.
    private lateinit var herramientaDataSource: HerramientaDataSource
    private lateinit var tecnicoDataSource: TecnicoDataSource
    private lateinit var asignacionDataSource: AsignacionDataSource

    // Listas para almacenar los datos cargados desde la BD para los spinners.
    private lateinit var herramientas: List<Herramienta>
    private lateinit var tecnicos: List<Tecnico>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout y prepara el View Binding y los DataSources.
        _binding = FragmentAsignacionBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        tecnicoDataSource = TecnicoDataSource(requireContext())
        asignacionDataSource = AsignacionDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carga los datos iniciales en los spinners.
        loadSpinners()

        // Configura los campos de fecha para que muestren un DatePickerDialog al hacer clic.
        binding.fechaInicioEditText.setOnClickListener { showDatePickerDialog(it as EditText) }
        binding.fechaFinEditText.setOnClickListener { showDatePickerDialog(it as EditText) }

        // Lógica del botón "Asignar".
        binding.asignarButton.setOnClickListener {
            if (validateInputs()) {
                val herramienta = herramientas[binding.herramientaSpinner.selectedItemPosition]
                val tecnico = tecnicos[binding.tecnicoSpinner.selectedItemPosition]
                val fechaInicio = binding.fechaInicioEditText.text.toString()
                val fechaFin = binding.fechaFinEditText.text.toString()

                // Verifica si la herramienta ya tiene una asignación que se solapa con las fechas seleccionadas.
                if (herramientaDataSource.isHerramientaAsignada(herramienta.id, fechaInicio, fechaFin)) {
                    Toast.makeText(requireContext(), "La herramienta ya está asignada en el período seleccionado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Muestra un diálogo de confirmación antes de proceder.
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmar Asignación")
                    .setMessage("¿Confirmar asignación de '${herramienta.nombre}' a '${tecnico.nombre}' del $fechaInicio al $fechaFin?")
                    .setPositiveButton("Confirmar") { _, _ ->
                        // Si el usuario confirma, guarda la asignación.
                        val id = asignacionDataSource.guardarAsignacion(herramienta.id, tecnico.id, fechaInicio, fechaFin)
                        if (id != -1L) {
                            // Cambia el estado de la herramienta a "ASIGNADA".
                            herramientaDataSource.updateHerramientaEstado(herramienta.id, "ASIGNADA")
                            Toast.makeText(requireContext(), "Asignación guardada con éxito", Toast.LENGTH_LONG).show()
                            clearForm() // Limpia el formulario.
                            loadSpinners() // Recarga los spinners para que la herramienta asignada ya no aparezca.
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar la asignación", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    /**
     * Carga las herramientas disponibles y todos los técnicos desde la BD y los muestra en los spinners.
     */
    private fun loadSpinners() {
        herramientas = herramientaDataSource.getHerramientasDisponibles()
        tecnicos = tecnicoDataSource.getTecnicos()
        setupSpinner(binding.herramientaSpinner, herramientas.map { it.nombre })
        setupSpinner(binding.tecnicoSpinner, tecnicos.map { it.nombre })
    }

    /**
     * Muestra un diálogo de calendario (DatePickerDialog) para que el usuario seleccione una fecha.
     * @param editText El campo de texto donde se insertará la fecha seleccionada.
     */
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), {
            _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            editText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    /**
     * Configura un Spinner con un adaptador y una lista de datos de tipo String.
     */
    private fun setupSpinner(spinner: Spinner, data: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Valida que todos los campos del formulario de asignación sean correctos.
     * @return `true` si la validación es exitosa, `false` en caso contrario.
     */
    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.herramientaSpinner.selectedItem == null || herramientas.isEmpty()) {
            Toast.makeText(requireContext(), "No hay herramientas disponibles para asignar", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.tecnicoSpinner.selectedItem == null) {
            Toast.makeText(requireContext(), "Seleccione un técnico", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        val fechaInicioStr = binding.fechaInicioEditText.text.toString()
        val fechaFinStr = binding.fechaFinEditText.text.toString()

        if (fechaInicioStr.isBlank() || fechaFinStr.isBlank()) {
            Toast.makeText(requireContext(), "Seleccione ambas fechas", Toast.LENGTH_SHORT).show()
            return false
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val fechaInicio = dateFormat.parse(fechaInicioStr)
            val fechaFin = dateFormat.parse(fechaFinStr)

            // Valida que la fecha de fin no sea anterior a la de inicio.
            if (fechaFin.before(fechaInicio)) {
                Toast.makeText(requireContext(), "La fecha de fin no puede ser anterior a la fecha de inicio", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        } catch (e: Exception) {
            isValid = false // No debería ocurrir gracias al DatePickerDialog.
        }

        return isValid
    }

    /**
     * Limpia los campos de fecha del formulario.
     */
    private fun clearForm() {
        binding.fechaInicioEditText.text = null
        binding.fechaFinEditText.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}