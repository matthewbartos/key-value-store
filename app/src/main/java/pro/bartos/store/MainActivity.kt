package pro.bartos.store

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import pro.bartos.store.cli.CliEffect
import pro.bartos.store.cli.CliViewModel
import pro.bartos.store.cli.MainScreen
import pro.bartos.store.ui.theme.StoreDemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CliViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            StoreDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        state = state,
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        handleIntent = viewModel::handleIntent,
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { handleEffect(it) }
            }
        }
    }

    private fun handleEffect(effect: CliEffect) {
        when (effect) {
            is CliEffect.ProvideHapticFeedback -> vibrateDevice(effect.duration)
        }
    }

    private fun vibrateDevice(durationMs: Long = 100L) {
        val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(durationMs)
        }
    }
}