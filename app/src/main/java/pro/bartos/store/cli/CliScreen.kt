package pro.bartos.store.cli

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pro.bartos.store.R
import pro.bartos.store.cli.CliOperation.TransactionOperation.Begin
import pro.bartos.store.cli.CliOperation.TransactionOperation.Commit
import pro.bartos.store.cli.CliOperation.TransactionOperation.Rollback
import pro.bartos.store.cli.CliOperation.ValueOperation.Count
import pro.bartos.store.cli.CliOperation.ValueOperation.Delete
import pro.bartos.store.cli.CliOperation.ValueOperation.Get
import pro.bartos.store.cli.CliOperation.ValueOperation.Set
import pro.bartos.store.ui.CommonComposableValues.spacer10
import pro.bartos.store.ui.CommonComposableValues.spacer15
import pro.bartos.store.ui.OperationButton
import pro.bartos.store.ui.OperationTitle
import pro.bartos.store.ui.TerminalText
import pro.bartos.store.ui.theme.StoreDemoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: CliState,
    handleIntent: (CliIntent) -> Unit,
) {
    val listState = rememberLazyListState()
    if (state !is CliState.Loaded) {
        // outside of task scope, other states like Empty, Error, Loading should be provided
        TODO("Implement other states")
    }

    if (state.showAlertDialog) {
        AlertDialog(
            onDismissRequest = { handleIntent(CliIntent.DialogDismissed) },
            title = { Text(text = stringResource(R.string.please_confirm)) },
            text = { Text(stringResource(R.string.are_you_sure)) },
            confirmButton = {
                Button(onClick = { handleIntent(CliIntent.DialogConfirmed) }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { handleIntent(CliIntent.DialogDismissed) }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black) // primary / secondary theme color
            .padding(15.dp),
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .weight(1f)
        ) {
            itemsIndexed(items = state.logLines) { index, line ->
                TerminalText(text = line, index = index)
            }
        }

        // when a new line is provided, scroll down to it
        LaunchedEffect(state.logLines) {
            if (state.logLines.isNotEmpty()) {
                listState.animateScrollToItem(state.logLines.size - 1)
            }
        }

        Spacer(modifier = Modifier.height(spacer10))

        Spacer(modifier = Modifier
            .height(2.dp)
            .fillMaxWidth()
            .background(Color.White)
        )

        Spacer(modifier = Modifier.height(spacer10))

        FlowRow(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            OperationTitle(stringResource(R.string.action))

            listOf(Get, Set, Delete, Count).forEach { operation ->
                OperationButton(text = operation.message,
                    selectedOperation = state.selectedOperation,
                    onClick = { handleIntent(CliIntent.ChangeOperation(operation)) }
                )
            }
        }

        Spacer(Modifier.height(spacer15))

        FlowRow(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
            OperationTitle(stringResource(R.string.transaction))
            listOf(Begin, Commit, Rollback).forEach { operation ->
                OperationButton(text = operation.message,
                    selectedOperation = state.selectedOperation,
                    onClick = { handleIntent(CliIntent.ChangeOperation(operation)) }
                )
            }
        }

        Spacer(Modifier.height(spacer10))

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.height(55.dp)
        ) {

            TextField(
                value = state.input,
                modifier = Modifier
                    .border(2.dp, Color.Green, RectangleShape)
                    .weight(1f),
                maxLines = 1,
                onValueChange = { handleIntent(CliIntent.ChangeInput(it)) },
                shape = RectangleShape,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        handleIntent(CliIntent.Execute)
                    }
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Black,
                    focusedContainerColor = Color.Black,
                    cursorColor = Color.Green,
                    focusedTextColor = Color.White,
                )
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .background(Color.Green)
                    .clickable { handleIntent(CliIntent.Execute) }
            ) {
                Text(text = stringResource(R.string.execute), color = Color.Black, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    StoreDemoTheme {
        MainScreen(
            state = CliState.Loaded(
                logLines = listOf("Testing"),
            ),
            handleIntent = {},
        )
    }
}