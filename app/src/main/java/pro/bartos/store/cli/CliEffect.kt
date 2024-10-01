package pro.bartos.store.cli

sealed class CliEffect {
    data class ProvideHapticFeedback(val duration: Long) : CliEffect()
}