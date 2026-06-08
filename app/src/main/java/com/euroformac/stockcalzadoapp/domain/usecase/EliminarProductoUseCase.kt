package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository

class EliminarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(productoId: Long) {
        repository.deleteProducto(productoId)
    }
}
