package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Movimiento
import com.euroformac.stockcalzadoapp.domain.repository.MovimientoRepository

class RegistrarSalidaUseCase(
    private val repository: MovimientoRepository
) {
    suspend operator fun invoke(movimiento: Movimiento) {
        if (movimiento.tipo != "SALIDA") throw IllegalArgumentException("Tipo de movimiento debe ser SALIDA")
        repository.registrarMovimiento(movimiento)
    }
}
