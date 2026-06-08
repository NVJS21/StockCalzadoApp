package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository

class EliminarTallaUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(tallaId: Long) {
        repository.deleteTalla(tallaId)
    }
}
