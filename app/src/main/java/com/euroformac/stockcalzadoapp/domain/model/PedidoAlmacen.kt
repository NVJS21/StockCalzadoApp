package com.euroformac.stockcalzadoapp.domain.model

data class PedidoAlmacen(
    val id: Long = 0,
    val codigoBarras: String,
    val nombreProducto: String,
    val talla: String,
    val color: String,
    val estado: String,
    val fechaCreacion: Long,
    val fechaFinalizacion: Long? = null
)
