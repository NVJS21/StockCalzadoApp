package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository

class ActualizarStockUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(talla: TallaDisponible) {
        repository.updateTalla(talla)
    }
}
