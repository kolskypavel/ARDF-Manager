package kolskypavel.ardfmanager.ui.startlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kolskypavel.ardfmanager.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class StartlistFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_startlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.startlist_bottom_navigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_startlist_draw -> {
                    showFragment(StartlistDrawFragment())
                    true
                }
                R.id.navigation_startlist_overview -> {
                    showFragment(StartlistOverviewFragment())
                    true
                }
                else -> false
            }
        }

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_startlist_draw
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.startlist_nav_host_fragment, fragment)
            .commit()
    }
}
