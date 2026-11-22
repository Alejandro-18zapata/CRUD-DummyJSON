package com.sena.crud_dummyjson.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.sena.crud_dummyjson.domain.model.ProductModel
import com.sena.crud_dummyjson.domain.repository.ProductRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repositorio que usa Cloud Firestore cuando la app está conectada a Firebase.
 * Si no existe google-services.json, delega en el repositorio Retrofit existente.
 */
class FirestoreProductRepository @Inject constructor(
    private val fallback: ProductRepositoryImpl
) : ProductRepository {

    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    override suspend fun getProducts(): List<ProductModel> {
        val db = firestore ?: return fallback.getProducts()
        return db.collection(COLLECTION)
            .orderBy("title")
            .get()
            .await()
            .documents
            .mapNotNull { it.toProduct() }
    }

    override suspend fun getProductById(id: Int): ProductModel {
        val db = firestore ?: return fallback.getProductById(id)
        return db.collection(COLLECTION)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toProduct()
            ?: throw NoSuchElementException("Producto $id no encontrado")
    }

    override suspend fun createProduct(product: ProductModel): ProductModel {
        val db = firestore ?: return fallback.createProduct(product)
        val doc = db.collection(COLLECTION).document()
        val saved = product.copy(id = doc.id.hashCode())
        doc.set(saved.toMap()).await()
        return saved
    }

    override suspend fun updateProduct(product: ProductModel): ProductModel {
        val db = firestore ?: return fallback.updateProduct(product)
        val doc = db.collection(COLLECTION)
            .whereEqualTo("id", product.id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: throw NoSuchElementException("Producto ${product.id} no encontrado")
        doc.reference.update(product.toMap()).await()
        return product
    }

    override suspend fun deleteProduct(id: Int): Boolean {
        val db = firestore ?: return fallback.deleteProduct(id)
        val doc = db.collection(COLLECTION)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: throw NoSuchElementException("Producto $id no encontrado")
        doc.reference.delete().await()
        return true
    }

    private fun ProductModel.toMap() = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "category" to category,
        "price" to price,
        "imageUrl" to imageUrl
    )

    private fun DocumentSnapshot.toProduct(): ProductModel? {
        val id = getLong("id")?.toInt() ?: return null
        val title = getString("title") ?: return null
        return ProductModel(
            id = id,
            title = title,
            description = getString("description").orEmpty(),
            category = getString("category").orEmpty(),
            price = getDouble("price") ?: 0.0,
            imageUrl = getString("imageUrl").orEmpty()
        )
    }

    private companion object {
        const val COLLECTION = "products"
    }
}
