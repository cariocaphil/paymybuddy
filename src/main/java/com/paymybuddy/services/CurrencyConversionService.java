package com.paymybuddy.services;

import com.paymybuddy.models.Currency;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyConversionService {

  private Map<Currency, Map<Currency, Double>> exchangeRates = new HashMap<>();

  public CurrencyConversionService() {
    // Initialize with some sample exchange rates
    // In reality, these would be dynamically fetched from a reliable financial data source

    // Rates from USD to other currencies
    Map<Currency, Double> usdRates = new HashMap<>();
    usdRates.put(Currency.EUR, 0.85);
    // Add more rates as needed

    // Rates from EUR to other currencies
    Map<Currency, Double> eurRates = new HashMap<>();
    eurRates.put(Currency.USD, 1.17);
    // Add more rates as needed

    exchangeRates.put(Currency.USD, usdRates);
    exchangeRates.put(Currency.EUR, eurRates);
  }

  public double convertCurrency(double amount, Currency sourceCurrency, Currency targetCurrency) {
    Logger.info("Converting currency from {} to {}", sourceCurrency, targetCurrency);

    if (sourceCurrency == targetCurrency) {
      return amount;
    }

    Map<Currency, Double> rates = exchangeRates.get(sourceCurrency);
    if (rates == null || !rates.containsKey(targetCurrency)) {
      Logger.error("Conversion rate from {} to {} not found", sourceCurrency, targetCurrency);
      throw new IllegalArgumentException("Unsupported currency conversion requested.");
    }

    double convertedAmount = amount * rates.get(targetCurrency);
    Logger.info("Converted {} from {} to {}: {}", amount, sourceCurrency, targetCurrency, convertedAmount);
    return convertedAmount;
  }

}
