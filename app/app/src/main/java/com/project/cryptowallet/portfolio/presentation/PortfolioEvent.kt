package com.project.cryptowallet.portfolio.presentation

import com.project.cryptowallet.core.domain.util.Error

interface PortfolioEvent {
    data class Error(val error: com.project.cryptowallet.core.domain.util.Error): PortfolioEvent
}