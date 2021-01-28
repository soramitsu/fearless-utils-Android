package jp.co.soramitsu.fearless_utils.runtime.definitions

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.common.assertInstance
import jp.co.soramitsu.fearless_utils.common.getResourceReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.CompactExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.FixedArrayExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.GenericsExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.OptionExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.TupleExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.extensions.VectorExtension
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.kusamaBaseTypes
import jp.co.soramitsu.fearless_utils.runtime.definitions.registry.preprocessors.RemoveGenericNoisePreprocessor
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Alias
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.CollectionEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.BooleanType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.NumberType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u32
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.FakeType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class TypeDefinitionParserTest {

    val gson = Gson()

    @Test
    fun `should resolve typealias`() {
        val A = FakeType("A")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
        }

        val definitions = definitions {
            """
            "B": "A"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val B = resultRegistry["B"]

        assertInstance<Alias>(B)
        assertEquals(A, B.aliasedReference.value)
    }

    @Test
    fun `should resolve struct`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "struct",
                "type_mapping": [
                    [
                        "a",
                        "A"
                    ],
                    [
                        "b",
                        "B"
                    ]
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Struct>(C)

        assertEquals(C["a"], A)
        assertEquals(C["b"], B)
    }

    @Test
    fun `should resolve dict enum`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "enum",
                "type_mapping": [
                    [
                        "a",
                        "A"
                    ],
                    [
                        "b",
                        "B"
                    ]
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<DictEnum>(C)

        assertEquals(C["A"], A)
        assertEquals(C["B"], B)
    }

    @Test
    fun `should resolve collection enum`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "enum",
                "value_list": [
                    "A",
                    "B"
                ]
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<CollectionEnum>(C)

        assertEquals(C[0], "A")
        assertEquals(C[1], "B")
    }

    @Test
    fun `should resolve set`() {

        val initialTypeRegistry = typeRegistry {
            registerType(u8)
        }

        val definitions = definitions {
            """
            "C": {
                "type": "set",
                "value_type": "u8",
                "value_list": {
                    "A": 1,
                    "B": 2
                }
            }
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<SetType>(C)

        assertEquals(C["A"], 1.toBigInteger())
        assertEquals(C["B"], 2.toBigInteger())
    }

    @Test
    fun `should resolve fixed array`() {

        val initialTypeRegistry = typeRegistry {
            registerType(BooleanType)
        }

        val definitions = definitions {
            """
            "C": "[bool; 5]"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<FixedArray>(C)

        assertEquals(C.length, 5)
        assertEquals(C.typeReference.value, BooleanType)
    }

    @Test
    fun `should resolve fixed u8 array optimized`() {

        val initialTypeRegistry = typeRegistry {
            registerType(u8)
        }

        val definitions = definitions {
            """
            "C": "[u8; 5]"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<FixedByteArray>(C)

        assertEquals(C.length, 5)
    }

    @Test
    fun `should resolve vector`() {

        val initialTypeRegistry = typeRegistry {
            registerType(u8)
        }

        val definitions = definitions {
            """
            "C": "Vec<u8>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Vec>(C)

        assertEquals(C.typeReference.value, u8)
    }

    @Test
    fun `should resolve tuple`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": "(A, B)"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Tuple>(C)

        assertEquals(C[0], A)
        assertEquals(C[1], B)
    }

    @Test
    fun `should resolve option`() {
        val A = FakeType("A")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
        }

        val definitions = definitions {
            """
            "C": "Option<A>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Option>(C)

        assertEquals(C.innerType, A)
    }

    @Test
    fun `should resolve complex type`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": "Vec<(A, Option<(A, B)>)>"
            """
        }

        val resultRegistry = parseFromJson(initialTypeRegistry, definitions)

        val C = resultRegistry["C"]

        assertInstance<Vec>(C)

        val innerTuple = C.innerType
        assertInstance<Tuple>(innerTuple)

        assertEquals(A, innerTuple[0])

        val innerOption = innerTuple[1]
        assertInstance<Option>(innerOption)

        val leafTuple = innerOption.innerType
        assertInstance<Tuple>(leafTuple)

        assertEquals(A, leafTuple[0])
        assertEquals(B, leafTuple[1])
    }

    @Test
    fun `should put unresolved type to unknown`() {
        val A = FakeType("A")
        val B = FakeType("B")

        val initialTypeRegistry = typeRegistry {
            registerType(A)
            registerType(B)
        }

        val definitions = definitions {
            """
            "C": "F"
            """
        }

        val tree = gson.fromJson(definitions, TypeDefinitionsTree::class.java)

        val unknown = TypeDefinitionParser.parseTypeDefinitions(tree, initialTypeRegistry).unknownTypes

        assert("F" in unknown)
    }

    @Test
    fun `should parse substrate data`() {
        val gson = Gson()
        val reader = JsonReader(getResourceReader("default.json"))
        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)

        val result = TypeDefinitionParser.parseTypeDefinitions(tree)

        print(result.unknownTypes)
        assertEquals(0, result.unknownTypes.size)
    }

    @Test
    fun `should parse network-specific patches`() {
        val gson = Gson()

        val defaultReader = JsonReader(getResourceReader("default.json"))
        val kusamaReader = JsonReader(getResourceReader("kusama.json"))

        val defaultTree = gson.fromJson<TypeDefinitionsTree>(defaultReader, TypeDefinitionsTree::class.java)
        val kusamaTree = gson.fromJson<TypeDefinitionsTree>(kusamaReader, TypeDefinitionsTree::class.java)

        val defaultParsed = TypeDefinitionParser.parseTypeDefinitions(defaultTree)

        val kusamaParsed = TypeDefinitionParser.parseTypeDefinitions(kusamaTree, defaultParsed.typeRegistry + kusamaBaseTypes(), forceOverride = true)

        print(kusamaParsed.unknownTypes)

        assertEquals(0, kusamaParsed.unknownTypes.size)

        val refCount = kusamaParsed.typeRegistry.get<NumberType>("RefCount")
        assertEquals(refCount, u32)

        val address = kusamaParsed.typeRegistry.get<FakeType>("Address")
        assertEquals("AccountIdAddress", address?.name)
    }

    private fun typeRegistry(builder: TypeRegistry.() -> Unit) = TypeRegistry().apply {
        addExtension(VectorExtension)
        addExtension(CompactExtension)
        addExtension(OptionExtension)
        addExtension(FixedArrayExtension)
        addExtension(TupleExtension)
        addExtension(GenericsExtension)

        addPreprocessor(RemoveGenericNoisePreprocessor)

        builder(this)
    }

    private fun parseFromJson(initialRegistry: TypeRegistry, json: String): TypeRegistry {
        val tree = gson.fromJson(json, TypeDefinitionsTree::class.java)

        return TypeDefinitionParser.parseTypeDefinitions(tree, initialRegistry).typeRegistry
    }

    private fun definitions(builder: () -> String): String {
        return """
            { 
            "types": {
                    ${builder.invoke()}
                }
             }
        """.trimIndent()
    }
}