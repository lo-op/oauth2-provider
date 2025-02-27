package oauth2.code

import oauth2.exception.OAuth2AuthorizationFailedException
import oauth2.request.OAuth2Request

data class AuthorizationCode(val code: String) {
    companion object {
        const val NAME = "code"
        const val LENGTH = 10 // fixed length
    }
}

interface OAuth2AuthorizationCodeGenerator {
    fun generateCode(): String
}

object AuthorizationCodeGenerator: OAuth2AuthorizationCodeGenerator {
    override fun generateCode(): String {
        return this.generateCodeByKotlinRandom()
    }

    private val pool = ('a' .. 'z') + ('A' .. 'Z') + ('0' .. '9')

    // FIXME: 하위 구현체로 분리
    private fun generateCodeByKotlinRandom(): String {
        return (1..AuthorizationCode.LENGTH)
            .map {
                kotlin.random.Random.nextInt(0, pool.size)
            }
            .map(pool::get)
            .joinToString("")
    }
}


interface OAuth2AuthorizationCodeManager {
    fun issueAuthorizationCode(request: OAuth2Request): AuthorizationCode

    fun consumeAuthorizationCode(value: String)
}

// TODO: codeGenerator - use delegation(by)

object InMemoryAuthorizationCodeManager: OAuth2AuthorizationCodeManager {

    // FIXME: DI
    private val codeGenerator: OAuth2AuthorizationCodeGenerator = AuthorizationCodeGenerator

    private val store = mutableMapOf<String, AuthorizationCode>()

    override fun issueAuthorizationCode(request: OAuth2Request): AuthorizationCode {

        // TODO: generate code value
        // duplicate?
        val value = codeGenerator.generateCode()

        val code = AuthorizationCode(value)

        // store
        // TODO: store code with request(client) information
        store[value] = code

        // issue authorization code
        return code
    }

    override fun consumeAuthorizationCode(value: String) {
        // take code from store
        val code = store.remove(value)

        // check if code was issued
        code ?: throw OAuth2AuthorizationFailedException(code = value)

        // TODO: check if code is associated with request(client)
    }
}