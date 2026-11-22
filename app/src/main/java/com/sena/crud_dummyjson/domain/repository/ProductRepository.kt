package com.sena.crud_dummyjson.domain.repository

import com.sena.crud_dummyjson.domain.model.ProductModel
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<ProductModel>>
    suspend fun getProductById(id: String): ProductModel
    suspend fun createProduct(product: ProductModel): ProductModel
    suspend fun updateProduct(product: ProductModel): ProductModel
    suspend fun deleteProduct(id: String): Boolean
}
