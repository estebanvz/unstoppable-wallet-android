package io.horizontalsystems.bankwallet.core.managers

import android.content.Context
import io.horizontalsystems.bankwallet.core.customCoinPrefix
import io.horizontalsystems.marketkit.MarketKit
import io.horizontalsystems.marketkit.SyncInfo
import io.horizontalsystems.marketkit.models.*
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class MarketKitWrapper(
    context: Context,
    hsApiBaseUrl: String,
    hsApiKey: String,
    cryptoCompareApiKey: String? = null,
    defiYieldApiKey: String? = null
) {
    private val marketKit: MarketKit = MarketKit.getInstance(
        context = context,
        hsApiBaseUrl = hsApiBaseUrl,
        hsApiKey = hsApiKey,
        cryptoCompareApiKey = cryptoCompareApiKey,
        defiYieldApiKey = defiYieldApiKey
    )

    // Coins

    val fullCoinsUpdatedObservable: Observable<Unit>
        get() = marketKit.fullCoinsUpdatedObservable

    fun fullCoins(filter: String, limit: Int = 20) = marketKit.fullCoins(filter, limit)

    fun fullCoins(coinUids: List<String>) = marketKit.fullCoins(coinUids)

    fun allCoins() = marketKit.allCoins()

    fun token(query: TokenQuery) = marketKit.token(query)

    fun tokens(queries: List<TokenQuery>) = marketKit.tokens(queries)

    fun tokens(reference: String) = marketKit.tokens(reference)

    fun tokens(blockchainType: BlockchainType, filter: String, limit: Int = 20) = marketKit.tokens(blockchainType, filter, limit)

    fun blockchains(uids: List<String>) = marketKit.blockchains(uids)

    fun blockchain(uid: String) = marketKit.blockchain(uid)

    fun marketInfosSingle(top: Int, currencyCode: String, defi: Boolean = false) = marketKit.marketInfosSingle(top, currencyCode, defi)

    fun advancedMarketInfosSingle(top: Int = 250, currencyCode: String) = marketKit.advancedMarketInfosSingle(top, currencyCode)

    fun marketInfosSingle(coinUids: List<String>, currencyCode: String): Single<List<MarketInfo>> =
        marketKit.marketInfosSingle(coinUids.removeCustomCoins(), currencyCode)

    fun marketInfosSingle(categoryUid: String, currencyCode: String) = marketKit.marketInfosSingle(categoryUid, currencyCode)

    fun marketInfoOverviewSingle(coinUid: String, currencyCode: String, language: String) =
        marketKit.marketInfoOverviewSingle(coinUid, currencyCode, language)

    fun marketInfoDetailsSingle(coinUid: String, currencyCode: String) = marketKit.marketInfoDetailsSingle(coinUid, currencyCode)

    fun analyticsSingle(coinUid: String, currencyCode: String) = marketKit.analyticsSingle(coinUid, currencyCode)

    fun analyticsPreviewSingle(coinUid: String) = marketKit.analyticsPreviewSingle(coinUid)

    fun marketInfoTvlSingle(coinUid: String, currencyCode: String, timePeriod: HsTimePeriod) =
        marketKit.marketInfoTvlSingle(coinUid, currencyCode, timePeriod)

    fun marketInfoGlobalTvlSingle(chain: String, currencyCode: String, timePeriod: HsTimePeriod) =
        marketKit.marketInfoGlobalTvlSingle(chain, currencyCode, timePeriod)

    fun defiMarketInfosSingle(currencyCode: String) = marketKit.defiMarketInfosSingle(currencyCode)

    // Categories

    fun coinCategoriesSingle(currencyCode: String) = marketKit.coinCategoriesSingle(currencyCode)

    fun coinCategoryMarketPointsSingle(categoryUid: String, interval: HsTimePeriod, currencyCode: String) =
        marketKit.coinCategoryMarketPointsSingle(categoryUid, interval, currencyCode)

    fun sync() = marketKit.sync()

    // Coin Prices

    private val String.isCustomCoin: Boolean
        get() = startsWith(TokenQuery.customCoinPrefix)

    private fun List<String>.removeCustomCoins(): List<String> = filterNot { it.isCustomCoin }

    fun refreshCoinPrices(currencyCode: String) = marketKit.refreshCoinPrices(currencyCode)

    fun coinPrice(coinUid: String, currencyCode: String): CoinPrice? =
        if (coinUid.isCustomCoin) null else marketKit.coinPrice(coinUid, currencyCode)

    fun coinPriceMap(coinUids: List<String>, currencyCode: String): Map<String, CoinPrice> {
        val coinUidsNoCustom = coinUids.removeCustomCoins()
        return when {
            coinUidsNoCustom.isEmpty() -> mapOf()
            else -> marketKit.coinPriceMap(coinUidsNoCustom, currencyCode)
        }
    }

    fun coinPriceObservable(coinUid: String, currencyCode: String): Observable<CoinPrice> =
        if (coinUid.isCustomCoin) Observable.never() else marketKit.coinPriceObservable(coinUid, currencyCode)

    fun coinPriceMapObservable(coinUids: List<String>, currencyCode: String): Observable<Map<String, CoinPrice>> {
        val coinUidsNoCustom = coinUids.removeCustomCoins()
        return when {
            coinUidsNoCustom.isEmpty() -> Observable.never()
            else -> marketKit.coinPriceMapObservable(coinUidsNoCustom, currencyCode)
        }
    }

    // Coin Historical Price

    fun coinHistoricalPriceSingle(coinUid: String, currencyCode: String, timestamp: Long): Single<BigDecimal> =
        if (coinUid.isCustomCoin) Single.never() else marketKit.coinHistoricalPriceSingle(coinUid, currencyCode, timestamp)

    fun coinHistoricalPrice(coinUid: String, currencyCode: String, timestamp: Long) =
        if (coinUid.isCustomCoin) null else marketKit.coinHistoricalPrice(coinUid, currencyCode, timestamp)

    // Posts

    fun postsSingle() = marketKit.postsSingle()

    // Market Tickers

    fun marketTickersSingle(coinUid: String) = marketKit.marketTickersSingle(coinUid)

    // Details

    fun tokenHoldersSingle(coinUid: String, blockchainUid: String) = marketKit.tokenHoldersSingle(coinUid, blockchainUid)

    fun treasuriesSingle(coinUid: String, currencyCode: String) = marketKit.treasuriesSingle(coinUid, currencyCode)

    fun investmentsSingle(coinUid: String) = marketKit.investmentsSingle(coinUid)

    fun coinReportsSingle(coinUid: String) = marketKit.coinReportsSingle(coinUid)

    fun auditReportsSingle(addresses: List<String>) = marketKit.auditReportsSingle(addresses)

    // Pro Details

    fun cexVolumesSingle(coinUid: String, currencyCode: String, timePeriod: HsTimePeriod) =
        marketKit.cexVolumesSingle(coinUid, currencyCode, timePeriod)

    fun dexLiquiditySingle(coinUid: String, currencyCode: String, timePeriod: HsTimePeriod, sessionKey: String?) =
        marketKit.dexLiquiditySingle(coinUid, currencyCode, timePeriod, sessionKey)

    fun dexVolumesSingle(coinUid: String, currencyCode: String, timePeriod: HsTimePeriod, sessionKey: String?) =
        marketKit.dexVolumesSingle(coinUid, currencyCode, timePeriod, sessionKey)

    fun transactionDataSingle(coinUid: String, timePeriod: HsTimePeriod, platform: String?, sessionKey: String?) =
        marketKit.transactionDataSingle(coinUid, timePeriod, platform, sessionKey)

    fun activeAddressesSingle(coinUid: String, timePeriod: HsTimePeriod, sessionKey: String?) =
        marketKit.activeAddressesSingle(coinUid, timePeriod, sessionKey)

    fun cexVolumeRanksSingle(currencyCode: String) = marketKit.cexVolumeRanksSingle(currencyCode)
    fun dexVolumeRanksSingle(currencyCode: String) = marketKit.dexVolumeRanksSingle(currencyCode)
    fun dexLiquidityRanksSingle(currencyCode: String) = marketKit.dexLiquidityRanksSingle(currencyCode)
    fun activeAddressRanksSingle(currencyCode: String) = marketKit.activeAddressRanksSingle(currencyCode)
    fun transactionCountsRanksSingle(currencyCode: String) = marketKit.transactionCountsRanksSingle(currencyCode)
    fun revenueRanksSingle(currencyCode: String) = marketKit.revenueRanksSingle(currencyCode)

    // Overview

    fun marketOverviewSingle(currencyCode: String) = marketKit.marketOverviewSingle(currencyCode)

    fun topMoversSingle(currencyCode: String) = marketKit.topMoversSingle(currencyCode)

    // Chart Info

    fun chartStartTimeSingle(coinUid: String) = marketKit.chartStartTimeSingle(coinUid)

    fun chartPointsSingle(coinUid: String, currencyCode: String, periodType: HsPeriodType) = marketKit.chartPointsSingle(coinUid, currencyCode, periodType)

    // Global Market Info

    fun globalMarketPointsSingle(currencyCode: String, timePeriod: HsTimePeriod) =
        marketKit.globalMarketPointsSingle(currencyCode, timePeriod)

    fun topPlatformsSingle(currencyCode: String) =
        marketKit.topPlatformsSingle(currencyCode)

    fun topPlatformMarketCapPointsSingle(chain: String, timePeriod: HsTimePeriod, currencyCode: String) =
        marketKit.topPlatformMarketCapPointsSingle(chain, timePeriod, currencyCode)

    fun topPlatformCoinListSingle(chain: String, currencyCode: String) =
        marketKit.topPlatformCoinListSingle(chain, currencyCode)

    // NFT

    suspend fun nftCollections(): List<NftTopCollection> =
        marketKit.nftTopCollections()

    // Misc

    fun syncInfo(): SyncInfo {
        return marketKit.syncInfo()
    }

}
