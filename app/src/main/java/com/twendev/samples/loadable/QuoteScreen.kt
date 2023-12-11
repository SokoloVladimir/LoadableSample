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
fun QuoteScreen() {
    val viewModel by remember { mutableStateOf(QuoteViewModel()) }

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
        }
    }
}

class QuoteViewModel() : LoadableViewModel() {
    val items = mutableStateListOf("quote0")
    var index = mutableIntStateOf(1)
}

data class LoadingUiState(
    val loading: Boolean = false
)

abstract class LoadableViewModel() : ViewModel() {
    private val _loadingUiState = MutableStateFlow(LoadingUiState())
    val loadingUiState = _loadingUiState.asStateFlow()

    fun actionWithLoading(action: suspend () -> Unit) {
        setLoadingState(true)
        viewModelScope.launch {
            action()
            setLoadingState(false)
        }
    }

    @Composable
    fun LoadableCompose(content: @Composable () -> Unit) {
        val state = _loadingUiState.collectAsState()

        if (state.value.loading) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
            }
        } else {
            content()
        }
    }

    private fun setLoadingState(newState: Boolean) {
        _loadingUiState.update { it.copy(loading = newState) }
    }
}