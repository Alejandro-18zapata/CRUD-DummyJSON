package com.sena.crud_dummyjson.data.remote.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sena.crud_dummyjson.data.auth.FirebaseAuthDataSource
import com.sena.crud_dummyjson.domain.model.ProductModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProductDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authDataSource: FirebaseAuthDataSource
) {
    private val productsCollection
        get() = firestore.collection(COLLECTION_PRODUCTOS)

    fun observeProducts(): Flow<List<ProductModel>> = callbackFlow {
        authDataSource.ensureSignedIn()

        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.mapNotNull { it.toProduct() }.orEmpty())
        }

        awaitClose { listener.remove() }
    }

    suspend fun getProductById(id: String): ProductModel {
        authDataSource.ensureSignedIn()
        val snapshot = productsCollection.document(id).get().await()
        return snapshot.toProduct() ?: error("Producto no encontrado")
    }

    suspend fun createProduct(product: ProductModel): ProductModel {
        val userId = authDataSource.ensureSignedIn()
        val data = product.toFirestoreMap(userId)
        val document = productsCollection.add(data).await()
        return product.copy(id = document.id, userId = userId)
    }

    suspend fun updateProduct(product: ProductModel): ProductModel {
        require(product.id.isNotBlank()) { "El producto no tiene un id de Firestore" }
        val userId = authDataSource.ensureSignedIn()
        val data = product.toFirestoreMap(product.userId.ifBlank { userId })
        productsCollection.document(product.id).set(data).await()
        return product.copy(userId = data[FIELD_USER_ID] as String)
    }

    suspend fun deleteProduct(id: String): Boolean {
        require(id.isNotBlank()) { "El producto no tiene un id de Firestore" }
        authDataSource.ensureSignedIn()
        productsCollection.document(id).delete().await()
        return true
    }

    private fun DocumentSnapshot.toProduct(): ProductModel? {
        val nombre = getString(FIELD_NOMBRE) ?: return null
        return ProductModel(
            id = id,
            title = nombre,
            description = getString(FIELD_DESCRIPCION).orEmpty(),
            category = getString(FIELD_CATEGORIA).orEmpty(),
            price = (get(FIELD_PRECIO) as? Number)?.toDouble() ?: 0.0,
            imageUrl = getString(FIELD_IMAGE_URL).orEmpty(),
            cantidad = (get(FIELD_CANTIDAD) as? Number)?.toInt() ?: 0,
            userId = getString(FIELD_USER_ID).orEmpty()
        )
    }

    private fun ProductModel.toFirestoreMap(userId: String): Map<String, Any> = mapOf(
        FIELD_NOMBRE to title,
        FIELD_DESCRIPCION to description,
        FIELD_PRECIO to price,
        FIELD_CANTIDAD to cantidad,
        FIELD_USER_ID to userId,
        FIELD_CATEGORIA to category,
        FIELD_IMAGE_URL to imageUrl
    )

    private companion object {
        const val COLLECTION_PRODUCTOS = "productos"
        const val FIELD_NOMBRE = "nombre"
        const val FIELD_DESCRIPCION = "descripcion"
        const val FIELD_PRECIO = "precio"
        const val FIELD_CANTIDAD = "cantidad"
        const val FIELD_USER_ID = "userId"
        const val FIELD_CATEGORIA = "categoria"
        const val FIELD_IMAGE_URL = "imageUrl"
    }
}
