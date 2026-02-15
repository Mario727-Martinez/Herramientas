package com.example.herramientas

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.herramientas.MysqLite.HerramientaDataSource
import com.example.herramientas.databinding.FragmentFirstBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragmento para dar de alta una nueva herramienta.
 * Contiene el formulario de registro, la lógica para tomar una foto y guardar en la base de datos.
 */
class FirstFragment : Fragment() {

    // Objeto de vinculación de vistas (View Binding) para este fragmento.
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // Instancia para interactuar con la tabla de herramientas en la base de datos.
    private lateinit var herramientaDataSource: HerramientaDataSource
    // URI que almacena temporalmente la ubicación de la foto recién tomada.
    private var currentPhotoUri: Uri? = null

    // --- Contratos de Actividad para la Cámara ---

    // Contrato para tomar una foto. Cuando la cámara devuelve un resultado exitoso,
    // actualiza el ImageView con la nueva foto.
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.fotoImageView.setImageURI(currentPhotoUri)
        }
    }

    // Contrato para solicitar el permiso de la cámara. Si el usuario concede el permiso,
    // se lanza la cámara. Si no, se muestra un mensaje.
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout y prepara el View Binding y el DataSource.
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        herramientaDataSource = HerramientaDataSource(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Listeners de los Botones ---

        // Al hacer clic en "Tomar foto", se solicita el permiso de la cámara.
        binding.seleccionarFotoButton.setOnClickListener {
            requestPermission.launch(android.Manifest.permission.CAMERA)
        }

        // Al hacer clic en "Guardar", se validan los datos y se procede a guardar.
        binding.guardarButton.setOnClickListener {
            if (validateInputs()) {
                val nombre = binding.nombreEditText.text.toString()
                val descripcion = binding.descripcionEditText.text.toString()
                val especificaciones = binding.especificacionesEditText.text.toString()

                // Llama al DataSource para insertar la nueva herramienta en la BD.
                val id = herramientaDataSource.guardarHerramienta(nombre, descripcion, especificaciones, currentPhotoUri.toString())
                if (id != -1L) {
                    Toast.makeText(requireContext(), "Herramienta guardada con éxito", Toast.LENGTH_LONG).show()
                    clearForm() // Limpia el formulario para un nuevo registro.
                } else {
                    Toast.makeText(requireContext(), "Error al guardar la herramienta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navega a la pantalla de lista de herramientas.
        binding.verListaButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ListaHerramientasFragment)
        }

        // Navega a la pantalla de asignación de herramientas.
        binding.asignarHerramientaButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_AsignacionFragment)
        }
    }

    /**
     * Prepara un archivo temporal y lanza la intención de la cámara para tomar una foto.
     */
    private fun launchCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null // Si hay un error creando el archivo, no se puede tomar la foto.
        }
        photoFile?.also { file ->
            // Obtiene una URI segura para el archivo a través del FileProvider.
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            currentPhotoUri = photoURI // Guarda la URI para usarla después de que la cámara termine.
            takePicture.launch(photoURI) // Lanza la cámara.
        }
    }

    /**
     * Crea un archivo de imagen temporal en el almacenamiento externo de la app.
     * El nombre del archivo incluye una marca de tiempo para evitar colisiones.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefijo */
            ".jpg", /* sufijo */
            storageDir /* directorio */
        )
    }

    /**
     * Valida que todos los campos obligatorios del formulario estén completos y correctos.
     * Muestra mensajes de error en los campos que no cumplen las reglas.
     * @return `true` si todas las validaciones son exitosas, `false` en caso contrario.
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        val nombre = binding.nombreEditText.text.toString()

        // Validación del nombre
        if (nombre.isBlank()) {
            binding.nombreLayout.error = "El nombre es obligatorio"
            isValid = false
        } else if (!nombre.matches(Regex("^[a-zA-Z0-9 ]{3,}$"))) {
            binding.nombreLayout.error = "Debe contener al menos 3 caracteres alfanuméricos"
            isValid = false
        } else {
            binding.nombreLayout.error = null
        }

        // Validación de la descripción
        if (binding.descripcionEditText.text.toString().isBlank()) {
            binding.descripcionLayout.error = "La descripción es obligatoria"
            isValid = false
        } else {
            binding.descripcionLayout.error = null
        }

        // Validación de las especificaciones
        if (binding.especificacionesEditText.text.toString().isBlank()) {
            binding.especificacionesLayout.error = "Las especificaciones son obligatorias"
            isValid = false
        } else {
            binding.especificacionesLayout.error = null
        }

        return isValid
    }

    /**
     * Limpia todos los campos del formulario, la imagen y los mensajes de error.
     * Se llama después de guardar una herramienta con éxito.
     */
    private fun clearForm() {
        binding.nombreEditText.text = null
        binding.descripcionEditText.text = null
        binding.especificacionesEditText.text = null
        binding.fotoImageView.setImageResource(android.R.drawable.ic_menu_camera)
        binding.nombreLayout.error = null
        binding.descripcionLayout.error = null
        binding.especificacionesLayout.error = null
        currentPhotoUri = null
    }

    /**
     * Limpia la referencia al objeto de vinculación de vistas cuando la vista del fragmento se destruye.
     * Es una medida de seguridad para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}