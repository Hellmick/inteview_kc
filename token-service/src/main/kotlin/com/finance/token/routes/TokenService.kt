package com.finance.token.routes

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.rest.RestBindingMode
import com.finance.token.config.TokenServiceConfig
import com.finance.token.model.AuthServerResponse
import org.apache.camel.model.dataformat.JsonLibrary


@ApplicationScoped
class TokenService @Inject constructor(
    private val config: TokenServiceConfig,
    private val TokenProcessor: tokenProcessor
) : RouteBuilder() {
    override fun configure() {
        // REST config
        restConfiguration()
            .component("netty-http")
            .port(8081)
            .bindingMode(RestBindingMode.off)

        // endpoint declaration
        rest("/token")
            .get()
            .param().name("code").type(org.apache.camel.model.rest.RestParamType.query).endParam()
            .to("direct:handleToken")

        // handling route
        from("direct:handleToken")
            .log("Received auth code: \${header.code}")
            .toD("${config.authServerUrl()}?bridgeEndpoint=true&throwExceptionOnFailure=false")
            .convertBodyTo(String::class.java)
            .log("Auth server response: \${body}")
            .unmarshal().json(JsonLibrary.Jackson, AuthServerResponse::class.java)
            .log("Parsed TokenResponse object: \${body}")
            .process("bean:tokenProcessor?method=exchangeCode(\${body.publicKey})")

        //    .log("JWT Token: \${body}")
        //    .to("direct:fetchTransactions")
    }
}