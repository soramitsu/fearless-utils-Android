package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.definitions.substrateBaseTypes

abstract class BaseTypeTest {

    protected val typeRegistry = substrateBaseTypes()
}