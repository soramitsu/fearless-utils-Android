package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.substrateRegistryPreset

abstract class BaseTypeTest {

    protected val typeRegistry = substrateRegistryPreset()
}