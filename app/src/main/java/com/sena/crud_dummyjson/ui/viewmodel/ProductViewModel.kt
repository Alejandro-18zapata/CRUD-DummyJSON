package com.sena.crud_dummyjson.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.crud_dummyjson.domain.model.ProductModel
import com.sena.crud_dummyjson.domain.repository.ProductRepository
import com.sena.crud_dummyjson.domain.usecase.GetProductUseCase
import com.sena.crud_dummyjson.ui.state.ProductUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = ProductUIState())
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { getProductUseCase() }
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, products = list) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun getProductById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { getProductUseCase.byId(id) }
                .onSuccess { product ->
                    _uiState.update { it.copy(isLoading = false, selectedProduct = product) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun createProduct(product: ProductModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.createProduct(product) }
                .onSuccess {
                    loadProducts()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun updateProduct(product: ProductModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.updateProduct(product) }
                .onSuccess {
                    loadProducts()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(id) }
                .onSuccess {
                    loadProducts()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSelectedProduct() {
        _uiState.update { it.copy(selectedProduct = null) }
    }
} 