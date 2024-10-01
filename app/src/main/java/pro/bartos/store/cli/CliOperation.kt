package pro.bartos.store.cli

sealed class CliOperation(open val message: String) {
    sealed class ValueOperation(override val message: String) : CliOperation(message) {
        data object Get : CliOperation("GET")
        data object Set : CliOperation("SET")
        data object Delete : CliOperation("DELETE")
        data object Count : CliOperation("COUNT")
    }

    sealed class TransactionOperation(override val message: String) : CliOperation(message) {
        data object Begin : CliOperation("BEGIN")
        data object Commit : CliOperation("COMMIT")
        data object Rollback : CliOperation("ROLLBACK")
    }
}