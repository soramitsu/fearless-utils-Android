package jp.co.soramitsu.fearless_utils.runtime.definitions

import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.CollectionEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.DictEnum
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.FixedArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Option
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.SetType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Tuple
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Vec
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.Compact
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.FixedByteArray
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.NumberType
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.primitives.u8
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub.Stub
import java.math.BigInteger
import java.util.Locale

class TypeDefinitionsTree(val types: Map<String, Any>)

class ParseResult(
    val typeRegistry: TypeRegistry,
    val unknownTypes: Set<String>
)

private const val TOKEN_SET = "set"
private const val TOKEN_STRUCT = "struct"
private const val TOKEN_ENUM = "enum"

class TypeDefinitionParser {

    private lateinit var parsedTree: TypeDefinitionsTree
    private lateinit var loweCaseTypes: Map<String, Any>

    private lateinit var unknownTypes: MutableSet<String>
    private lateinit var typeRegistry: TypeRegistry

    private val inProgress = mutableSetOf<String>()

    fun parseTypeDefinitions(
        tree: TypeDefinitionsTree,
        prepopulatedTypeRegistry: TypeRegistry = prepopulatedTypeRegistry()
    ): ParseResult {

        parsedTree = tree
        loweCaseTypes = parsedTree.types.mapKeys { (key, _) -> key.toLowerCase(Locale.ROOT) }

        unknownTypes = mutableSetOf()
        typeRegistry = prepopulatedTypeRegistry

        inProgress.clear()

        for (name in parsedTree.types.keys) {
            retrieveOrParse(name)
        }

        typeRegistry.removeStubs()

        return ParseResult(typeRegistry, unknownTypes)
    }

    private fun retrieveOrParse(name: String): Type<*>? {
        val aliased = typeRegistry[name]

        return aliased ?: parse(name)
    }

    private fun parse(name: String): Type<*>? {
        if (name in inProgress) {
            return Stub(name)
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
            unknownTypes.add(name)
        }

        inProgress -= name

        return typeFromName
    }

    private fun getFromTree(name: String): Any? {
        val withOriginalName = parsedTree.types[name]

        return if (withOriginalName != null) {
            withOriginalName
        } else { // letter case mistake correction
            val lowerCaseName = name.toLowerCase()

            loweCaseTypes[lowerCaseName]
        }
    }

    private fun parseType(name: String, typeValue: Any?): Type<*>? {

        val type: Type<*>? = when (typeValue) {
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
                    TOKEN_STRUCT -> {
                        val typeMapping = typeValueCasted["type_mapping"] as List<List<String>>
                        val children = parseTypeMapping(typeMapping)

                        children?.let { Struct(name, it) }
                    }

                    TOKEN_ENUM -> {
                        val valueList = typeValueCasted["value_list"] as? List<String>
                        val typeMapping = typeValueCasted["type_mapping"] as? List<List<String>>

                        when {
                            valueList != null -> CollectionEnum(name, valueList)

                            typeMapping != null -> {
                                val children = parseTypeMapping(typeMapping)?.map { (name, type) ->
                                    DictEnum.Entry(name, type)
                                }

                                children?.let { DictEnum(name, it) }
                            }
                            else -> null
                        }
                    }

                    TOKEN_SET -> {
                        val valueTypeName = typeValueCasted["value_type"] as String
                        val valueListRaw = typeValueCasted["value_list"] as Map<String, Double>
                        val valueType = retrieveOrParse(valueTypeName) as? NumberType

                        valueType?.let { numberType ->
                            val valueList = valueListRaw.mapValues { (_, value) ->
                                BigInteger(
                                    value.toInt().toString()
                                )
                            }

                            SetType(name, numberType, LinkedHashMap(valueList))
                        }
                    }

                    else -> null
                }
            }

            else -> null
        }

        if (type != null) {
            typeRegistry[name] = type
        }

        return type
    }

    private fun parseTypeMapping(typeMapping: List<List<String>>): LinkedHashMap<String, Type<*>>? {
        val children = LinkedHashMap<String, Type<*>>()

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

    private fun isFixedArray(definition: String) = definition.startsWith("[")

    private fun parseFixedArray(name: String, definition: String): Type<*>? {
        val withoutBrackets = definition.removeSurrounding("[", "]").replace(" ", "")
        val (typeName, lengthRaw) = withoutBrackets.split(";")

        val length = lengthRaw.toInt()

        val type = retrieveOrParse(typeName) ?: return null

        return if (type == u8) {
            FixedByteArray(name, length)
        } else {
            FixedArray(name, length, type)
        }
    }

    private fun isVector(definition: String) = definition.startsWith("Vec<")

    private fun parseVec(name: String, definition: String): Vec? {
        val innerType = definition.removeSurrounding("Vec<", ">")

        val type = retrieveOrParse(innerType) ?: return null

        return Vec(name, type)
    }

    private fun isTuple(definition: String) = definition.startsWith("(")

    private fun parseTuple(name: String, definition: String): Tuple? {
        val innerTypeDefinitions = definition.splitTuple()

        val innerTypes = innerTypeDefinitions.map {
            val result = retrieveOrParse(it)

            result ?: return null
        }

        return Tuple(name, innerTypes)
    }

    private fun isOption(definition: String) = definition.startsWith("Option<")

    private fun parseOption(name: String, definition: String): Option? {
        val innerType = definition.removeSurrounding("Option<", ">")

        val type = retrieveOrParse(innerType) ?: return null

        return Option(name, type)
    }

    private fun isCompact(definition: String) = definition.startsWith("Compact<")

    private fun parseCompact(name: String, definition: String): Compact? {
        val innerType = definition.removeSurrounding("Compact<", ">")

        val type = retrieveOrParse(innerType) as? NumberType ?: return null

        return Compact(name, type)
    }
}