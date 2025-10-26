package com.finance.token.processors

import com.finance.token.config.TokenServiceConfig
import com.finance.token.model.ErrorResponse
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.inject.Named
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.ProducerTemplate
import org.slf4j.LoggerFactory
import java.time.Instant
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date
import java.util.UUID


@ApplicationScoped
@Named("tokenProcessor")
class TokenProcessor @Inject constructor(
    private val config: TokenServiceConfig,
    private val producerTemplate: ProducerTemplate
) : Processor {

    companion object {
        private val logger = LoggerFactory.getLogger(TokenProcessor::class.java)
    }

    override fun process(exchange: Exchange) {
        val code = exchange.message.getHeader("code", String::class.java)

        if (code.isNullOrBlank()) {
            val errorMsg = "Missing or empty 'code' parameter in request"
            logger.error(errorMsg)
            setErrorResponse(exchange, 400, errorMsg)
            return
        }

        try {
            logger.info("Preparing token request parameters for authorization code")
            val formParams = prepareFormData(code)

            // Set the parameters in the body for the route to send
            exchange.message.body = formParams
            exchange.message.headers["CamelHttpMethod"] = "POST"
            exchange.message.headers["Content-Type"] = "application/x-www-form-urlencoded"

        } catch (ex: Exception) {
            logger.error("Error while preparing token request", ex)
            setErrorResponse(exchange, 500, "Internal server error: ${ex.message}")
        }
    }


    fun mapToFormData(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, StandardCharsets.UTF_8)}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
        }
    }

    fun prepareFormData(code: String): String {
        val formParams = mutableMapOf(
            "grant_type" to "authorization_code",
            "code" to code,
            "redirect_uri" to config.redirectUrl(),
            "client_id" to config.kcClientId()
        )
        if (!config.useClientAssertion() && config.kcClientSecret() != null) {
            formParams["client_secret"] = config.kcClientSecret()
        }
        else {
            formParams.remove("client_secret")
            formParams["client_assertion_type"] =
                "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
            formParams["client_assertion"] = generateSignedJWT()
        }

        return mapToFormData(formParams)
    }

    fun generateSignedJWT() : String {
        val now = Instant.now()

        val claims = JWTClaimsSet.Builder()
            .issuer(config.kcClientId())
            .subject(config.kcClientId())
            .audience(config.kcTokenServiceUrl())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(300)))
            .jwtID(UUID.randomUUID().toString())
            .build()

        val privateKey = loadPrivateKey(config.privateKeyLocation())

        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .build()

        val signedJWT = SignedJWT(header, claims)
        signedJWT.sign(RSASSASigner(privateKey))

        return signedJWT.serialize()
    }

    fun loadPrivateKey(pemFile: String): PrivateKey {
        val pem = File(pemFile).readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(pem)
        val keySpec = PKCS8EncodedKeySpec(decoded)

        val keyFactory = KeyFactory.getInstance(config.keyAlgorithm())
        return keyFactory.generatePrivate(keySpec)
    }

    private fun setErrorResponse(exchange: Exchange, statusCode: Int, message: String) {
        exchange.message.body = ErrorResponse(message)
        exchange.message.headers["CamelHttpResponseCode"] = statusCode
        exchange.message.headers["Content-Type"] = "application/json"
    }

}