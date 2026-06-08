package com.euroformac.stockcalzadoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.euroformac.stockcalzadoapp.data.local.entity.TallaDisponibleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TallaDisponibleDao {
    @Query("SELECT * FROM tallas_disponibles WHERE productoId = :productoId")
    fun getTallasByProducto(productoId: Long): Flow<List<TallaDisponibleEntity>>

    @Query("SELECT * FROM tallas_disponibles WHERE codigoBarras = :barcode LIMIT 1")
    fun getTallaByBarcode(barcode: String): Flow<TallaDisponibleEntity?>

    @Query("SELECT * FROM tallas_disponibles WHERE productoId = :productoId AND talla = :talla")
    suspend fun getTalla(productoId: Long, talla: Double): TallaDisponibleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tallas: List<TallaDisponibleEntity>)

    @Update
    suspend fun updateTalla(talla: TallaDisponibleEntity)

    @Query("DELETE FROM tallas_disponibles WHERE id = :id")
    suspend fun deleteTallaById(id: Long)

    @Query("DELETE FROM tallas_disponibles WHERE productoId = :productoId")
    suspend fun deleteTallasByProductoId(productoId: Long)
}
