package dev.zt64.compose.pdf.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.zt64.compose.pdf.component.PdfColumn
import dev.zt64.compose.pdf.PdfState
import dev.zt64.compose.pdf.RemotePdfState
import dev.zt64.compose.pdf.rememberLocalPdfState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.io.File
import java.net.URL

@Composable
fun Application() {
    Theme {
        val errorIcon = rememberVectorPainter(Icons.Default.Error)
        val loadingIcon = rememberVectorPainter(Icons.Default.Refresh)
        var pdf: PdfState? by remember {
            mutableStateOf(null, referentialEqualityPolicy())
        }
        var url by remember {
            mutableStateOf("")
        }

        if (pdf == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                var showFilePicker by rememberSaveable { mutableStateOf(false) }

                PdfPicker(
                    show = showFilePicker,
                    onSelectFile = {
                        pdf = it
                        showFilePicker = false
                    }
                )

                Button(
                    onClick = { showFilePicker = true }
                ) {
                    Text("Select PDF file")
                }

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                    },
                    label = {
                        Text("URL")
                    }
                )

                Button(
                    onClick = {
                        pdf = RemotePdfState(url = URL(url), errorIcon, loadingIcon)
                    }
                ) {
                    Text("Load from url")
                }
            }
        } else {
            PdfScreen(
                pdf = pdf!!,
                onClickBack = { pdf = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfScreen(
    pdf: PdfState,
    onClickBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    var scale by rememberSaveable { mutableStateOf(1f) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Compose PDF viewer") },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { scale -= 0.1f }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = null)
                    }

                    IconButton(onClick = { scale += 0.1f }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .zoomable(rememberZoomableState())
        ) {
            val lazyListState = rememberLazyListState()

            PdfColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale),
                state = pdf,
                lazyListState = lazyListState,
            )

            val currentPage by remember {
                derivedStateOf { lazyListState.firstVisibleItemIndex + 1 }
            }

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                text = "Page $currentPage of ${pdf.pageCount}"
            )

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                lazyListState = lazyListState,
            )
        }
    }
}

@Composable
expect fun PdfPicker(
    show: Boolean,
    onSelectFile: (PdfState) -> Unit,
    fileExtensions: List<String> = listOf("pdf")
)

@Composable
expect fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState
)