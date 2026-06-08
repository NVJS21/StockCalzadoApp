package com.euroformac.stockcalzadoapp.domain.model

data class Movimiento(
    val id: Long,
    val productoId: Long,
    val talla: Double,
    val cantidad: Int,
    val tipo: String,
    val fecha: Long,
    val origen: String
)
