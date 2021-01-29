package jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions

import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GenericsExtensionTest {

    @Mock
    private lateinit var typeRegistry: TypeRegistry

    @Before
    fun startUp(){
        `when`(typeRegistry.get(anyString(), resolveAliasing = anyBoolean(), storageOnly =  anyBoolean()))
            .thenAnswer { FakeType(it.arguments[0] as String) }
    }

    @Test
    fun `should extract raw type`() {
        val typeDef = "AccountInfo<T::Index, T::AccountData>"

        val createdType = GenericsExtension.createType("Test", typeDef, typeRegistry)

        assert(createdType != null)
        assertEquals(createdType!!.name, "AccountInfo")
    }

    @Test
    fun `should return null for plain type`() {
        val typeDef = "AccountInfo"

        val createdType = GenericsExtension.createType("Test", typeDef, typeRegistry)

        assert(createdType == null)
    }
}