package io.horizontalsystems.bankwallet.modules.walletconnect.list.v1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.walletconnect.sign.client.Sign
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.managers.EvmBlockchainManager
import io.horizontalsystems.bankwallet.core.providers.Translator
import io.horizontalsystems.bankwallet.core.subscribeIO
import io.horizontalsystems.bankwallet.modules.walletconnect.list.WalletConnectListModule
import io.horizontalsystems.bankwallet.modules.walletconnect.list.WalletConnectListModule.Section
import io.horizontalsystems.bankwallet.modules.walletconnect.list.v2.WC2ListService
import io.horizontalsystems.bankwallet.modules.walletconnect.version1.WC1SessionKillManager
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2Parser
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2Service
import io.horizontalsystems.core.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import java.net.UnknownHostException

class WalletConnectListViewModel(
    private val service: WalletConnectListService,
    private val wc2ListService: WC2ListService,
    private val evmBlockchainManager: EvmBlockchainManager,
    private val wc2Service: WC2Service
) : ViewModel() {
    sealed class Route {
        data class WC1Session(val uri: String) : Route()
        object Error: Route()
    }

    var route by mutableStateOf<Route?>(null)
        private set

    var initialConnectionPrompted = false

    private var v1SectionItem: Section? = null
    private var v2SectionItem: Section? = null
    private var pairingsNumber = wc2Service.getPairings().size
    private val emptyScreen: Boolean
        get() = v1SectionItem == null && v2SectionItem == null && pairingsNumber == 0

    var uiState by mutableStateOf(
        WalletConnectListUiState(
            v1SectionItem = v1SectionItem,
            v2SectionItem = v2SectionItem,
            pairingsNumber = pairingsNumber,
            emptyScreen = emptyScreen,
        )
    )
        private set

    val killingSessionInProcessLiveEvent = SingleLiveEvent<Unit>()
    val killingSessionCompletedLiveEvent = SingleLiveEvent<Unit>()
    val killingSessionFailedLiveEvent = SingleLiveEvent<String>()

    private val disposables = CompositeDisposable()

    init {
        service.itemsObservable
            .subscribeIO {
                sync(it)
                emitState()
            }
            .let { disposables.add(it) }

        service.sessionKillingStateObservable
            .subscribeIO { sync(it) }
            .let { disposables.add(it) }

        wc2ListService.sessionsObservable
            .subscribeIO {
                syncV2(it)
                emitState()
            }
            .let { disposables.add(it) }

        wc2ListService.pendingRequestsObservable
            .subscribeIO {
                syncPendingRequestsCount(it)
                emitState()
            }
            .let { disposables.add(it) }

        sync(service.items)
        syncV2(wc2ListService.sessions)
        emitState()
    }

    fun setConnectionUri(uri: String) {
        route = when (WalletConnectListModule.getVersionFromUri(uri)) {
            1 -> Route.WC1Session(uri)
            2 -> {
                wc2Service.pair(uri)
                null
            }
            else -> Route.Error
        }
    }

    private fun emitState() {
        uiState = WalletConnectListUiState(
            v1SectionItem = v1SectionItem,
            v2SectionItem = v2SectionItem,
            pairingsNumber = pairingsNumber,
            emptyScreen = emptyScreen
        )
    }

    fun onDelete(sessionsId: String) {
        service.kill(sessionsId)
    }

    private fun sync(state: WC1SessionKillManager.State) {
        when (state) {
            is WC1SessionKillManager.State.Failed -> {
                val errorMessage = if (state.error is UnknownHostException)
                    Translator.getString(R.string.Hud_Text_NoInternet)
                else
                    state.error.message ?: state.error::class.java.simpleName

                killingSessionFailedLiveEvent.postValue(errorMessage)
            }
            WC1SessionKillManager.State.Killed -> {
                killingSessionCompletedLiveEvent.postValue(Unit)
            }
            WC1SessionKillManager.State.NotConnected,
            WC1SessionKillManager.State.Processing -> {
                killingSessionInProcessLiveEvent.postValue(Unit)
            }
        }
    }

    private fun sync(items: List<WalletConnectListService.Item>) {
        if (items.isEmpty()) {
            v1SectionItem = null
            return
        }
        val sessions = mutableListOf<WalletConnectListModule.SessionViewItem>()
        items.forEach { item ->
            val itemSessions = item.sessions.map { session ->
                WalletConnectListModule.SessionViewItem(
                    sessionId = session.remotePeerId,
                    title = session.remotePeerMeta.name,
                    subtitle = item.chain,
                    url = session.remotePeerMeta.url,
                    imageUrl = getSuitableIcon(session.remotePeerMeta.icons),
                )
            }
            sessions.addAll(itemSessions)
        }
        v1SectionItem = Section(WalletConnectListModule.Version.Version1, sessions)
    }

    private fun getSuitableIcon(imageUrls: List<String>): String? {
        return imageUrls.lastOrNull { it.endsWith("png", ignoreCase = true) }
            ?: imageUrls.lastOrNull()
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refreshPairingsNumber() {
        pairingsNumber = wc2Service.getPairings().size
        emitState()
    }

    private fun syncPendingRequestsCount(count: Int) {
        v2SectionItem?.let {
            setSectionViewItem(it.sessions, count)
        }
    }

    private fun setSectionViewItem(
        sessions: List<WalletConnectListModule.SessionViewItem>,
        pendingRequestsCount: Int
    ) {
        val count = if (pendingRequestsCount > 0) pendingRequestsCount else null
        v2SectionItem = Section(WalletConnectListModule.Version.Version2, sessions, count)
    }

    private fun syncV2(sessions: List<Sign.Model.Session>) {
        if (sessions.isEmpty() && wc2ListService.pendingRequestsCount == 0) {
            v2SectionItem = null
            return
        }

        val sessionItems = sessions.map { session ->
            WalletConnectListModule.SessionViewItem(
                sessionId = session.topic,
                title = session.metaData?.name ?: "",
                subtitle = getSubtitle(session.namespaces.values.map { it.accounts }.flatten()),
                url = session.metaData?.url ?: "",
                imageUrl = session.metaData?.icons?.lastOrNull(),
            )
        }

        setSectionViewItem(sessionItems, wc2ListService.pendingRequestsCount)
    }

    private fun getSubtitle(chains: List<String>): String {
        val chainNames = chains.mapNotNull { chain ->
            WC2Parser.getChainId(chain)?.let { chainId ->
                evmBlockchainManager.getBlockchain(chainId)?.name
            }
        }
        return chainNames.joinToString(", ")
    }

    fun onDeleteV2(sessionId: String) {
        wc2ListService.delete(sessionId)
    }

    fun onHandleRoute() {
        route = null
    }
}

data class WalletConnectListUiState(
    val v1SectionItem: Section?,
    val v2SectionItem: Section?,
    val pairingsNumber: Int,
    val emptyScreen: Boolean
)
