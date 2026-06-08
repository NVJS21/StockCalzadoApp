package com.euroformac.stockcalzadoapp.presentation.almacen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.euroformac.stockcalzadoapp.R
import com.euroformac.stockcalzadoapp.databinding.ItemPedidoAlmacenBinding
import com.euroformac.stockcalzadoapp.domain.model.PedidoAlmacen
import kotlinx.coroutines.*

class PedidoAdapter(
    private val onPedidoClick: (PedidoAlmacen) -> Unit
) : ListAdapter<PedidoAlmacen, PedidoAdapter.PedidoViewHolder>(PedidoDiffCallback()) {

    inner class PedidoViewHolder(
        private val binding: ItemPedidoAlmacenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var timerJob: Job? = null

        fun bind(item: PedidoAlmacen) {
            timerJob?.cancel()
            binding.apply {
                tvPedidoNombre.text = item.nombreProducto
                tvPedidoDetalle.text = "Talla: ${item.talla} | Color: ${item.color} | Ref: ${item.codigoBarras}"
                tvPedidoEstado.text = item.estado

                when (item.estado) {
                    "PENDIENTE" -> {
                        root.strokeColor = ContextCompat.getColor(root.context, R.color.neon_purple)
                        ivPedidoIcono.setImageResource(android.R.drawable.ic_dialog_info)
                        ivPedidoIcono.setColorFilter(ContextCompat.getColor(root.context, R.color.neon_purple))
                        tvPedidoEstado.setTextColor(ContextCompat.getColor(root.context, R.color.neon_purple))
                        
                        timerJob = CoroutineScope(Dispatchers.Main).launch {
                            while (isActive) {
                                val diff = System.currentTimeMillis() - item.fechaCreacion
                                tvPedidoTiempo.text = formatDuration(diff)
                                delay(1000)
                            }
                        }
                        
                        root.setOnClickListener { onPedidoClick(item) }
                    }
                    "COMPLETADO" -> {
                        root.strokeColor = ContextCompat.getColor(root.context, R.color.toxic_green)
                        ivPedidoIcono.setImageResource(android.R.drawable.checkbox_on_background)
                        ivPedidoIcono.setColorFilter(ContextCompat.getColor(root.context, R.color.toxic_green))
                        tvPedidoEstado.setTextColor(ContextCompat.getColor(root.context, R.color.toxic_green))
                        tvPedidoEstado.text = "✓ COMPLETADO"
                        
                        val diff = (item.fechaFinalizacion ?: System.currentTimeMillis()) - item.fechaCreacion
                        tvPedidoTiempo.text = "Tiempo: ${formatDuration(diff)}"
                        
                        root.setOnClickListener(null)
                    }
                    "FALLIDO" -> {
                        root.strokeColor = ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                        ivPedidoIcono.setImageResource(android.R.drawable.ic_delete)
                        ivPedidoIcono.setColorFilter(ContextCompat.getColor(root.context, android.R.color.holo_red_dark))
                        tvPedidoEstado.setTextColor(ContextCompat.getColor(root.context, android.R.color.holo_red_dark))
                        tvPedidoEstado.text = "✕ FALLIDO"
                        
                        val diff = (item.fechaFinalizacion ?: System.currentTimeMillis()) - item.fechaCreacion
                        tvPedidoTiempo.text = "Tiempo: ${formatDuration(diff)}"
                        
                        root.setOnClickListener(null)
                    }
                }
            }
        }

        fun cancelTimer() {
            timerJob?.cancel()
            timerJob = null
        }

        private fun formatDuration(millis: Long): String {
            val totalSeconds = Math.max(0, millis / 1000)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoAlmacenBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: PedidoViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelTimer()
    }

    override fun onViewDetachedFromWindow(holder: PedidoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.cancelTimer()
    }

    private class PedidoDiffCallback : DiffUtil.ItemCallback<PedidoAlmacen>() {
        override fun areItemsTheSame(oldItem: PedidoAlmacen, newItem: PedidoAlmacen): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PedidoAlmacen, newItem: PedidoAlmacen): Boolean =
            oldItem == newItem
    }
}
