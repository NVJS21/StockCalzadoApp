package com.euroformac.stockcalzadoapp.presentation.almacen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.euroformac.stockcalzadoapp.databinding.ItemProductoBinding
import com.euroformac.stockcalzadoapp.domain.model.Producto

data class ProductoConStock(
    val producto: Producto,
    val stockAlmacenTotal: Int
)

class ProductoAdapter(
    private val onVerTallasClick: (Producto) -> Unit,
    private val onAumentarStockClick: (Producto) -> Unit,
    private val onDisminuirStockClick: (Producto) -> Unit
) : ListAdapter<ProductoConStock, ProductoAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    inner class ProductoViewHolder(
        private val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProductoConStock) {
            val producto = item.producto
            binding.apply {
                tvItemMarcaModelo.text = "${producto.marca} ${producto.modelo}"
                tvItemPrecio.text = "%.0f€".format(producto.precio)
                tvItemColor.text = "Color: ${producto.color} · ${producto.genero}"
                tvItemStockTotal.text = item.stockAlmacenTotal.toString()

                val imgUrl = producto.imagenUrl
                if (imgUrl.isNotEmpty() && (imgUrl.startsWith("content://") || imgUrl.startsWith("/") || imgUrl.contains(":"))) {
                    try {
                        val uri = if (imgUrl.startsWith("/")) {
                            android.net.Uri.fromFile(java.io.File(imgUrl))
                        } else {
                            android.net.Uri.parse(imgUrl)
                        }
                        ivProducto.setImageURI(uri)
                    } catch (e: Exception) {
                        ivProducto.setImageResource(com.euroformac.stockcalzadoapp.R.drawable.producto_placeholder)
                    }
                } else {
                    val resId = root.context.resources.getIdentifier(
                        imgUrl, "drawable", root.context.packageName
                    )
                    if (resId != 0) {
                        ivProducto.setImageResource(resId)
                    } else {
                        ivProducto.setImageResource(com.euroformac.stockcalzadoapp.R.drawable.producto_placeholder)
                    }
                }

                btnVerTallas.setOnClickListener { onVerTallasClick(producto) }
                btnAumentar.setOnClickListener { onAumentarStockClick(producto) }
                btnDisminuir.setOnClickListener { onDisminuirStockClick(producto) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ProductoDiffCallback : DiffUtil.ItemCallback<ProductoConStock>() {
        override fun areItemsTheSame(oldItem: ProductoConStock, newItem: ProductoConStock): Boolean =
            oldItem.producto.id == newItem.producto.id

        override fun areContentsTheSame(oldItem: ProductoConStock, newItem: ProductoConStock): Boolean =
            oldItem == newItem
    }
}
