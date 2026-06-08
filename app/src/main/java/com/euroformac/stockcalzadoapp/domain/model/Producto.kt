package com.euroformac.stockcalzadoapp.domain.model

data class Producto(
    val id: Long,
    val barcode: String,
    val marca: String,
    val modelo: String,
    val color: String,
    val genero: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
)
