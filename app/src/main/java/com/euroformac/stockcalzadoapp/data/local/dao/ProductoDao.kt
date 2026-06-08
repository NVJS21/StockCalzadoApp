package com.euroformac.stockcalzadoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.euroformac.stockcalzadoapp.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE barcode = :barcode")
    fun getProductoByBarcode(barcode: String): Flow<ProductoEntity?>

    @Query("SELECT * FROM productos WHERE id = :id")
    fun getProductoById(id: Long): Flow<ProductoEntity?>

    @Query("SELECT * FROM productos")
    fun getAllProductos(): Flow<List<ProductoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(productos: List<ProductoEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(producto: ProductoEntity): Long

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun deleteProductoById(id: Long)
}
