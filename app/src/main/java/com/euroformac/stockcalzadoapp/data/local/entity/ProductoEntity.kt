package com.euroformac.stockcalzadoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val marca: String,
    val modelo: String,
    val color: String,
    val genero: String,
    val descripcion: String,
    val precio: Double,
    val imagenUrl: String
)
