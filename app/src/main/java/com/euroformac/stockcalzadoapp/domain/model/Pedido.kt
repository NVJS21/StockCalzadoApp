package com.euroformac.stockcalzadoapp.domain.model

data class Pedido(
    val id: Long,
    val tipo: String, // INTERNO, EXTERNO
    val estado: String,
    val fechaCreacion: Long,
    val notas: String
)
