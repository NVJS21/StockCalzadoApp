package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

class GetAllProductosUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(): Flow<List<Producto>> {
        return repository.getAllProductos()
    }
}
