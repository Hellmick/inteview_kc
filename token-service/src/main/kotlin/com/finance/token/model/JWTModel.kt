package com.finance.token.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class JWTModel @JsonCreator constructor (
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("expires_in") val expiresIn: Int,
    @JsonProperty("refresh_expires_in") val refreshExpiresIn: Int,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("id_token") val idToken: String,
    @JsonProperty("not-before-policy") val notBeforePolicy: Boolean,
    @JsonProperty("session_state") val sessionState: String,
    @JsonProperty("scope") val scope: String
)
