package com.finance.token.routes

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.rest.RestBindingMode
import com.finance.token.config.TokenServiceConfig
import com.finance.token.model.AuthServerResponse
import com.finance.token.model.JWTModel
import com.finance.token.processors.TokenProcessor
import org.apache.camel.Exchange
import org.apache.camel.http.base.HttpOperationFailedException
import org.apache.camel.model.dataformat.JsonLibrary


@ApplicationScoped
class TokenService @Inject constructor(
    private val config: TokenServiceConfig,
    private val tokenProcessor : TokenProcessor
) : RouteBuilder() {
    override fun configure() {
        // REST config
        restConfiguration()
            .component("platform-http")
            .contextPath("/")
            .bindingMode(RestBindingMode.json)

        // endpoint declaration
        rest("/token")
            .get()
            .param().name("code").type(org.apache.camel.model.rest.RestParamType.query).endParam()
            .to("direct:handleAuthCode")

        from("direct:handleAuthCode")
            .process(tokenProcessor)
            .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
            .setHeader("CamelHttpMethod", constant("POST"))
            .toD("${config.kcTokenServiceUrl()}?bridgeEndpoint=true")
            .unmarshal().json(JsonLibrary.Jackson, JWTModel::class.java)
            .log("Generated JWT token")
            .to("direct:getTransactions")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setBody(simple("{\"status\": \"success\", \"message\": \"JWT Successfully generated\"}"))

        from("direct:getTransactions")
            .doTry()
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("Bearer \${body.accessToken}"))
                .setBody(constant(null))
                .toD("${config.transactionServiceUrl()}?bridgeEndpoint=true")
                .log("Transactions fetched")
                .to("kafka:user-transactions?brokers=localhost:9092")
                .log("Transactions published to Kafka topic")
            .doCatch(HttpOperationFailedException::class.java)
                .log("Transaction service call failed: \${exception.message}")
            .doCatch(Exception::class.java)
                .log("Unexpected error: \${exception.message}")
            .end()

    }
}