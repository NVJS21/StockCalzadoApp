package com.euroformac.stockcalzadoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.euroformac.stockcalzadoapp.data.local.entity.PedidoAlmacenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoAlmacenDao {
    @Query("SELECT * FROM pedidos_almacen WHERE estado = 'PENDIENTE' ORDER BY fechaCreacion DESC")
    fun obtenerPedidosPendientes(): Flow<List<PedidoAlmacenEntity>>

    @Query("SELECT * FROM pedidos_almacen ORDER BY fechaCreacion DESC")
    fun obtenerTodosLosPedidos(): Flow<List<PedidoAlmacenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPedido(pedido: PedidoAlmacenEntity)

    @Query("UPDATE pedidos_almacen SET estado = :nuevoEstado, fechaFinalizacion = :fecha WHERE id = :id")
    suspend fun actualizarEstadoPedido(id: Long, nuevoEstado: String, fecha: Long)

    @Query("UPDATE pedidos_almacen SET estado = 'COMPLETADO' WHERE codigoBarras = :codigoBarras AND estado = 'PENDIENTE'")
    suspend fun marcarPedidoCompletado(codigoBarras: String)
}
