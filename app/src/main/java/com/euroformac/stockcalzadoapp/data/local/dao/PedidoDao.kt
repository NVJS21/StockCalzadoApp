package com.euroformac.stockcalzadoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.euroformac.stockcalzadoapp.data.local.entity.LineaPedidoEntity
import com.euroformac.stockcalzadoapp.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedido(pedido: PedidoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineasPedido(lineas: List<LineaPedidoEntity>)

    @Query("SELECT * FROM pedidos ORDER BY fechaCreacion DESC")
    fun getAllPedidos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM lineas_pedido WHERE pedidoId = :pedidoId")
    fun getLineasByPedido(pedidoId: Long): Flow<List<LineaPedidoEntity>>

    @Query("SELECT COUNT(*) FROM lineas_pedido lp JOIN pedidos p ON lp.pedidoId = p.id WHERE lp.productoId = :productoId AND p.estado = 'PENDIENTE'")
    suspend fun countPendingOrdersForProducto(productoId: Long): Int

    @Query("SELECT COUNT(*) FROM lineas_pedido lp JOIN pedidos p ON lp.pedidoId = p.id WHERE lp.productoId = :productoId AND lp.talla = :talla AND p.estado = 'PENDIENTE'")
    suspend fun countPendingOrdersForTalla(productoId: Long, talla: Double): Int
}
