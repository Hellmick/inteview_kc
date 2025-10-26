import com.finance.token.processors.TokenProcessor
import com.finance.token.routes.TokenService
import org.apache.camel.Exchange
import org.apache.camel.RoutesBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.junit5.CamelTestSupport
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

// Mock data classes
data class JWTModel(val accessToken: String)
data class Transaction(val id: Int, val amount: Double)

// Example interface; in your project, replace with the actual TokenServiceConfig
interface TokenServiceConfig {
    fun kcTokenServiceUrl(): String
    fun transactionServiceUrl(): String
    fun kcHost(): String
    fun kcRealmUrl(): String
    fun kcClientId(): String
    fun kcClientSecret(): String
    fun redirectUrl(): String
    fun useClientAssertion(): Boolean
}

class TokenServiceTest : CamelTestSupport() {

    private val mockTokenProcessor = mock<TokenProcessor>()

    private val mockConfig: TokenServiceConfig = mock<TokenServiceConfig>().apply {
        whenever(kcTokenServiceUrl()).thenReturn("mock:kcTokenService")
        whenever(transactionServiceUrl()).thenReturn("mock:transactionService")
        whenever(kcHost()).thenReturn("mock-host")
        whenever(kcRealmUrl()).thenReturn("mock-realm")
        whenever(kcClientId()).thenReturn("mock-client-id")
        whenever(kcClientSecret()).thenReturn("mock-secret")
        whenever(redirectUrl()).thenReturn("mock-redirect")
        whenever(useClientAssertion()).thenReturn(false)
    }

    override fun createRouteBuilder(): RoutesBuilder {
        // Pass mocks into TokenService
        return TokenService(mockConfig as com.finance.token.config.TokenServiceConfig, mockTokenProcessor)
    }

    @Test
    fun `test full token and transaction flow`() {
        // Mock TokenProcessor to inject a JWT
        doAnswer { invocation ->
            val exchange = invocation.arguments[0] as Exchange
            exchange.`in`.body = JWTModel("fake-access-token")
            null
        }.whenever(mockTokenProcessor).process(any())

        context.start()

        // Setup mock endpoints
        val kcMock = getMockEndpoint("mock:kcTokenService")
        kcMock.expectedMessageCount(1)
        kcMock.whenAnyExchangeReceived { it.message.body = JWTModel("fake-access-token") }

        val transactionMock = getMockEndpoint("mock:transactionService")
        transactionMock.expectedMessageCount(1)
        transactionMock.whenAnyExchangeReceived {
            it.message.body = listOf(Transaction(1, 100.0), Transaction(2, 250.0))
        }

        val kafkaMock = getMockEndpoint("mock:kafka:user-transactions")
        kafkaMock.expectedMessageCount(1)

        // Trigger the route
        template.sendBody("direct:handleAuthCode", null)

        // Assertions
        kcMock.assertIsSatisfied()
        transactionMock.assertIsSatisfied()
        kafkaMock.assertIsSatisfied()

        val publishedBody = kafkaMock.exchanges[0].message.body.toString()
        assert(publishedBody.contains("100.0"))
        assert(publishedBody.contains("250.0"))
    }
}