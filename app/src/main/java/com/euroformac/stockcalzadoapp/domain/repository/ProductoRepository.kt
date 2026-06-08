package com.euroformac.stockcalzadoapp.domain.repository

import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    fun getProductoByBarcode(barcode: String): Flow<Producto?>
    fun getProductoById(id: Long): Flow<Producto?>
    fun getAllProductos(): Flow<List<Producto>>
    fun getTallasByProducto(productoId: Long): Flow<List<TallaDisponible>>
    fun getTallaByBarcode(barcode: String): Flow<TallaDisponible?>
    suspend fun updateTalla(talla: TallaDisponible)
    suspend fun insertProducto(producto: Producto): Long
    suspend fun insertTalla(talla: TallaDisponible)
    suspend fun deleteProducto(productoId: Long)
    suspend fun deleteTalla(tallaId: Long)
}
