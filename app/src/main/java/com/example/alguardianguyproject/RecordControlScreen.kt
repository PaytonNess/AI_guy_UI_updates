package com.example.alguardianguyproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alguardianguyproject.video.VideoUploadUiState

@Composable
fun RecordControlScreen(recordViewModel: RecordViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by recordViewModel.uiState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {

            Button(onClick = {
                (context as MainActivity).startRecording()
            }) {
                Text("Start Recording")
            }
        }
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            Button(onClick = {
                (context as MainActivity).stopRecording()
            }) {
                Text("Stop Recording")
            }
        }
        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            if (uiState > 0) {
                Text(
                    text = "text: $uiState",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else {
                Text(
                    text = "text: $uiState",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
