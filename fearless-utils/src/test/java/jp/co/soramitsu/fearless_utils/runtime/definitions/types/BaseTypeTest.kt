package jp.co.soramitsu.fearless_utils.runtime.definitions.types

import jp.co.soramitsu.fearless_utils.runtime.definitions.prepopulatedTypeRegistry

abstract class BaseTypeTest {

    protected val typeRegistry = prepopulatedTypeRegistry()
}