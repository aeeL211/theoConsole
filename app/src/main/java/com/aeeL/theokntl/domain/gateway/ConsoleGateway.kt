package com.aeeL.theokntl.domain.gateway

import com.aeeL.theokntl.domain.model.ConsoleState
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.LoginResult
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
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
