package com.finance.token.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "token.service")
interface TokenServiceConfig {
    fun kcHost(): String
    fun kcRealmUrl(): String
    fun kcTokenServiceUrl(): String
    fun kcClientId(): String
    fun kcClientSecret(): String
    fun redirectUrl(): String
    fun useClientAssertion(): Boolean
    fun keyAlgorithm(): String
    fun privateKeyLocation(): String
    fun transactionServiceUrl(): String
}
