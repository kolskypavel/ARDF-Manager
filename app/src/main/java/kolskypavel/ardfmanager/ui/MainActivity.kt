package kolskypavel.ardfmanager.ui

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.databinding.ActivityMainBinding
import kolskypavel.ardfmanager.room.ARDFRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        ARDFRepository.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menu_item_about_app,
                R.id.menu_item_global_settings,
                R.id.navigation_readouts,
                R.id.navigation_results
            )
        )
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.menu_item_about_app, R.id.menu_item_global_settings, R.id.navigation_readouts, R.id.navigation_results -> navView.visibility =
                    View.VISIBLE

                else -> navView.visibility = View.GONE
            }
        }

    }
}