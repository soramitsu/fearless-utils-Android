package jp.co.soramitsu.fearless_utils.runtime

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.FileReader

internal fun <T : Type> T.replaceStubsWithChildren(
    registry: Map<String, Type>,
    children: LinkedHashMap<String, Type>,
    copyCreator: (newChildren: LinkedHashMap<String, Type>) -> T
): T {
    var changed = 0

    val newChildren = children.mapValuesTo(LinkedHashMap()) { (_, type) ->
        val newType = type.replaceStubs(registry)

        if (newType !== type) changed++

        newType
    }

    return if (changed > 0) copyCreator(newChildren) else this
}

internal fun <T : Type> T.replaceStubsWithChildren(
    registry: Map<String, Type>,
    children: List<Type>,
    copyCreator: (newChildren: List<Type>) -> T
): T {
    var changed = 0

    val newChildren = children.map { type ->
        val newType = type.replaceStubs(registry)

        if (newType !== type) changed++

        newType
    }

    return if (changed > 0) copyCreator(newChildren) else this
}

internal fun <T : Type> T.replaceStubsWithChild(
    registry: Map<String, Type>,
    child: Type,
    copyCreator: (newChild: Type) -> T
): T {
    val updatedChild = child.replaceStubs(registry)

    return if (updatedChild !== child) {
        copyCreator(updatedChild)
    } else {
        this
    }
}

abstract class Type(val name: String) {

    /**
     * Contract - if nothing was replaced, the return type should be === current
     */
    internal abstract fun replaceStubs(registry: Map<String, Type>): Type

    override fun toString(): String {
        return name
    }

    class Struct(name: String, val children: LinkedHashMap<String, Type>) : Type(name) {
        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChildren(registry, children) { newChildren ->
                Struct(name, newChildren)
            }
        }
    }

    class Enum(name: String, val content: EnumContent) : Type(name) {

        sealed class EnumContent {
            class Collection(val elements: List<String>) : EnumContent()

            class Dict(val elements: LinkedHashMap<String, Type>) : EnumContent()
        }

        override fun replaceStubs(registry: Map<String, Type>): Type {
            return when (content) {
                is EnumContent.Collection -> this // no stubs possible
                is EnumContent.Dict -> replaceStubsWithChildren(
                    registry,
                    content.elements
                ) { newChildren ->
                    Enum(name, EnumContent.Dict(newChildren))
                }
            }
        }
    }

    class Set(name: String, val valueType: Type, val valueList: LinkedHashMap<String, Any>) :
        Type(name) {
        override fun replaceStubs(registry: Map<String, Type>) =
            replaceStubsWithChild(registry, valueType) { newChild ->
                Set(name, newChild, valueList)
            }
    }

    class FixedArray(name: String, val length: Int, val type: Type) : Type(name) {

        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChild(registry, type) { newChild ->
                FixedArray(name, length, newChild)
            }
        }
    }

    class Vec(name: String, val type: Type) : Type(name) {

        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChild(registry, type) { newChild ->
                Vec(name, newChild)
            }
        }
    }

    class Tuple(name: String, val types: List<Type>) : Type(name) {

        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChildren(registry, types) { newChildren ->
                Tuple(name, newChildren)
            }
        }
    }

    class Option(name: String, val type: Type) : Type(name) {
        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChild(registry, type) { newChildren ->
                Option(name, newChildren)
            }
        }
    }

    class Compact(name: String, val type: Type) : Type(name) {
        override fun replaceStubs(registry: Map<String, Type>): Type {
            return replaceStubsWithChild(registry, type) { newChildren ->
                Compact(name, newChildren)
            }
        }
    }

    class Stub(name: String) : Type(name) {
        override fun replaceStubs(registry: Map<String, Type>): Type {
            return registry[name] ?: throw StubNotResolvedException(name)
        }

        override fun toString(): String {
            return "STUB($name)"
        }
    }
}

class StubNotResolvedException(val stubName: String) : Exception()

class TypeDefinitions(val types: Map<String, Any>)

@RunWith(JUnit4::class)
class TypeDefinitionsTest {

    val gson = Gson()

    val types = mutableMapOf<String, Type>()
    val unknownTypes = mutableSetOf<String>()

    val inProgress = mutableSetOf<String>()

    lateinit var parsedTree: TypeDefinitions

    lateinit var loweCaseTypes: Map<String, Any>

    fun registerType(type: Type) {
        types[type.name] = type
    }

    fun registerType(name: String) {
        val type = object : Type(name) {
            override fun replaceStubs(registry: Map<String, Type>) = this
        }

        registerType(type)
    }

    @Before
    fun setup() {
        // register not declared fields

        registerType("Null")
        registerType("u128")
        registerType("GenericBlock")
        registerType("GenericCall")
        registerType("H160")
        registerType("H256")
        registerType("H512")
        registerType("u64")
        registerType("GenericAccountId")
        registerType("GenericVote")
        registerType("u32")
        registerType("u16")
        registerType("bool")
        registerType("u8")
        registerType("u64")
        registerType("Bytes")
        registerType("BitVec")
        registerType("U64")
        registerType("ExtrinsicsDecoder")
        registerType("CallBytes")
        registerType("Era")
        registerType("Data")
        registerType("BoxProposal")
        registerType("GenericConsensusEngineId")
        registerType("SessionKeysSubstrate")
        registerType("GenericMultiAddress")
        registerType("OpaqueCall") // declared as "OpaqueCall": "OpaqueCall"
        registerType("GenericAccountIndex") // declared as "OpaqueCall": "OpaqueCall"
        registerType("GenericEvent") // declared as "OpaqueCall": "OpaqueCall"
        registerType("EventRecord") // "EventRecord": "EventRecord"
        registerType("u256") // "EventRecord": "EventRecord"
    }

    @Test
    fun testParsing() {
        val reader =
            JsonReader(FileReader(".\\src\\test\\java\\jp\\co\\soramitsu\\fearless_utils\\runtime\\test.json"))

        parsedTree = gson.fromJson(reader, TypeDefinitions::class.java)
        loweCaseTypes = parsedTree.types.mapKeys { (key, _) -> key.toLowerCase() }

        for (name in parsedTree.types.keys) {
            retrieveOrParse(name)
        }

        types.forEach { (name, type) ->
            val updated = type.replaceStubs(types)

            if (updated !== type) {
                types[name] = updated
            }
        }

        print("Parsed Types[${types.size}] ${types.keys}\n\n")
        print("Unknown Types[${unknownTypes.size}] $unknownTypes\n\n")
    }

    @Test
    fun testSplitTuple() {
        val splitted = splitTuple("(ParaId, CollatorId)")

        assertEquals(listOf("ParaId", "CollatorId"), splitted)
    }

    @Test
    fun testSplitTuple2() {
        val splitted = splitTuple("(BalanceOf<T, I>, BidKind<AccountId, BalanceOf<T, I>>)")

        assertEquals(listOf("BalanceOf<T,I>", "BidKind<AccountId,BalanceOf<T,I>>"), splitted)
    }

    @Test
    fun testSplitTuple3() {
        val splitted = splitTuple("(NominatorIndex, [CompactScore; 0], ValidatorIndex)")

        assertEquals(listOf("NominatorIndex", "[CompactScore;0]", "ValidatorIndex"), splitted)
    }

    @Test
    fun testSplitTuple4() {
        val splitted = splitTuple("(ParaId, Option<(CollatorId, Retriable)>)")

        assertEquals(listOf("ParaId", "Option<(CollatorId,Retriable)>"), splitted)
    }

    fun retrieveOrParse(name: String): Type? {
        val aliased = types[name]

        return aliased ?: parse(name)
    }

    private fun parseVec(name: String, definition: String): Type.Vec? {
        val innerType = definition.removeSurrounding("Vec<", ">")

        val type = retrieveOrParse(innerType)

        return type?.let { Type.Vec(name, type) }
    }

    private fun parseTuple(name: String, definition: String): Type.Tuple? {
        val innerTypeDefinitions = splitTuple(definition)

        val innerTypes = innerTypeDefinitions.map {
            val result = retrieveOrParse(it)

            result ?: return null
        }

        return Type.Tuple(name, innerTypes)
    }

    private fun parseOption(name: String, definition: String): Type.Option? {
        val innerType = definition.removeSurrounding("Option<", ">")

        val type = retrieveOrParse(innerType)

        return type?.let { Type.Option(name, type) }
    }

    private fun parseCompact(name: String, definition: String): Type.Compact? {
        val innerType = definition.removeSurrounding("Compact<", ">")

        val type = retrieveOrParse(innerType)

        return type?.let { Type.Compact(name, type) }
    }

    private fun splitTuple(tupleDefinition: String): List<String> {
        val innerPart = tupleDefinition.removeSurrounding("(", ")").replace(" ", "")

        val result = mutableListOf<String>()
        var bracketsCount = 0
        var currentBeginning = 0

        innerPart.forEachIndexed { index, c ->
            when (c) {
                '(', '<', '[' -> bracketsCount++
                ')', '>', ']' -> bracketsCount--
                ',' -> {
                    if (bracketsCount == 0) {
                        result += innerPart.substring(currentBeginning, index)
                        currentBeginning = index + 1
                    }
                }
            }
        }

        if (currentBeginning < innerPart.length - 1) {
            result += innerPart.substring(currentBeginning, innerPart.length)
        }

        return result
    }

    private fun parseFixedArray(name: String, definition: String): Type.FixedArray? {
        val withoutBrackets = definition.removeSurrounding("[", "]").replace(" ", "")
        val (typeName, lengthRaw) = withoutBrackets.split(";")

        val length = lengthRaw.toInt()

        val type = retrieveOrParse(typeName)

        return type?.let { Type.FixedArray(name, length, it) }
    }

    private fun isVector(definition: String) = definition.startsWith("Vec<")
    private fun isFixedArray(definition: String) = definition.startsWith("[")
    private fun isTuple(definition: String) = definition.startsWith("(")
    private fun isOption(definition: String) = definition.startsWith("Option<")
    private fun isCompact(definition: String) = definition.startsWith("Compact<")

    private fun getFromTree(name: String): Any? {
        val withOriginalName = parsedTree.types[name]

        return if (withOriginalName != null) {
            withOriginalName
        } else { // letter case mistake correction
            val lowerCaseName = name.toLowerCase()

            loweCaseTypes[lowerCaseName]
        }
    }

    fun parse(name: String): Type? {
        if (name in inProgress) {
            return Type.Stub(name)
        }

        inProgress += name

        val typeValue = getFromTree(name)

        val typeFromValue = parseType(name, typeValue)

        if (typeFromValue != null) {
            inProgress -= name

            return typeFromValue
        }

        val typeFromName = parseType(name, name)

        if (typeFromName == null) {
            unknownTypes += name
        }

        inProgress -= name

        return typeFromName
    }

    fun parseType(name: String, typeValue: Any?): Type? {

        val type: Type? = when (typeValue) {
            is String -> {
                when {
                    isFixedArray(typeValue) -> parseFixedArray(name, typeValue)
                    isVector(typeValue) -> parseVec(name, typeValue)
                    isOption(typeValue) -> parseOption(name, typeValue)
                    isCompact(typeValue) -> parseCompact(name, typeValue)
                    isTuple(typeValue) -> parseTuple(name, typeValue)
                    typeValue == name -> null // avoid infinite recursion
                    else -> retrieveOrParse(typeValue)
                }
            }

            is Map<*, *> -> {
                val typeValueCasted = typeValue as Map<String, Any?>

                val compoundType = typeValueCasted["type"]

                when (compoundType) {
                    "struct" -> {
                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
                        val children = parseTypeMapping(typeMapping)

                        children?.let { Type.Struct(name, it) }
                    }

                    "enum" -> {
                        val valueList = typeValueCasted["value_list"] as? List<String>
                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>

                        when {
                            valueList != null -> Type.Enum(
                                name,
                                Type.Enum.EnumContent.Collection(valueList)
                            )
                            typeMapping != null -> {
                                val children = parseTypeMapping(typeMapping)

                                children?.let { Type.Enum(name, Type.Enum.EnumContent.Dict(it)) }
                            }
                            else -> null
                        }
                    }

                    "set" -> {
                        val valueTypeName = typeValueCasted["value_type"] as String
                        val valueList = typeValueCasted["value_list"] as Map<String, Any>
                        val valueType = retrieveOrParse(valueTypeName)

                        valueType?.let { Type.Set(name, it, LinkedHashMap(valueList)) }
                    }

                    else -> null
                }
            }
            else -> null
        }

        if (type != null) {
            types[name] = type
        }

        return type
    }

    private fun parseTypeMapping(typeMapping: List<List<String>>): LinkedHashMap<String, Type>? {
        val children = LinkedHashMap<String, Type>()

        for ((fieldName, fieldType) in typeMapping) {
            val type = retrieveOrParse(fieldType)

            if (type != null) {
                children[fieldName] = type
            } else {
                break
            }
        }

        return if (children.size < typeMapping.size) {
            null
        } else {
            children
        }
    }
}