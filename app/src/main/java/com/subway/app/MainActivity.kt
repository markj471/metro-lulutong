package com.subway.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.subway.app.data.WuhanMetroJsonLoader
import com.subway.app.data.local.MetroDatabase
import com.subway.app.data.repository.MetroRepository
import com.subway.app.data.settings.AppSettingsManager
import com.subway.app.data.settings.ThemeMode
import com.subway.app.ui.AppNavigation
import com.subway.app.ui.theme.SubwayTheme
import com.subway.app.ui.theme.ThemePolicy
import com.subway.app.viewmodel.MetroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MetroViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            requestCurrentLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MetroDatabase.getDatabase(this)
        val repository = MetroRepository(
            lineDao = database.lineDao(),
            stationDao = database.stationDao(),
            connectionDao = database.connectionDao(),
            historyDao = database.historyDao(),
            database = database
        )

        viewModel = MetroViewModel(repository)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel.initialize(this)
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { WuhanMetroJsonLoader.loadFromAssets(this@MainActivity) }
                .onSuccess(database::loadFromMetroData)
                .onFailure { database.ensureInitialized() }
        }

        setContent {
            val settingsManager = remember { AppSettingsManager(this) }
            var themeMode by remember { mutableStateOf(settingsManager.themeMode()) }
            var fontScale by remember { mutableFloatStateOf(settingsManager.fontScale()) }
            val useDarkTheme = ThemePolicy.useDarkTheme(themeMode, isSystemInDarkTheme())

            SubwayTheme(themeMode = themeMode, fontScale = fontScale) {
                val view = LocalView.current
                val systemBarColor = MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    window.statusBarColor = systemBarColor
                    window.navigationBarColor = systemBarColor
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !useDarkTheme
                        isAppearanceLightNavigationBars = !useDarkTheme
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        themeMode = themeMode,
                        fontScale = fontScale,
                        onThemeModeChange = { mode: ThemeMode ->
                            settingsManager.setThemeMode(mode)
                            themeMode = mode
                        },
                        onFontScaleChange = { scale: Float ->
                            settingsManager.setFontScale(scale)
                            fontScale = scale
                        }
                    )
                }
            }
        }

        checkAndRequestLocation()
    }

    private fun checkAndRequestLocation() {
        if (hasLocationPermission()) {
            requestCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestCurrentLocation() {
        if (!hasLocationPermission()) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.updateNearbyStations(it.latitude, it.longitude)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
