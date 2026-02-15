package com.example.herramientas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.herramientas.databinding.ActivityMainBinding

/**
 * Actividad principal de la aplicación.
 * Es el punto de entrada y contiene el NavHostFragment que gestiona la navegación entre los diferentes fragmentos.
 */
class MainActivity : AppCompatActivity() {

    // Configuración para la barra de aplicaciones (AppBar) que integra el componente de navegación.
    private lateinit var appBarConfiguration: AppBarConfiguration
    // Objeto de vinculación de vistas (View Binding) para acceder a los componentes del layout de forma segura.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla (crea) el layout de la actividad usando View Binding.
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Establece el layout inflado como el contenido de la actividad.
        setContentView(binding.root)

        // Configura la Toolbar (barra de herramientas) como la AppBar de la actividad.
        setSupportActionBar(binding.toolbar)

        // Busca el NavController, que es el responsable de gestionar la navegación entre los fragmentos.
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Crea la configuración de la AppBar, vinculándola con el grafo de navegación.
        appBarConfiguration = AppBarConfiguration(navController.graph)
        // Conecta la AppBar con el NavController para que los títulos se actualicen y se muestre el botón de "atrás".
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Gestiona el evento de navegación "hacia arriba" (el botón de atrás en la Toolbar).
     * Permite que el NavController maneje la acción de retroceder en el historial de navegación.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}