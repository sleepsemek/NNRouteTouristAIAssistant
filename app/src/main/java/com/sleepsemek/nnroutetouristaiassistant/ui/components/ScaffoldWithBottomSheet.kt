package com.sleepsemek.nnroutetouristaiassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldWithBottomSheet(
    sheetController: BottomSheetController,
    mapContent: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    BottomSheetScaffold(
        scaffoldState = sheetController.scaffoldState,
        sheetContent = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    sheetContent()
                }
            }
        },
        sheetPeekHeight = 110.dp,
        sheetDragHandle = { BottomSheetDragHandle() },
        sheetSwipeEnabled = true,
        sheetShape = MaterialTheme.shapes.extraLarge,
    ) {
        mapContent()
    }
}

@Composable
fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(64.dp)
            .height(3.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetController(
    val scaffoldState: BottomSheetScaffoldState,
    private val scope: CoroutineScope
) {
    fun expand() {
        scope.launch {
            scaffoldState.bottomSheetState.expand()
        }
    }

    fun collapse() {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    fun hide() {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetController(): BottomSheetController {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    return remember { BottomSheetController(scaffoldState, scope) }
}
