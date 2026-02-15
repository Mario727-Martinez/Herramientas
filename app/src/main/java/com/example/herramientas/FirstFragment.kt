package com.example.herramientas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.herramientas.MysqLite.HerramientaDataSource
import com.example.herramientas.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var herramientaDataSource: HerramientaDataSource

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seleccionarFotoButton.setOnClickListener {
            // TODO: Implement camera or gallery selection
        }

        binding.guardarButton.setOnClickListener {
            if (validateInputs()) {
                val nombre = binding.nombreEditText.text.toString()
                val descripcion = binding.descripcionEditText.text.toString()
                val especificaciones = binding.especificacionesEditText.text.toString()

                val id = herramientaDataSource.guardarHerramienta(nombre, descripcion, especificaciones, null)
                if (id != -1L) {
                    Toast.makeText(requireContext(), "Herramienta guardada con éxito", Toast.LENGTH_LONG).show()
                    clearForm()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la herramienta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.verListaButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ListaHerramientasFragment)
        }

        binding.asignarHerramientaButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_AsignacionFragment)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val nombre = binding.nombreEditText.text.toString()

        if (nombre.isBlank()) {
            binding.nombreLayout.error = "El nombre es obligatorio"
            isValid = false
        } else if (!nombre.matches(Regex("^[a-zA-Z0-9 ]{3,}$"))) {
            binding.nombreLayout.error = "El nombre debe contener al menos 3 caracteres alfanuméricos"
            isValid = false
        } else {
            binding.nombreLayout.error = null
        }

        if (binding.descripcionEditText.text.toString().isBlank()) {
            binding.descripcionLayout.error = "La descripción es obligatoria"
            isValid = false
        } else {
            binding.descripcionLayout.error = null
        }

        if (binding.especificacionesEditText.text.toString().isBlank()) {
            binding.especificacionesLayout.error = "Las especificaciones son obligatorias"
            isValid = false
        } else {
            binding.especificacionesLayout.error = null
        }

        return isValid
    }

    private fun clearForm() {
        binding.nombreEditText.text = null
        binding.descripcionEditText.text = null
        binding.especificacionesEditText.text = null
        binding.fotoImageView.setImageResource(android.R.drawable.ic_menu_camera)
        binding.nombreLayout.error = null
        binding.descripcionLayout.error = null
        binding.especificacionesLayout.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}