package com.project.cryptowallet.portfolio.presentation

interface PortfolioEvent {
    data class Error(val error: Throwable): PortfolioEvent
}