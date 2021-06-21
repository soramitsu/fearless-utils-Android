package jp.co.soramitsu.fearless_utils.runtime

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.metadata.RuntimeMetadata

class RuntimeSnapshot {
    lateinit var typeRegistry: TypeRegistry
    lateinit var metadata: RuntimeMetadata
}
