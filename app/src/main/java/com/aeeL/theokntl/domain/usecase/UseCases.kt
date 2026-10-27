package com.aeeL.theokntl.domain.usecase

import com.aeeL.theokntl.domain.gateway.ConsoleGateway
import com.aeeL.theokntl.domain.model.FeatureReport
import com.aeeL.theokntl.domain.model.FeatureStatus
import com.aeeL.theokntl.domain.model.LoginResult
import com.aeeL.theokntl.domain.model.TopUpOutcome
import com.aeeL.theokntl.domain.model.TopUpReview
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        remember: Boolean,
    ): LoginResult = gateway.login(username, password, remember)
}

class RefreshBalanceUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke() = gateway.refreshBalance()
}

class ReviewTopUpUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    operator fun invoke(rawAmount: String): TopUpReview = gateway.reviewTopUp(rawAmount)
}

class ApplyTopUpUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke(): TopUpOutcome = gateway.applyTopUp()
}

class CancelTopUpUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    operator fun invoke() = gateway.cancelTopUp()
}

class ReadPurchasesUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke(): List<FeatureStatus> = gateway.readPurchases()
}

class SyncFeaturesUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke(): FeatureReport = gateway.syncFeatures()
}

class SignOutUseCase @Inject constructor(
    private val gateway: ConsoleGateway,
) {
    suspend operator fun invoke() = gateway.signOut()
}
