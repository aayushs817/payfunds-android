package com.payfunds.wallet.modules.market.overview.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.horizontalsystems.marketkit.models.CoinCategory
import com.payfunds.wallet.modules.market.overview.MarketOverviewModule
import com.payfunds.wallet.ui.compose.components.CategoryCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSectorsBoardView(
    board: MarketOverviewModule.TopSectorsBoard,
    onItemClick: (CoinCategory) -> Unit
) {
    MarketsSectionHeader(
        title = board.title,
        icon = painterResource(board.iconRes),
    )

    MarketsHorizontalCards(board.items.size) {
        val category = board.items[it]
        CategoryCard(category) {
            onItemClick(category.coinCategory)
        }
    }
}
