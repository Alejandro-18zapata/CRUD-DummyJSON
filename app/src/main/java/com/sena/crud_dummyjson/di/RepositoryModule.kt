package com.sena.crud_dummyjson.di

import com.sena.crud_dummyjson.data.repository.FirestoreProductRepository
import com.sena.crud_dummyjson.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: FirestoreProductRepository
    ): ProductRepository
}