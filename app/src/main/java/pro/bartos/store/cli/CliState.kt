package pro.bartos.store.cli

sealed class CliState {

    data class Loaded(
        val logLines: List<String> = listOf(),
        val selectedOperation: CliOperation? = null,
        val input: String = "",
        val showAlertDialog: Boolean = false,
    ) : CliState()
}