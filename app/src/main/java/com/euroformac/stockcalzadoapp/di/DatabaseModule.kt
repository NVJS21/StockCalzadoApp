package com.euroformac.stockcalzadoapp.di

import android.content.Context
import com.euroformac.stockcalzadoapp.data.local.StockDatabase
import com.euroformac.stockcalzadoapp.data.local.dao.MovimientoDao
import com.euroformac.stockcalzadoapp.data.local.dao.PedidoDao
import com.euroformac.stockcalzadoapp.data.local.dao.ProductoDao
import com.euroformac.stockcalzadoapp.data.local.dao.TallaDisponibleDao
import com.euroformac.stockcalzadoapp.data.repository.MovimientoRepositoryImpl
import com.euroformac.stockcalzadoapp.data.repository.PedidoRepositoryImpl
import com.euroformac.stockcalzadoapp.data.repository.ProductoRepositoryImpl
import com.euroformac.stockcalzadoapp.domain.repository.MovimientoRepository
import com.euroformac.stockcalzadoapp.domain.repository.PedidoRepository
import com.euroformac.stockcalzadoapp.domain.repository.ProductoRepository
import com.euroformac.stockcalzadoapp.domain.usecase.ActualizarStockUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetAllProductosUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetProductoByBarcodeUseCase
import com.euroformac.stockcalzadoapp.domain.usecase.GetStockByModeloUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ── Base de datos ──────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StockDatabase {
        return StockDatabase.getDatabase(context)
    }

    // ── DAOs ──────────────────────────────────────────────────────────────────

    @Provides
    fun provideProductoDao(db: StockDatabase): ProductoDao = db.productoDao()

    @Provides
    fun provideTallaDisponibleDao(db: StockDatabase): TallaDisponibleDao = db.tallaDisponibleDao()

    @Provides
    fun providePedidoDao(db: StockDatabase): PedidoDao = db.pedidoDao()

    @Provides
    fun provideMovimientoDao(db: StockDatabase): MovimientoDao = db.movimientoDao()

    // ── Repositorios ─────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideProductoRepository(
        productoDao: ProductoDao,
        tallaDisponibleDao: TallaDisponibleDao
    ): ProductoRepository = ProductoRepositoryImpl(productoDao, tallaDisponibleDao)

    @Provides
    @Singleton
    fun providePedidoRepository(
        pedidoDao: PedidoDao
    ): PedidoRepository = PedidoRepositoryImpl(pedidoDao)

    @Provides
    @Singleton
    fun provideMovimientoRepository(
        movimientoDao: MovimientoDao
    ): MovimientoRepository = MovimientoRepositoryImpl(movimientoDao)

    // ── Casos de uso ─────────────────────────────────────────────────────────

    @Provides
    fun provideGetAllProductosUseCase(
        repository: ProductoRepository
    ): GetAllProductosUseCase = GetAllProductosUseCase(repository)

    @Provides
    fun provideGetStockByModeloUseCase(
        repository: ProductoRepository
    ): GetStockByModeloUseCase = GetStockByModeloUseCase(repository)

    @Provides
    fun provideActualizarStockUseCase(
        repository: ProductoRepository
    ): ActualizarStockUseCase = ActualizarStockUseCase(repository)

    @Provides
    fun provideGetProductoByBarcodeUseCase(
        repository: ProductoRepository
    ): GetProductoByBarcodeUseCase = GetProductoByBarcodeUseCase(repository)

    @Provides
    fun provideGetProductoByIdUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.GetProductoByIdUseCase = com.euroformac.stockcalzadoapp.domain.usecase.GetProductoByIdUseCase(repository)

    @Provides
    fun provideGetTallaByBarcodeUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.GetTallaByBarcodeUseCase = com.euroformac.stockcalzadoapp.domain.usecase.GetTallaByBarcodeUseCase(repository)

    @Provides
    fun providePedidoAlmacenDao(db: StockDatabase): com.euroformac.stockcalzadoapp.data.local.dao.PedidoAlmacenDao = db.pedidoAlmacenDao()

    @Provides
    @Singleton
    fun providePedidoAlmacenRepository(
        dao: com.euroformac.stockcalzadoapp.data.local.dao.PedidoAlmacenDao
    ): com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository = com.euroformac.stockcalzadoapp.data.repository.PedidoAlmacenRepositoryImpl(dao)

    @Provides
    fun provideObtenerPedidosPendientesUseCase(
        repository: com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.ObtenerPedidosPendientesUseCase = com.euroformac.stockcalzadoapp.domain.usecase.ObtenerPedidosPendientesUseCase(repository)

    @Provides
    fun provideObtenerTodosLosPedidosUseCase(
        repository: com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.ObtenerTodosLosPedidosUseCase = com.euroformac.stockcalzadoapp.domain.usecase.ObtenerTodosLosPedidosUseCase(repository)

    @Provides
    fun provideCrearPedidoAlmacenUseCase(
        repository: com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.CrearPedidoAlmacenUseCase = com.euroformac.stockcalzadoapp.domain.usecase.CrearPedidoAlmacenUseCase(repository)

    @Provides
    fun provideCompletarPedidoAlmacenUseCase(
        repository: com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.CompletarPedidoAlmacenUseCase = com.euroformac.stockcalzadoapp.domain.usecase.CompletarPedidoAlmacenUseCase(repository)

    @Provides
    fun provideActualizarEstadoPedidoUseCase(
        repository: com.euroformac.stockcalzadoapp.domain.repository.PedidoAlmacenRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.ActualizarEstadoPedidoUseCase = com.euroformac.stockcalzadoapp.domain.usecase.ActualizarEstadoPedidoUseCase(repository)

    @Provides
    fun provideInsertProductoUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.InsertProductoUseCase = com.euroformac.stockcalzadoapp.domain.usecase.InsertProductoUseCase(repository)

    @Provides
    fun provideInsertTallaUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.InsertTallaUseCase = com.euroformac.stockcalzadoapp.domain.usecase.InsertTallaUseCase(repository)

    @Provides
    fun provideEliminarProductoUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.EliminarProductoUseCase = com.euroformac.stockcalzadoapp.domain.usecase.EliminarProductoUseCase(repository)

    @Provides
    fun provideEliminarTallaUseCase(
        repository: ProductoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.EliminarTallaUseCase = com.euroformac.stockcalzadoapp.domain.usecase.EliminarTallaUseCase(repository)

    @Provides
    fun provideCheckPendingOrdersUseCase(
        repository: PedidoRepository
    ): com.euroformac.stockcalzadoapp.domain.usecase.CheckPendingOrdersUseCase = com.euroformac.stockcalzadoapp.domain.usecase.CheckPendingOrdersUseCase(repository)
}
