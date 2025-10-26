package com.finance.token.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class AuthServerResponse @JsonCreator constructor (
    @JsonProperty("realm") val realm: String,
    @JsonProperty("public_key") val publicKey: String,
    @JsonProperty("token-service") val tokenService: String,
    @JsonProperty("account-service") val accountService: String,
    @JsonProperty("tokens-not-before") val tokensNotBefore: Int
)
