package com.euroformac.stockcalzadoapp.domain.usecase

import com.euroformac.stockcalzadoapp.domain.model.LineaPedido
import com.euroformac.stockcalzadoapp.domain.model.Pedido
import com.euroformac.stockcalzadoapp.domain.repository.PedidoRepository

class CrearPedidoUseCase(
    private val repository: PedidoRepository
) {
    suspend operator fun invoke(pedido: Pedido, lineas: List<LineaPedido>) {
        repository.crearPedido(pedido, lineas)
    }
}
