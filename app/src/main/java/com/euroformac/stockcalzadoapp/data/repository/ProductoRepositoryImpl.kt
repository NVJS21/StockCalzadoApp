package com.euroformac.stockcalzadoapp.data.repository

import com.euroformac.stockcalzadoapp.data.local.dao.ProductoDao
import com.euroformac.stockcalzadoapp.data.local.dao.TallaDisponibleDao
import com.euroformac.stockcalzadoapp.domain.model.Producto
import com.euroformac.stockcalzadoapp.domain.model.TallaDisponible
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductoRepositoryImpl(
    private val productoDao: ProductoDao,
    private val tallaDisponibleDao: TallaDisponibleDao
) : ProductoRepository {

    override fun getProductoByBarcode(barcode: String): Flow<Producto?> {
        return productoDao.getProductoByBarcode(barcode).map { entity ->
            entity?.let {
                Producto(
                    id = it.id,
                    barcode = it.barcode,
                    marca = it.marca,
                    modelo = it.modelo,
                    color = it.color,
                    genero = it.genero,
                    descripcion = it.descripcion,
                    precio = it.precio,
                    imagenUrl = it.imagenUrl
                )
            }
        }
    }

    override fun getProductoById(id: Long): Flow<Producto?> {
        return productoDao.getProductoById(id).map { entity ->
            entity?.let {
                Producto(
                    id = it.id,
                    barcode = it.barcode,
                    marca = it.marca,
                    modelo = it.modelo,
                    color = it.color,
                    genero = it.genero,
                    descripcion = it.descripcion,
                    precio = it.precio,
                    imagenUrl = it.imagenUrl
                )
            }
        }
    }

    override fun getAllProductos(): Flow<List<Producto>> {
        return productoDao.getAllProductos().map { list ->
            list.map { entity ->
                Producto(
                    id = entity.id,
                    barcode = entity.barcode,
                    marca = entity.marca,
                    modelo = entity.modelo,
                    color = entity.color,
                    genero = entity.genero,
                    descripcion = entity.descripcion,
                    precio = entity.precio,
                    imagenUrl = entity.imagenUrl
                )
            }
        }
    }

    override fun getTallasByProducto(productoId: Long): Flow<List<TallaDisponible>> {
        return tallaDisponibleDao.getTallasByProducto(productoId).map { list ->
            list.map { entity ->
                TallaDisponible(
                    id = entity.id,
                    productoId = entity.productoId,
                    talla = entity.talla,
                    stockAlmacen = entity.stockAlmacen,
                    stockTienda = entity.stockTienda,
                    codigoBarras = entity.codigoBarras
                )
            }
        }
    }

    override fun getTallaByBarcode(barcode: String): Flow<TallaDisponible?> {
        return tallaDisponibleDao.getTallaByBarcode(barcode).map { entity ->
            entity?.let {
                TallaDisponible(
                    id = it.id,
                    productoId = it.productoId,
                    talla = it.talla,
                    stockAlmacen = it.stockAlmacen,
                    stockTienda = it.stockTienda,
                    codigoBarras = it.codigoBarras
                )
            }
        }
    }

    override suspend fun updateTalla(talla: TallaDisponible) {
        tallaDisponibleDao.updateTalla(
            com.euroformac.stockcalzadoapp.data.local.entity.TallaDisponibleEntity(
                id = talla.id,
                productoId = talla.productoId,
                talla = talla.talla,
                stockAlmacen = talla.stockAlmacen,
                stockTienda = talla.stockTienda,
                codigoBarras = talla.codigoBarras
            )
        )
    }

    override suspend fun insertProducto(producto: Producto): Long {
        return productoDao.insert(
            com.euroformac.stockcalzadoapp.data.local.entity.ProductoEntity(
                barcode = producto.barcode,
                marca = producto.marca,
                modelo = producto.modelo,
                color = producto.color,
                genero = producto.genero,
                descripcion = producto.descripcion,
                precio = producto.precio,
                imagenUrl = producto.imagenUrl
            )
        )
    }

    override suspend fun insertTalla(talla: TallaDisponible) {
        tallaDisponibleDao.insertAll(listOf(
            com.euroformac.stockcalzadoapp.data.local.entity.TallaDisponibleEntity(
                productoId = talla.productoId,
                talla = talla.talla,
                stockAlmacen = talla.stockAlmacen,
                stockTienda = talla.stockTienda,
                codigoBarras = talla.codigoBarras
            )
        ))
    }
    
    override suspend fun deleteProducto(productoId: Long) {
        tallaDisponibleDao.deleteTallasByProductoId(productoId)
        productoDao.deleteProductoById(productoId)
    }

    override suspend fun deleteTalla(tallaId: Long) {
        tallaDisponibleDao.deleteTallaById(tallaId)
    }
}
