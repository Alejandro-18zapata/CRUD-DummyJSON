package com.sena.crud_dummyjson.data.remote.dto.product

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatchProduct(
    @param:Json(name = "category")
    val category: String
)
