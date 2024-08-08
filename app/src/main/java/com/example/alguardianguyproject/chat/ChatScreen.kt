package com.example.alguardianguyproject.chat
//
//import android.content.res.Resources
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//data class Message(val text: String, val isUser: Boolean)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChatScreen(chatViewModel: ChatViewModel = viewModel()) {
//    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
//    var newMessageText by rememberSaveable { mutableStateOf(TextFieldValue("")) }
//
//    Column(modifier = Modifier.fillMaxSize()) {// Chat Messages Display
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//                .padding(16.dp),
//            reverseLayout = true, // Display messages from bottom to top
//        ) {
//            items(messages.reversed()) { message ->
//                MessageItem(message)
//            }
//        }
//        // Input Row
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//        ){
//            OutlinedTextField(
//                value = newMessageText,
//                onValueChange = { newMessageText = it },
//                label = { Text("Enter message") },
//                modifier = Modifier.weight(1f)
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Button(onClick = {
//                if (newMessageText.text.isNotBlank()) {
//                    chatViewModel.sendMessage(newMessageText.text)
//                    newMessageText = TextFieldValue("") // Clear input field
//                }
//            }){
//                Text("Send")
//            }
//        }
//    }
//}
//
//@Composable
//fun MessageItem(message: Message) {
//    println("message is user: $message.isUser")
//    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
//    val background = if (message.isUser) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.secondary
//
//    Box(
//        modifier = Modifier
//            .padding(8.dp)
//            .background(background, RoundedCornerShape(8.dp)), // Apply background to Box
//        contentAlignment = alignment
//    ) {
//        Text(
//            text = message.text,
//            modifier = Modifier.padding(16.dp),
//            fontWeight = if (message.isUser) FontWeight.Bold else FontWeight.Normal,
//            color = MaterialTheme.colorScheme.tertiary
//        )
//    }
//}