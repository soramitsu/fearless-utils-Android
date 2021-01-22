package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry

abstract class Primitive<I>(name: String) : Type<I>(name) {

    // no stubs possible for numbers
    override fun replaceStubs(registry: TypeRegistry): Type<I> = this
}