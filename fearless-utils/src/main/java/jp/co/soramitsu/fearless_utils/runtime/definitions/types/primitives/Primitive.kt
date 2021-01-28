package jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry

abstract class Primitive<I>(name: String) : Type<I>(name) {

    override val isFullyResolved = true
}