package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow

class GetProductoByBarcodeUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(barcode: String): Flow<Producto?> {
        return repository.getProductoByBarcode(barcode)
    }
}
