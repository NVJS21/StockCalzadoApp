package com.euroformac.stockcalzadoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos_almacen")
data class PedidoAlmacenEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigoBarras: String,
    val nombreProducto: String,
    val talla: String,
    val color: String,
    val estado: String,
    val fechaCreacion: Long,
    val fechaFinalizacion: Long? = null
)
