package com.euroformac.stockcalzadoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String, // "INTERNO" (tienda pide a almacen), "EXTERNO" (tienda pide fuera)
    val estado: String, // "PENDIENTE", "COMPLETADO", "CANCELADO"
    val fechaCreacion: Long,
    val fechaFinalizacion: Long? = null,
    val notas: String
)
