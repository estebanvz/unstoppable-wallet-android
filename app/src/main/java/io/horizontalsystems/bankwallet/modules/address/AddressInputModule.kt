package io.horizontalsystems.bankwallet.modules.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery

object AddressInputModule {

    class FactoryToken(private val tokenQuery: TokenQuery, private val coinCode: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val addressViewModel = AddressViewModel(tokenQuery.blockchainType, App.contactsRepository)

            addressViewModel.addAddressHandler(AddressHandlerEns(EnsResolverHolder.resolver))
            addressViewModel.addAddressHandler(AddressHandlerUdn(tokenQuery, coinCode))

            when (tokenQuery.blockchainType) {
                BlockchainType.Bitcoin,
                BlockchainType.BitcoinCash,
                BlockchainType.ECash,
                BlockchainType.Litecoin,
                BlockchainType.Dash,
                BlockchainType.Zcash,
                BlockchainType.BinanceChain -> {
                    addressViewModel.addAddressHandler(AddressHandlerPure())
                }
                BlockchainType.Ethereum,
                BlockchainType.EthereumGoerli,
                BlockchainType.BinanceSmartChain,
                BlockchainType.Polygon,
                BlockchainType.Avalanche,
                BlockchainType.Optimism,
                BlockchainType.Gnosis,
                BlockchainType.Fantom,
                BlockchainType.ArbitrumOne -> {
                    addressViewModel.addAddressHandler(AddressHandlerEvm())
                }
                BlockchainType.Solana -> {
                    addressViewModel.addAddressHandler(AddressHandlerSolana())
                }
                is BlockchainType.Unsupported -> Unit
            }

            return addressViewModel as T
        }
    }

    class FactoryNft(private val blockchainType: BlockchainType) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val addressViewModel = AddressViewModel(blockchainType, App.contactsRepository)

            addressViewModel.addAddressHandler(AddressHandlerEns(EnsResolverHolder.resolver))

            when (blockchainType) {
                BlockchainType.Bitcoin,
                BlockchainType.BitcoinCash,
                BlockchainType.ECash,
                BlockchainType.Litecoin,
                BlockchainType.Dash,
                BlockchainType.Zcash,
                BlockchainType.BinanceChain -> {
                    addressViewModel.addAddressHandler(AddressHandlerPure())
                }
                BlockchainType.Ethereum,
                BlockchainType.EthereumGoerli,
                BlockchainType.BinanceSmartChain,
                BlockchainType.Polygon,
                BlockchainType.Avalanche,
                BlockchainType.Optimism,
                BlockchainType.Gnosis,
                BlockchainType.Fantom,
                BlockchainType.ArbitrumOne -> {
                    addressViewModel.addAddressHandler(AddressHandlerEvm())
                }
                BlockchainType.Solana,
                is BlockchainType.Unsupported -> Unit
            }

            return addressViewModel as T
        }
    }

}
