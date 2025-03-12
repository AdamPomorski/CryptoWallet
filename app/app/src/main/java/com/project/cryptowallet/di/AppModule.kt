package com.project.cryptowallet.di

import androidx.room.Room
import com.project.cryptowallet.core.data.networking.HttpClientFactory
import com.project.cryptowallet.crypto_list.data.repository.CoinDataSourceImpl
import com.project.cryptowallet.crypto_list.domain.CoinDataSource
import com.project.cryptowallet.crypto_list.presentation.coin_list.CoinListViewModel
import com.project.cryptowallet.login.domain.AuthorizationRepository
import com.project.cryptowallet.login.presentation.AuthViewModel
import com.project.cryptowallet.portfolio.data.OperationRepositoryImpl
import com.project.cryptowallet.portfolio.data.PriceRepositoryImpl
import com.project.cryptowallet.portfolio.data.local.PortfolioDatabase
import com.project.cryptowallet.portfolio.domain.OperationRepository
import com.project.cryptowallet.portfolio.domain.PriceRepository
import com.project.cryptowallet.portfolio.domain.use_cases.CalculatePortfolioUseCase
import com.project.cryptowallet.portfolio.domain.use_cases.ManageAssetsUseCase
import com.project.cryptowallet.portfolio.presentation.PortfolioViewModel
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { HttpClientFactory.create(CIO.create()) }
    singleOf(::CoinDataSourceImpl).bind<CoinDataSource>()
}

val coinListModule = module {

    viewModelOf(::CoinListViewModel)


}

val authModule = module {
    single { AuthorizationRepository() }
    viewModel { AuthViewModel(get()) }
}

val portfolioModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            PortfolioDatabase::class.java,
            "portfolio_db"
        ).build()
    }
    single { get<PortfolioDatabase>().operationsDao }
    single { get<PortfolioDatabase>().historicalPricesDao }
    single<OperationRepository> { OperationRepositoryImpl(get()) }
    single<PriceRepository> { PriceRepositoryImpl(get(), get()) }
    factory { CalculatePortfolioUseCase(get(), get()) }
    factory { ManageAssetsUseCase(get(), get<CoinDataSource>() ) }
    viewModel { PortfolioViewModel(get(), get(), get(), get())  }
}



