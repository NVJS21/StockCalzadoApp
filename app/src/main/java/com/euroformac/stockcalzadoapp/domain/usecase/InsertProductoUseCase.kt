package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository

class InsertProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(producto: Producto): Long = repository.insertProducto(producto)
}
