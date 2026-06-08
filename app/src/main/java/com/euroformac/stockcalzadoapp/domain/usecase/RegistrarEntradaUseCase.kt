package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Movimiento
import com.euroformac.stockcalzadoapp.domain.repository.MovimientoRepository

class RegistrarEntradaUseCase(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(movimiento: Movimiento) {
        if (movimiento.tipo != "ENTRADA") throw IllegalArgumentException("Tipo de movimiento debe ser ENTRADA")
        repository.registrarMovimiento(movimiento)
    }
}
