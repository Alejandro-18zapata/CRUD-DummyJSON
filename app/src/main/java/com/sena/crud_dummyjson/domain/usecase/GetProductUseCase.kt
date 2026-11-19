package com.sena.crud_dummyjson.domain.usecase

import com.sena.crud_dummyjson.domain.model.ProductModel
import com.sena.crud_dummyjson.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): List<ProductModel> = repository.getProducts()

    suspend fun byId(id: Int): ProductModel = repository.getProductById(id)
}