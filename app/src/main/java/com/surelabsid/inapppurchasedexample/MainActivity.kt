package com.surelabsid.inapppurchasedexample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.surelabsid.inapppurchasedexample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), BillingProcessor.IBillingHandler,
    BillingProcessor.IPurchasesResponseListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var billingProcessor: BillingProcessor
    private var isDoAPayment = false
    private var isRestorePayment = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billingProcessor = BillingProcessor(this, LICENSE_KEY, this)
        billingProcessor.initialize()

        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                binding.purchaseApps.text = "Upgrade to Chickin Premium"
            }
        }, 3000)

        binding.purchaseApps.setOnClickListener {
            purchaseProduct()
        }

        binding.restorePurchase.setOnClickListener {
            resetPayment()
        }
    }

    private fun purchaseProduct() {
        isDoAPayment = true
        billingProcessor.purchase(this, PRODUCT_ID)
    }

    private fun updateForPremiumUsers() {
        binding.purchaseApps.text = "You have a premium version"
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        Log.d(TAG, "$productId was successfully purchased")
        if (productId == PRODUCT_ID) {
            updateForPremiumUsers()
        }
    }


    override fun onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored has been called")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Log.d(TAG, "onBillingError has been called")
        Log.e(TAG, "error: ${error.toString()}")
    }

    override fun onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized has been called")

        billingProcessor.loadOwnedPurchasesFromGoogleAsync(this)
        if (isPremiumUser())
            updateForPremiumUsers()
    }

    private fun resetPayment() {
        isRestorePayment = true
        billingProcessor.consumePurchaseAsync(PRODUCT_ID, this)
    }


    private fun isPremiumUser() = billingProcessor.isPurchased(PRODUCT_ID)


    override fun onDestroy() {
        super.onDestroy()
        billingProcessor.release()
    }

    override fun onPurchasesSuccess() {
        if (isDoAPayment) {
            Toast.makeText(this, "Payment successfully", Toast.LENGTH_SHORT).show()
            isDoAPayment = false
        }
        if (isRestorePayment) {
            binding.purchaseApps.text = "Upgrade to Chickin Premium"
            Toast.makeText(this, "Restore successfully", Toast.LENGTH_SHORT).show()
            isRestorePayment = false
        }
    }

    override fun onPurchasesError() {
        Toast.makeText(this, "Payment/Restore Error", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val LICENSE_KEY = "YOUR_LICENSE_KEY_FROM_GOOGLE_PLAY"
        val TAG = MainActivity::class.java.simpleName
        const val PRODUCT_ID = "YOUR_PRODUCT_ID"
    }

}