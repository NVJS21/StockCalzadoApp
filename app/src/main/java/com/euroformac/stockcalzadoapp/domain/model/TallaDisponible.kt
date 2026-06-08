package com.euroformac.stockcalzadoapp.domain.model

data class TallaDisponible(
    val id: Long,
    val productoId: Long,
    val talla: Double,
    val stockAlmacen: Int,
    val stockTienda: Int,
    val codigoBarras: String = ""
)
