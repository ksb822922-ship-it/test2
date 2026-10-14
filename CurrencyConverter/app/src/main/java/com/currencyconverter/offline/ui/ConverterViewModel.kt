package com.currencyconverter.offline.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.currencyconverter.offline.data.AppDatabase
import com.currencyconverter.offline.data.RateResult
import com.currencyconverter.offline.data.RatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val CURRENCIES = listOf(
    "USD", "EUR", "GBP", "RUB", "UAH", "KZT", "BYN", "PLN", "CZK", "CHF",
    "JPY", "CNY", "KRW", "INR", "TRY", "AED", "CAD", "AUD", "NZD", "SEK",
    "NOK", "BRL", "MXN", "ZAR"
)

data class ConverterState(
    val amountText: String = "",
    val from: String = "USD",
    val to: String = "EUR",
    val result: String? = null,
    val isOffline: Boolean = false,
    val lastUpdated: Long? = null,
    val error: String? = null,
    val loading: Boolean = false
)

class ConverterViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = RatesRepository(AppDatabase.get(app).ratesDao())

    private val _state = MutableStateFlow(ConverterState())
    val state: StateFlow<ConverterState> = _state.asStateFlow()

    fun onAmountChange(text: String) {
        if (text.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.update { it.copy(amountText = text) }
        }
    }

    fun onFromChange(code: String) = _state.update { it.copy(from = code) }
    fun onToChange(code: String) = _state.update { it.copy(to = code) }

    fun convert() {
        val current = _state.value
        val amount = current.amountText.replace(',', '.').toDoubleOrNull()
        if (amount == null || amount < 0) {
            _state.update {
                it.copy(error = "Введите корректную сумму", result = null, isOffline = false)
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = repo.getRate(current.from, current.to)) {
                is RateResult.Success -> {
                    val value = amount * r.rate
                    _state.update {
                        it.copy(
                            loading = false,
                            result = "%.2f %s".format(value, current.to),
                            isOffline = r.offline,
                            lastUpdated = r.updatedAt,
                            error = null
                        )
                    }
                }
                RateResult.NoData -> _state.update {
                    it.copy(
                        loading = false,
                        result = null,
                        isOffline = false,
                        error = "Подключитесь к интернету"
                    )
                }
            }
        }
    }
}
