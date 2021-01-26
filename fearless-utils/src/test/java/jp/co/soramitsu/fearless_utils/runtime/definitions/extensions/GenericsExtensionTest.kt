package jp.co.soramitsu.fearless_utils.runtime.definitions.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import org.junit.Assert.*
import org.junit.Test

class GenericsExtensionTest {

    @Test
    fun `should extract raw type`() {
        val typeDef = "AccountInfo<T::Index, T::AccountData>"

        val createdType = GenericsExtension.createType(typeDef) {
            FakeType(it)
        }

        assert(createdType != null)
        assertEquals(createdType!!.name, "AccountInfo")
    }

    @Test
    fun `should return null for plain type`() {
        val typeDef = "AccountInfo"

        val createdType = GenericsExtension.createType(typeDef) {
            FakeType(it)
        }

        assert(createdType == null)
    }
}