package com.example.alguardianguyproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun RecordControlScreen() {
    val context = LocalContext.current
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
    }
}
