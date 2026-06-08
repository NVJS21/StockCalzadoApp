package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

class GetProductoByIdUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(id: Long): Flow<Producto?> {
        return repository.getProductoById(id)
    }
}
