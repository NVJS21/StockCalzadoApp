package com.euroformac.stockcalzadoapp.domain.model

data class LineaPedido(
    val id: Long,
    val pedidoId: Long,
    val productoId: Long,
    val talla: Double,
    val cantidad: Int
)
