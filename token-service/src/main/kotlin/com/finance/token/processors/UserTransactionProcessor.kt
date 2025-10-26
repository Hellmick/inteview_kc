package com.finance.token.processors

import org.apache.camel.Exchange
import org.apache.camel.Processor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class UserTransactionsProcessor : Processor {
    private val mapper = jacksonObjectMapper()

    override fun process(exchange: Exchange) {
        val transactionsJson = exchange.getIn().body.toString()

        val transactionsMap: Map<String, Any> = mapper.readValue(transactionsJson)
        val userId = transactionsMap["userId"]?.toString() ?: "unknown"
        val userInfo = mapOf(
            "user" to userId,
            "transactions" to transactionsJson
        )
        exchange.getIn().body = userInfo
    }
}