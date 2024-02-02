package kolskypavel.ardfmanager.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import kolskypavel.ardfmanager.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}