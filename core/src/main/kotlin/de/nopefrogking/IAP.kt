package de.nopefrogking

import com.badlogic.gdx.pay.*
import de.nopefrogking.utils.error
import de.nopefrogking.utils.info
import org.funktionale.either.Disjunction
import org.funktionale.either.Disjunction.*
import java.util.*


private typealias PurchaseListener = (Disjunction<Throwable, IAP.Transaction>)->Unit
private typealias InitListener = ()->Unit
private typealias RestoreListener = (Disjunction<Throwable, List<IAP.Transaction>>)->Unit

object IAP {
    enum class Offer(val id: String, val type: OfferType) {
        GiftSmall("gift_small", OfferType.CONSUMABLE),
        GiftMedium("gift_medium", OfferType.CONSUMABLE),
        GiftBig("gift_big", OfferType.CONSUMABLE),
        GiftHuge("gift_huge", OfferType.CONSUMABLE),
        Premium("premium", OfferType.CONSUMABLE),
        Test("test_consumable", OfferType.CONSUMABLE),

        ;

        companion object {
            private val idToOffer = Offer.values().associateBy { it.id }
            internal fun fromId(id: String) = idToOffer[id]!!
        }
    }

    var isAvailable = false
        private set

    private val purchaseListeners = ArrayList<PurchaseListener>()
    private val initListeners = ArrayList<InitListener>()
    private val restoreListeners = ArrayList<RestoreListener>()

    fun onPurchaseResult(listener: PurchaseListener) = run { purchaseListeners.add(listener); listener }
    fun removePurchaseListener(listener: PurchaseListener) { purchaseListeners.remove(listener) }

    fun onInit(listener: InitListener) = run { initListeners.add(listener); listener }
    fun removeInitListener(listener: InitListener) { initListeners.remove(listener) }

    fun onRestoreResult(listener: RestoreListener) = run { restoreListeners.add(listener); listener }
    fun removeRestoreListener(listener: RestoreListener) { restoreListeners.remove(listener) }

    class PurchasedCancelledException: RuntimeException {
        constructor() : super("Purchase cancelled")
        constructor(cause: Throwable) : super("Purchase cancelled", cause)
    }

    class IAPNotInitializedException: RuntimeException {
        constructor() : super("IAP not initialized")
        constructor(cause: Throwable) : super("IAP not initialized", cause)
    }

    class Transaction(val offer: Offer, private val gdxTransaction: com.badlogic.gdx.pay.Transaction) {
        val storeName: String get() = gdxTransaction.storeName
        val orderId: String get() = gdxTransaction.orderId
        val requestId: String get() = gdxTransaction.requestId
        val userId: String get() = gdxTransaction.userId
        val isPurchased: Boolean get() = gdxTransaction.isPurchased
        val purchaseTime: Date get() = gdxTransaction.purchaseTime
        val purchaseText: String get() = gdxTransaction.purchaseText
        val purchaseCost: Int get() = gdxTransaction.purchaseCost
        val purchaseCostCurrency: String get() = gdxTransaction.purchaseCostCurrency
        val reversalTime: Date get() = gdxTransaction.reversalTime
        val reversalText: String get() = gdxTransaction.reversalText
        val transactionData: String get() = gdxTransaction.transactionData
        val transactionDataSignature: String get() = gdxTransaction.transactionDataSignature

        companion object {

            internal fun fromGdxTransaction(transaction: com.badlogic.gdx.pay.Transaction): Transaction {
                val offer = Offer.fromId(transaction.identifier)
                return IAP.Transaction(offer, transaction)
            }
        }
    }


    fun init() {
        isAvailable = false

        PurchaseSystem.onAppRestarted()
        if (PurchaseSystem.hasManager())
        {
            val config = PurchaseManagerConfig()

            Offer.values().forEach { config.addOffer(Offer().setType(it.type).setIdentifier(it.id)) }

            config.addStoreParam(PurchaseManagerConfig.STORE_NAME_ANDROID_GOOGLE, GOOGLE_PLAY_IAP_KEY)
            config.addStoreParam(PurchaseManagerConfig.STORE_NAME_IOS_APPLE, "")

            PurchaseSystem.install(object : PurchaseObserver {

                override fun handleRestore(transactions: Array<out com.badlogic.gdx.pay.Transaction>) {
                    info { "IAP handleRestore called" }
                    val transactions = transactions.map { Transaction.fromGdxTransaction(it) }
                    restoreListeners.forEach { it(Right(transactions)) }
                }

                override fun handleRestoreError(e: Throwable) {
                    error(e) { "IAP handleRestoreError called" }
                    restoreListeners.forEach { it(Left(e)) }
                }

                override fun handlePurchaseCanceled() {
                    val e = PurchasedCancelledException()
                    error(e) { "IAP handlePurchaseCanceled called" }
                    purchaseListeners.forEach { it(Left(e)) }
                }

                override fun handlePurchaseError(e: Throwable) {
                    error(e) { "IAP handlePurchaseError called" }
                    purchaseListeners.forEach { it(Left(e)) }
                }

                override fun handleInstall() {
                    info { "IAP handleInstall called" }
                    isAvailable = true
                    initListeners.forEach { it() }
                }

                override fun handlePurchase(transaction: com.badlogic.gdx.pay.Transaction) {
                    info { "IAP handlePurchase called" }
                    purchaseListeners.forEach { it(Right(Transaction.fromGdxTransaction(transaction))) }
                }

                override fun handleInstallError(e: Throwable) {
                    error(e) { "IAP handleInstallError called" }
                }

            }, config)
        }
    }

    private fun checkInitialized(): Boolean {
        if (!isAvailable) {
            val e = IAPNotInitializedException()
            error(e) { "IAP not initialized" }
            purchaseListeners.forEach { it(Left(e)) }
        }
        return isAvailable
    }

    fun purchase(offer: Offer) {
        if (!checkInitialized()) return

        com.badlogic.gdx.pay.PurchaseSystem.purchase(offer.id)
    }

    fun getInfo(offer: Offer): Information {
        if (!checkInitialized()) IAPNotInitializedException()

        return com.badlogic.gdx.pay.PurchaseSystem.getInformation(offer.id)
    }

    val storeName get() = com.badlogic.gdx.pay.PurchaseSystem.storeName()

}


