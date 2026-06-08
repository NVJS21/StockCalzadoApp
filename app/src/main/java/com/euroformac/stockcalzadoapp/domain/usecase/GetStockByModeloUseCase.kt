package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

class GetStockByModeloUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(productoId: Long): Flow<List<TallaDisponible>> {
        return repository.getTallasByProducto(productoId)
    }
}
