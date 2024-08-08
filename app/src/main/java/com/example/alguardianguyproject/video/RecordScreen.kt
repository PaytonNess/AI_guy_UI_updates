package com.example.alguardianguyproject.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alguardianguyproject.MainActivity
import com.example.alguardianguyproject.R
import com.example.alguardianguyproject.RecordViewModel

@Composable
fun RecordScreen(recordViewModel: RecordViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by recordViewModel.uiState.collectAsState()
    val progress by recordViewModel.progress.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {// Chat Messages Display
        Text(
            text = "Preparing video...",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(10) { index -> // Replace 10 with the actual number of rows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Row ${index + 1} $progress",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(2.dp))

                    if (uiState > index) {
                        Image(
                            painter = painterResource(id = R.drawable.baked_goods_1), // Replace with your image resource
                            contentDescription = "Image",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    else {
                        Image(
                            painter = painterResource(id = R.drawable.baked_goods_2), // Replace with your image resource
                            contentDescription = "Image",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
        // Button at the bottom center
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { (context as MainActivity).startRecording() },
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            ) {
                Text("Chat")
            }
        }
        // Button at the bottom center
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { (context as MainActivity).stopRecording() },
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            ) {
                Text("stop")
            }
        }
    }
}
