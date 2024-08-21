package kolskypavel.ardfmanager.ui.settings

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import kolskypavel.ardfmanager.R
import kolskypavel.ardfmanager.ui.MainActivity
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setPreferences()
    }

    private fun setPreferences() {

        findPreference<CheckBoxPreference>("keep_screen_open")
            ?.setOnPreferenceChangeListener { _, newValue ->
                //   window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                true // Return true if the event is handled.
            }

        findPreference<ListPreference>("language")
            ?.setOnPreferenceChangeListener { _, newValue ->
                //   window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                true // Return true if the event is handled.
            }


        findPreference<androidx.preference.Preference>("prints")
            ?.setOnPreferenceClickListener {
                findNavController().navigate(SettingsFragmentDirections.configurePrints())
                true
            }
    }


    fun changeAppLanguage(language: String) {
        val myLocale = Locale(language)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration

        // Update the locale configuration
        conf.setLocale(myLocale)

        // Update the configuration for newer API levels
        @Suppress("DEPRECATION")
        res.updateConfiguration(conf, dm)

        requireActivity().finish()
        startActivity(requireActivity().intent)
    }

}