package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

class GetTallaByBarcodeUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(barcode: String): Flow<TallaDisponible?> {
        return repository.getTallaByBarcode(barcode)
    }
}
