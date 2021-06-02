package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.TypeProvider
import jp.co.soramitsu.fearless_utils.runtime.definitions.dynamic.extentsions.HashMapExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeReference
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import org.junit.Assert
import org.junit.Test

class HashMapExtensionTest {

    private val fakeTypeProvider: TypeProvider = {
        TypeReference(FakeType(it))
    }

    @Test
    fun `should parse hashmap`() {
        val type = HashMapExtension.createType("HashMap<Text, Text>", "HashMap<Text, Text>", fakeTypeProvider)
        assertInstance<Vec>(type)
        Assert.assertEquals(type.typeReference.value?.name, "(Text,Text)" )
    }
}
