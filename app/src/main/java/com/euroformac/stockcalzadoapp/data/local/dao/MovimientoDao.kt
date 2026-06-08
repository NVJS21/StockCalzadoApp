package com.euroformac.stockcalzadoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.euroformac.stockcalzadoapp.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovimiento(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun getAllMovimientos(): Flow<List<MovimientoEntity>>
    
    @Query("SELECT * FROM movimientos WHERE productoId = :productoId ORDER BY fecha DESC")
    fun getMovimientosByProducto(productoId: Long): Flow<List<MovimientoEntity>>
}
