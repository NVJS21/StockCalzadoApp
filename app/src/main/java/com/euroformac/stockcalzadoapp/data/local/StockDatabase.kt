package com.euroformac.stockcalzadoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.euroformac.stockcalzadoapp.data.local.dao.*
import com.euroformac.stockcalzadoapp.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductoEntity::class,
        TallaDisponibleEntity::class,
        PedidoEntity::class,
        LineaPedidoEntity::class,
        MovimientoEntity::class,
        PedidoAlmacenEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun tallaDisponibleDao(): TallaDisponibleDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun pedidoAlmacenDao(): PedidoAlmacenDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.productoDao(), database.tallaDisponibleDao())
                    }
                }
            }

            suspend fun populateDatabase(productoDao: ProductoDao, tallaDisponibleDao: TallaDisponibleDao) {
                // Producto 1
                val p1 = ProductoEntity(1, "8412345678901", "Nike", "Air Max 270", "Blanco/Negro", "Unisex", "Zapatilla urbana", 150.0, "nike_air_max_270")
                // Producto 2
                val p2 = ProductoEntity(2, "8412345678902", "Adidas", "Yeezy Boost 350", "Gris", "Unisex", "Diseño exclusivo", 220.0, "adidas_yeezy_boost_350")
                // Producto 3
                val p3 = ProductoEntity(3, "8412345678903", "New Balance", "550", "Blanco/Verde", "Hombre", "Estilo retro", 130.0, "new_balance_550")
                
                productoDao.insertAll(listOf(p1, p2, p3))

                val tallas = mutableListOf<TallaDisponibleEntity>()
                
                // Tallas Producto 1
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 38.0, stockAlmacen = 5, stockTienda = 0, codigoBarras = "8430000000010"))
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 39.0, stockAlmacen = 4, stockTienda = 0, codigoBarras = "8430000000011"))
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 40.0, stockAlmacen = 5, stockTienda = 0, codigoBarras = "8430000000012"))
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 41.0, stockAlmacen = 1, stockTienda = 0, codigoBarras = "8430000000013"))
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 42.0, stockAlmacen = 3, stockTienda = 0, codigoBarras = "8430000000014"))
                tallas.add(TallaDisponibleEntity(productoId = 1, talla = 43.0, stockAlmacen = 1, stockTienda = 0, codigoBarras = "8430000000015"))

                // Tallas Producto 2
                tallas.add(TallaDisponibleEntity(productoId = 2, talla = 40.0, stockAlmacen = 4, stockTienda = 0, codigoBarras = "8430000000020"))
                tallas.add(TallaDisponibleEntity(productoId = 2, talla = 41.0, stockAlmacen = 6, stockTienda = 0, codigoBarras = "8430000000021"))
                tallas.add(TallaDisponibleEntity(productoId = 2, talla = 42.0, stockAlmacen = 2, stockTienda = 0, codigoBarras = "8430000000022"))
                tallas.add(TallaDisponibleEntity(productoId = 2, talla = 43.0, stockAlmacen = 3, stockTienda = 0, codigoBarras = "8430000000023"))

                // Tallas Producto 3
                tallas.add(TallaDisponibleEntity(productoId = 3, talla = 39.0, stockAlmacen = 2, stockTienda = 0, codigoBarras = "8430000000030"))
                tallas.add(TallaDisponibleEntity(productoId = 3, talla = 40.0, stockAlmacen = 3, stockTienda = 0, codigoBarras = "8430000000031"))
                tallas.add(TallaDisponibleEntity(productoId = 3, talla = 41.0, stockAlmacen = 1, stockTienda = 0, codigoBarras = "8430000000032"))
                tallas.add(TallaDisponibleEntity(productoId = 3, talla = 42.0, stockAlmacen = 2, stockTienda = 0, codigoBarras = "8430000000033"))

                tallaDisponibleDao.insertAll(tallas)
            }
        }
    }
}
