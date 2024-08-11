package kolskypavel.ardfmanager.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import kolskypavel.ardfmanager.R

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
    }

    fun changeAppLanguage(language: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
    }
}