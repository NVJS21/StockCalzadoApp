package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
import kotlinx.coroutines.flow.Flow

class ObtenerPedidosPendientesUseCase(private val repository: PedidoAlmacenRepository) {
    operator fun invoke(): Flow<List<PedidoAlmacen>> = repository.obtenerPedidosPendientes()
}

class ObtenerTodosLosPedidosUseCase(private val repository: PedidoAlmacenRepository) {
    operator fun invoke(): Flow<List<PedidoAlmacen>> = repository.obtenerTodosLosPedidos()
}

class CrearPedidoAlmacenUseCase(private val repository: PedidoAlmacenRepository) {
    suspend operator fun invoke(pedido: PedidoAlmacen) = repository.crearPedido(pedido)
}

class CompletarPedidoAlmacenUseCase(private val repository: PedidoAlmacenRepository) {
    suspend operator fun invoke(codigoBarras: String) = repository.completarPedidoPorCodigo(codigoBarras)
}

class ActualizarEstadoPedidoUseCase(private val repository: PedidoAlmacenRepository) {
    suspend operator fun invoke(id: Long, nuevoEstado: String, fecha: Long) = repository.actualizarEstadoPedido(id, nuevoEstado, fecha)
}
