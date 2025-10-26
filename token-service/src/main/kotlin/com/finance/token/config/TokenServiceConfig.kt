package com.finance.token.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "token.service")
interface TokenServiceConfig {
    fun authServerUrl(): String
}
