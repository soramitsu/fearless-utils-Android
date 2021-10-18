package jp.co.soramitsu.fearless_utils.runtime.metadata.v14

import jp.co.soramitsu.fearless_utils.scale.Schema
import jp.co.soramitsu.fearless_utils.scale.compactInt
import jp.co.soramitsu.fearless_utils.scale.dataType.EnumType
import jp.co.soramitsu.fearless_utils.scale.dataType.list
import jp.co.soramitsu.fearless_utils.scale.dataType.scalable
import jp.co.soramitsu.fearless_utils.scale.enum
import jp.co.soramitsu.fearless_utils.scale.schema
import jp.co.soramitsu.fearless_utils.scale.string
import jp.co.soramitsu.fearless_utils.scale.uint32
import jp.co.soramitsu.fearless_utils.scale.uint8
import jp.co.soramitsu.fearless_utils.scale.vector

object RuntimeMetadataSchemaV14 : Schema<RuntimeMetadataSchemaV14>() {
    val lookup by schema(LookupSchema)
    val pallets by vector(PalletMetadataV14)
    val extrinsic by schema(ExtrinsicMetadataV14)
    val type by compactInt()
}

object LookupSchema : Schema<LookupSchema>() {
    val types by vector(PortableType)
}

object PortableType : Schema<PortableType>() {
    val id by compactInt()
    val type by schema(RegistryType)
}

object RegistryType : Schema<RegistryType>() {
    val path by vector(jp.co.soramitsu.fearless_utils.scale.dataType.string)
    val params by vector(TypeParameter)
    val def by enum(
        scalable(TypeDefComposite),
        scalable(TypeDefVariant),
        scalable(TypeDefSequence),
        scalable(TypeDefArray),
        list(jp.co.soramitsu.fearless_utils.scale.dataType.compactInt),
        EnumType(TypeDefEnum::class.java),
        scalable(TypeDefCompact),
        scalable(TypeDefBitSequence),
    )
    val docs by vector(jp.co.soramitsu.fearless_utils.scale.dataType.string)
}

enum class TypeDefEnum {
    bool, char, str, u8, u16, u32, u64, u128, u256, i8, i16, i32, i64, i128, i256
}

object TypeDefBitSequence : Schema<TypeDefBitSequence>() {
    val bit_store_type by compactInt()
    val bit_order_type by compactInt()
}

object TypeDefCompact : Schema<TypeDefCompact>() {
    val type by compactInt()
}

object TypeDefArray : Schema<TypeDefArray>() {
    val len by uint32()
    val type by compactInt()
}

object TypeDefSequence : Schema<TypeDefSequence>() {
    val type by compactInt()
}

object TypeDefVariant : Schema<TypeDefVariant>() {
    val variants by vector(TypeDefVariantItem)
}

object TypeDefVariantItem : Schema<TypeDefVariantItem>() {
    val name by string()
    val fields2 by vector(TypeDefCompositeField)
    val index by uint8()
    val docs by vector(jp.co.soramitsu.fearless_utils.scale.dataType.string)
}

object TypeDefComposite : Schema<TypeDefComposite>() {
    val fields2 by vector(TypeDefCompositeField)
}

object TypeDefCompositeField : Schema<TypeDefCompositeField>() {
    val name by string().optional()
    val type by compactInt()
    val typeName by string().optional()
    val docs by vector(jp.co.soramitsu.fearless_utils.scale.dataType.string)
}

object TypeParameter : Schema<TypeParameter>() {
    val name by string()
    val type by compactInt().optional()
}
