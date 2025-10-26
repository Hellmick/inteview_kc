package com.finance.token.processors

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Named
import org.apache.camel.Processor
import org.slf4j.LoggerFactory


@ApplicationScoped
@Named("tokenProcessor")
class TokenProcessor : Processor {

    companion object {
        private val logger = LoggerFactory.getLogger(TokenProcessor::class.java)
    }

    fun process(publicKey: String): String {
        val formattedPublicKey = "-----BEGIN PUBLIC KEY-----\n$publicKey\n-----END PUBLIC KEY-----"
        logger.info(formattedPublicKey)

        val jwt = ""
        return jwt
    }

}