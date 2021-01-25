package jp.co.soramitsu.fearless_utils.runtime.definitions.types.stub

import jp.co.soramitsu.fearless_utils.runtime.definitions.TypeRegistry
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.Type
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.TypeMapping

internal fun <T : Type<*>> T.replaceStubsWithChildren(
    registry: TypeRegistry,
    children: TypeMapping,
    copyCreator: (newChildren: TypeMapping) -> T
): T {
    var changed = 0

    val newChildren = children.mapValuesTo(LinkedHashMap()) { (_, type) ->
        val newType = type.replaceStubs(registry)

        if (newType !== type) changed++

        newType
    }

    return if (changed > 0) copyCreator(newChildren) else this
}

internal fun <T : Type<*>> T.replaceStubsWithChildren(
    registry: TypeRegistry,
    children: List<Type<*>>,
    copyCreator: (newChildren: List<Type<*>>) -> T
): T {
    var changed = 0

    val newChildren = children.map { type ->
        val newType = type.replaceStubs(registry)

        if (newType !== type) changed++

        newType
    }

    return if (changed > 0) copyCreator(newChildren) else this
}

internal fun <T : Type<*>> T.replaceStubsWithChild(
    registry: TypeRegistry,
    child: Type<*>,
    copyCreator: (newChild: Type<*>) -> T
): T {
    val updatedChild = child.replaceStubs(registry)

    return if (updatedChild !== child) {
        copyCreator(updatedChild)
    } else {
        this
    }
}