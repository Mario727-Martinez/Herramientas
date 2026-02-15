package com.example.herramientas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.example.herramientas.MysqLite.* 
import com.example.herramientas.databinding.FragmentAsignacionBinding
import java.text.SimpleDateFormat
import java.util.*

class AsignacionFragment : Fragment() {

    private var _binding: FragmentAsignacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var herramientaDataSource: HerramientaDataSource
    private lateinit var tecnicoDataSource: TecnicoDataSource
    private lateinit var asignacionDataSource: AsignacionDataSource

    private lateinit var herramientas: List<Herramienta>
    private lateinit var tecnicos: List<Tecnico>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsignacionBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        tecnicoDataSource = TecnicoDataSource(requireContext())
        asignacionDataSource = AsignacionDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        herramientas = herramientaDataSource.getHerramientasDisponibles()
        tecnicos = tecnicoDataSource.getTecnicos()

        setupSpinner(binding.herramientaSpinner, herramientas.map { it.nombre })
        setupSpinner(binding.tecnicoSpinner, tecnicos.map { it.nombre })

        binding.asignarButton.setOnClickListener {
            if (validateInputs()) {
                val herramienta = herramientas[binding.herramientaSpinner.selectedItemPosition]
                val tecnico = tecnicos[binding.tecnicoSpinner.selectedItemPosition]
                val fechaInicio = binding.fechaInicioEditText.text.toString()
                val fechaFin = binding.fechaFinEditText.text.toString()

                if (!herramientaDataSource.isHerramientaAsignada(herramienta.id, fechaInicio, fechaFin)) {
                    val id = asignacionDataSource.guardarAsignacion(herramienta.id, tecnico.id, fechaInicio, fechaFin)
                    if (id != -1L) {
                        Toast.makeText(requireContext(), "Asignación guardada con éxito", Toast.LENGTH_LONG).show()
                        clearForm()
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar la asignación", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "La herramienta ya está asignada en el período seleccionado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, data: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.herramientaSpinner.selectedItem == null) {
            Toast.makeText(requireContext(), "Seleccione una herramienta", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.tecnicoSpinner.selectedItem == null) {
            Toast.makeText(requireContext(), "Seleccione un técnico", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        val fechaInicioStr = binding.fechaInicioEditText.text.toString()
        val fechaFinStr = binding.fechaFinEditText.text.toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val fechaInicio = dateFormat.parse(fechaInicioStr)
            val fechaFin = dateFormat.parse(fechaFinStr)

            if (fechaFin.before(fechaInicio)) {
                Toast.makeText(requireContext(), "La fecha de fin no puede ser anterior a la fecha de inicio", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Formato de fecha no válido. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun clearForm() {
        binding.fechaInicioEditText.text = null
        binding.fechaFinEditText.text = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}