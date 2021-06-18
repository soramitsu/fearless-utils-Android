package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.schema.definitions.dynamic.extentsions.VectorExtension
import jp.co.soramitsu.schema.definitions.types.TypeReference
import jp.co.soramitsu.schema.definitions.types.composite.Vec
import jp.co.soramitsu.schema.definitions.types.primitives.BooleanType
import jp.co.soramitsu.schema.definitions.types.primitives.DynamicByteArray
import jp.co.soramitsu.schema.definitions.types.primitives.u8
import org.junit.Test

class VectorExtensionTest {

    @Test
    fun `should create optimized type for u8`() {
        val result = VectorExtension.createWrapper("A", TypeReference(u8))

        assertInstance<DynamicByteArray>(result)
    }

    @Test
    fun `should create vec type for other type`() {
        val result = VectorExtension.createWrapper("A", TypeReference(BooleanType))

        assertInstance<Vec>(result)
    }
}
