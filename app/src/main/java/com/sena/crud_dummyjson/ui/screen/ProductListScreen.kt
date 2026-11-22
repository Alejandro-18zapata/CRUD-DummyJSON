package com.sena.crud_dummyjson.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sena.crud_dummyjson.domain.model.ProductModel
import com.sena.crud_dummyjson.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(viewModel: ProductViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditor by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductModel?>(null) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Productos") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingProduct = null
                showEditor = true
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading && state.products.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.products.isEmpty() -> {
                    Text(
                        text = "No hay productos. Pulsa + para crear uno.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onClick = { viewModel.getProductById(product.id) },
                                onEdit = {
                                    editingProduct = product
                                    showEditor = true
                                },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                }
            }
        }
    }

    state.selectedProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedProduct() },
            title = { Text(product.title) },
            text = {
                Text(
                    "Categoría: ${product.category}\n" +
                        "Precio: ${product.price}\n\n" +
                        product.description
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSelectedProduct() }) { Text("Cerrar") }
            }
        )
    }

    if (showEditor) {
        ProductEditorDialog(
            product = editingProduct,
            onDismiss = { showEditor = false },
            onSave = { title, description, category, price ->
                val model = ProductModel(
                    id = editingProduct?.id.orEmpty(),
                    title = title,
                    description = description,
                    category = category,
                    price = price,
                    imageUrl = editingProduct?.imageUrl.orEmpty()
                )
                if (editingProduct == null) {
                    viewModel.createProduct(model)
                } else {
                    viewModel.updateProduct(model)
                }
                showEditor = false
            }
        )
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar \"${product.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(product.id)
                    productToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductCard(
    product: ProductModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${product.price}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun ProductEditorDialog(
    product: ProductModel?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double) -> Unit
) {
    var title by remember { mutableStateOf(product?.title.orEmpty()) }
    var description by remember { mutableStateOf(product?.description.orEmpty()) }
    var category by remember { mutableStateOf(product?.category.orEmpty()) }
    var price by remember { mutableStateOf(product?.price?.toString().orEmpty()) }
    var showValidationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    isError = showValidationError && title.isBlank()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.heightIn(min = 80.dp)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    isError = showValidationError && category.isBlank()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    singleLine = true,
                    isError = showValidationError && price.toDoubleOrNull() == null
                )
                if (showValidationError) {
                    Text(
                        text = "Completa título y categoría, y usa un precio válido.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedPrice = price.toDoubleOrNull()
                if (title.isBlank() || category.isBlank() || parsedPrice == null || parsedPrice <= 0.0) {
                    showValidationError = true
                } else {
                    onSave(title.trim(), description.trim(), category.trim(), parsedPrice)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
