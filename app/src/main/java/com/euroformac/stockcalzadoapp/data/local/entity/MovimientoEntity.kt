package com.euroformac.stockcalzadoapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productoId")
    ]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productoId: Long,
    val talla: Double,
    val cantidad: Int,
    val tipo: String, // "ENTRADA", "SALIDA"
    val fecha: Long,
    val origen: String // "ALMACEN", "TIENDA"
)
