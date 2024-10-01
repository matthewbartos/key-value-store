package pro.bartos.store.cli

sealed class CliIntent {
    data object Execute : CliIntent()
    data class ChangeOperation(val operation: CliOperation) : CliIntent()
    data class ChangeInput(val input: String) : CliIntent()
    data object DialogDismissed : CliIntent()
    data object DialogConfirmed : CliIntent()
}