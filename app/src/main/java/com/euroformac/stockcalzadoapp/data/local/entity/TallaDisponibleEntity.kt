package com.euroformac.stockcalzadoapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tallas_disponibles",
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
data class TallaDisponibleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productoId: Long,
    val talla: Double,
    val stockAlmacen: Int,
    val stockTienda: Int,
    val codigoBarras: String = ""
)
