package com.twendev.samples.loadable


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun LazyQuoteScreen() {
    val viewModel by remember { mutableStateOf(LazyQuoteViewModel()) }

    viewModel.LoadableCompose {
        LazyColumn(
            Modifier.padding(15.dp)
        ) {
            items(viewModel.items) { item ->
                Text(text = item, modifier = Modifier.padding(0.dp, 10.dp))
            }

            item {
                Button(onClick = {
                    viewModel.actionWithLoading {
                        delay(1000L)
                        viewModel.items.add("quote${viewModel.index.intValue++}")
                    }
                }) {
                    Text("new quote")
                }
            }

            item {
                Button(onClick = {
                    viewModel.actionWithLoading(status = LoadingStatus.Lazy) {
                        delay(1000L)
                        viewModel.items.add("quote${viewModel.index.intValue++}")
                    }
                }) {
                    Text("new lazy quote")
                }
            }
        }
    }
}


enum class LoadingStatus {
    None,
    Lazy,
    Full
}

data class LazyLoadingUiState(
    val loading: LoadingStatus = LoadingStatus.None
) {
    fun isLoading() : Boolean {
        return loading != LoadingStatus.None
    }
}

class LazyQuoteViewModel() : LazyLoadableViewModel() {
    val items = mutableStateListOf("quote0")
    var index = mutableIntStateOf(1)
}

abstract class LazyLoadableViewModel() : ViewModel() {
    private val _loadingUiState = MutableStateFlow(LazyLoadingUiState())
    val loadingUiState = _loadingUiState.asStateFlow()

    fun actionWithLoading(status: LoadingStatus = LoadingStatus.Full, action: suspend () -> Unit) {
        setLoadingState(status)
        viewModelScope.launch {
            action()
            setLoadingState(LoadingStatus.None)
        }
    }

    @Composable
    fun LoadableCompose(content: @Composable () -> Unit) {
        val state = _loadingUiState.collectAsState()

        if (state.value.isLoading()) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
            }
        }

        if (state.value.loading != LoadingStatus.Full) {
            content()
        }
    }

    private fun setLoadingState(newState: LoadingStatus) {
        _loadingUiState.update { it.copy(loading = newState) }
    }
}
