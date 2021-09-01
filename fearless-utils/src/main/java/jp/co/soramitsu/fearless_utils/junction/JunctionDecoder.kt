package jp.co.soramitsu.fearless_utils.junction

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

abstract class JunctionDecoder {

    sealed class DecodingError : Exception() {
        object InvalidStart : DecodingError()
        object EmptyPath : DecodingError()
        object EmptyPassphrase : DecodingError()
        object EmptyJunction : DecodingError()
        object MultiplePassphrase : DecodingError()
    }

    class DecodeResult(
        val password: String?,
        val junctions: List<Junction>
    )

    companion object {
        const val SOFT_SEPARATOR = "/"
        const val HARD_SEPARATOR = "//"
        const val PASSWORD_SEPARATOR = "///"
    }

    fun decode(path: String): DecodeResult {
        require(path.startsWith(SOFT_SEPARATOR)) {
            DecodingError.InvalidStart
        }

        val passwordComponents = path.split(PASSWORD_SEPARATOR)

        val junctionsPath = passwordComponents.firstOrNull() ?: throw DecodingError.EmptyPath

        require(passwordComponents.size <= 2) {
            DecodingError.MultiplePassphrase
        }

        val password = if (passwordComponents.size == 2) {
            passwordComponents.last()
        } else {
            null
        }

        password?.let {
            require(password.isNotEmpty()) {
                DecodingError.EmptyPassphrase
            }
        }

        return DecodeResult(password = password, junctions = parseJunctionsFromPath(junctionsPath))
    }

    private fun parseJunctionsFromPath(junctionsPath: String): List<Junction> {
        return junctionsPath.split(HARD_SEPARATOR)
            .map { component ->
                val junctions = mutableListOf<Junction>()

                val subComponents = component.split(SOFT_SEPARATOR)

                val hardJunction = subComponents.firstOrNull() ?: throw DecodingError.EmptyJunction

                if (hardJunction.isNotEmpty()) {
                    junctions.add(decodeJunction(hardJunction, JunctionType.HARD))
                }

                val softJunctions = subComponents.drop(1).map {
                    decodeJunction(it, JunctionType.SOFT)
                }

                junctions.addAll(softJunctions)

                junctions
            }.flatten()
    }

    protected abstract fun decodeJunction(rawJunction: String, type: JunctionType): Junction
}

@OptIn(ExperimentalContracts::class)
private fun require(condition: Boolean, lazyException: () -> Exception) {
    contract {
        returns() implies condition
    }

    if (!condition) throw lazyException()
}