package aeeL.theoSol.domain.gateway

import aeeL.theoSol.domain.model.ConsoleState
import aeeL.theoSol.domain.model.FeatureReport
import aeeL.theoSol.domain.model.FeatureStatus
import aeeL.theoSol.domain.model.LoginResult
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.model.TopUpReview
import kotlinx.coroutines.flow.StateFlow

interface ConsoleGateway {
    val state: StateFlow<ConsoleState>

    suspend fun login(username: String, password: String, remember: Boolean): LoginResult

    suspend fun refreshBalance()

    fun reviewTopUp(rawAmount: String): TopUpReview

    fun cancelTopUp()

    suspend fun applyTopUp(): TopUpOutcome

    suspend fun readPurchases(): List<FeatureStatus>

    suspend fun syncFeatures(): FeatureReport

    suspend fun signOut()
}
