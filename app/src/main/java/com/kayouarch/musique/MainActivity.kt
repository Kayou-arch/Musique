package com.kayouarch.musique

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kayouarch.musique.ui.LibraryScreen
import com.kayouarch.musique.ui.LoginScreen
import com.kayouarch.musique.ui.MainViewModel
import com.kayouarch.musique.ui.UiState
import com.kayouarch.musique.ui.theme.MusiqueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusiqueTheme {
                Surface(Modifier.fillMaxSize()) {
                    val vm: MainViewModel = viewModel()
                    val state by vm.state.collectAsStateWithLifecycle()

                    when (val s = state) {
                        UiState.Starting -> Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                        is UiState.Login -> LoginScreen(s, vm::login)
                        is UiState.Library -> LibraryScreen(s, vm::refresh, vm::logout)
                    }
                }
            }
        }
    }
}
