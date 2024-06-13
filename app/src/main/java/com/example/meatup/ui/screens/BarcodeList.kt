package com.example.meatup.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

data class Product(
    val barcode: String = "",
    val name: String = "",
    val image: String = "",
    val count: Long = 1
)

@Composable
fun BarcodeListScreen() {
    val db = Firebase.firestore
    var products by remember { mutableStateOf(listOf<Product>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            val productList = mutableListOf<Product>()
            for (doc in snapshot!!) {
                val product = doc.toObject(Product::class.java)
                productList.add(product)
            }
            products = productList
        }
    }

    LazyColumn {
        items(products.size) { index ->
            val product = products[index]
            var productName by remember { mutableStateOf(TextFieldValue(product.name)) }
            Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Barcode: ${product.barcode}",
                        style = MaterialTheme.typography.bodyLarge)

                    BasicTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        textStyle = MaterialTheme.typography.bodyLarge)

                    Image(painter = rememberAsyncImagePainter(product.image), contentDescription = null, modifier = Modifier.size(100.dp))
                    Text(
                        text = "Amount: ${product.count}",
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
