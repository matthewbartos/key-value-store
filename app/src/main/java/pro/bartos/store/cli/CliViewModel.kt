package pro.bartos.store.cli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pro.bartos.store.data.store.InMemoryKeyValueStoreImpl
import pro.bartos.store.data.store.KeyValueStore
import pro.bartos.store.cli.CliOperation.TransactionOperation.Begin
import pro.bartos.store.cli.CliOperation.TransactionOperation.Commit
import pro.bartos.store.cli.CliOperation.TransactionOperation.Rollback
import pro.bartos.store.cli.CliOperation.ValueOperation.Count
import pro.bartos.store.cli.CliOperation.ValueOperation.Delete
import pro.bartos.store.cli.CliOperation.ValueOperation.Get
import pro.bartos.store.cli.CliOperation.ValueOperation.Set

class CliViewModel(
    private val keyValueStore: KeyValueStore = InMemoryKeyValueStoreImpl(),
) : ViewModel() {

    private val _state = MutableStateFlow<CliState>(
        CliState.Loaded(
            logLines = listOf(WELCOME_MESSAGE, INSTRUCTIONS_MESSAGE),
        )
    )
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CliEffect>()
    val effect = _effect.asSharedFlow()

    private val currentLoadedState: CliState.Loaded?
        get() = _state.value as? CliState.Loaded

    // Skipping Intent -> Action mapping
    // It's 1:1 mapping, avoiding unnecessary complexity
    fun handleIntent(intent: CliIntent) = viewModelScope.launch(Dispatchers.IO) {
        when (intent) {
            is CliIntent.ChangeInput -> currentLoadedState?.copy(input = intent.input)?.let { _state.value = it }

            is CliIntent.ChangeOperation -> currentLoadedState?.copy(selectedOperation = intent.operation)?.let { _state.value = it }

            CliIntent.Execute -> execute()

            CliIntent.DialogConfirmed -> {
                currentLoadedState?.copy(showAlertDialog = false)?.let { _state.value = it }
                execute(showConfirmationDialog = false)
            }

            CliIntent.DialogDismissed -> currentLoadedState?.copy(showAlertDialog = false)?.let { _state.value = it }
        }
    }

    private fun execute(showConfirmationDialog: Boolean = true) = viewModelScope.launch {
        val input = currentLoadedState?.input
        val type = currentLoadedState?.selectedOperation
        val inputValues = input.orEmpty().trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val logMessage = "> ${type?.message} $input"

        if (showConfirmationDialog && (type is Delete || type is Commit || type is Rollback)) {
            viewModelScope.launch {
                _effect.emit(CliEffect.ProvideHapticFeedback(LONG_HAPTIC_FEEDBACK))
                currentLoadedState?.copy(showAlertDialog = true)?.let { _state.value = it }
            }
            return@launch
        }

        currentLoadedState?.copy(input = "")?.let { _state.value = it }
        _effect.emit(CliEffect.ProvideHapticFeedback(SHORT_HAPTIC_FEEDBACK))

        when (type) {
            null -> appendLog(NO_OPERATION_ERROR)

            Get -> {
                if (input == null || inputValues.size != 1) {
                    appendLog(logMessage, listOf(SINGLE_VALUE_ERROR))
                    return@launch
                }
                val result = keyValueStore.get(input) ?: KEY_NOT_SET_ERROR
                appendLog(logMessage, listOf(result))
            }

            Set -> {
                val (key, value) = inputValues.getOrNull(0) to inputValues.getOrNull(1)
                if (key == null || value == null || inputValues.size > 2) {
                    appendLog(logMessage, listOf(DOUBLE_VALUE_ERROR))
                    return@launch
                }
                keyValueStore.set(key = key, value = value)
                appendLog(logMessage)
            }

            Count -> {
                if (input == null || inputValues.size != 1) {
                    appendLog(logMessage, listOf(SINGLE_VALUE_ERROR))
                    return@launch
                }
                val result = keyValueStore.count(value = input).toString()
                appendLog(logMessage, listOf(result))
            }

            Delete -> {
                if (input == null || inputValues.size != 1) {
                    appendLog(logMessage, listOf(SINGLE_VALUE_ERROR))
                    return@launch
                }
                keyValueStore.delete(key = input)
                appendLog(logMessage)
            }

            Begin -> {
                keyValueStore.beginTransaction()
                appendLog(logMessage)
            }

            Commit -> {
                val error = runCatching { keyValueStore.commitTransaction() }.exceptionOrNull()
                appendLog(
                    logMessage,
                    if (error != null) listOf(NO_TRANSACTION_ERROR) else emptyList()
                )
            }

            Rollback -> {
                val error = runCatching { keyValueStore.rollbackTransaction() }.exceptionOrNull()
                appendLog(
                    logMessage,
                    if (error != null) listOf(NO_TRANSACTION_ERROR) else emptyList()
                )
            }
        }
    }

    private fun appendLog(message: String, additional: List<String> = emptyList()) {
        val currentState = (_state.value as CliState.Loaded)
        _state.value = currentState.copy(
            logLines = buildList {
                addAll(currentState.logLines)
                add(message)
                addAll(additional)
            }
        )
    }

    companion object {
        private val WELCOME_MESSAGE = """
            ███████████████
            █ S █ T █ O █ R █ E █
            ███████████████
            █ R █ E █ A █ D █ Y █
            ███████████████
            """.trimIndent()

        private const val INSTRUCTIONS_MESSAGE = "[ Select a command and provide a value ] "
        private const val SINGLE_VALUE_ERROR = "[!] Please provide exactly ONE value"
        private const val DOUBLE_VALUE_ERROR = "[!] Please provide exactly TWO values"
        private const val NO_TRANSACTION_ERROR = "No transaction"
        private const val NO_OPERATION_ERROR = "[!] Please select an operation first"
        private const val KEY_NOT_SET_ERROR = "Key not set"

        private const val LONG_HAPTIC_FEEDBACK = 500L
        private const val SHORT_HAPTIC_FEEDBACK = 100L
    }
}