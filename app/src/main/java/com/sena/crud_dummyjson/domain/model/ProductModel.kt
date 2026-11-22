package com.sena.crud_dummyjson.domain.model

data class ProductModel(
    val id: String = "",
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val imageUrl: String = "",
    val cantidad: Int = 0,
    val userId: String = ""
)
