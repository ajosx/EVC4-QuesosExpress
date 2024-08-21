package idat.edu.quesosexpress.ui.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("quesos_express_prefs", Context.MODE_PRIVATE)

    fun saveProductQuantity(productId: String, quantity: Int) {
        prefs.edit().putInt(productId, quantity).apply()
    }

    fun getProductQuantity(productId: String): Int {
        return prefs.getInt(productId, 0)
    }

    fun clearProductQuantities() {
        prefs.edit().clear().apply()
    }

    fun getAllProductIds(): Set<String> {
        return prefs.all.keys
    }
}