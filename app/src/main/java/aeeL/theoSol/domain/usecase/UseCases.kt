package aeeL.theoSol.domain.usecase

import aeeL.theoSol.domain.gateway.ConsoleGateway
import aeeL.theoSol.domain.model.FeatureReport
import aeeL.theoSol.domain.model.FeatureStatus
import aeeL.theoSol.domain.model.LoginResult
import aeeL.theoSol.domain.model.TopUpOutcome
import aeeL.theoSol.domain.model.TopUpReview
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
