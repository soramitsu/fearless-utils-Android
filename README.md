# fearless-utils-Android
fearless-utils-Android (This repository is managed by Terraform!)

## Scale

Library provides a convinient dsl to deal with scale encoding/decoding. Orinial codec reference: [Link](https://substrate.dev/docs/en/knowledgebase/advanced/codec).
### Define a schema

``` kotlin
object AccountData : Schema<AccountData>() {
    val free by uint128()
    val reserved by uint128()
    val miscFrozen by uint128()
    val feeFrozen by uint128()
}
```
### Create and encode structs

``` kotlin
val struct = AccountData { data ->
    data[free] = BigDecimal("1")
    data[reserved] = BigInteger("0")
    data[miscFrozen] = BigInteger("0")
    data[feeFrozen] = BigInteger("0")
}

val inHex = struct.toHexString() // encode
val asBytes = struct.toByteArray() // or as byte array
```
###  Decode and use structs

``` kotlin
val inHex = ...
val struct = AccountData.read(inHex)
val free = struct[AccountData.free]
```

### Standart Data Types
Library provides the support for the following data types:
* Numbers: `uint8`, `uint16`, `uint32`, `uint64`, `uint128`, `uint(nBytes)`, `compactInt`, `byte`, `long`
* Primitives: `bool`, `string`
* Arrays:
    * `sizedByteArray(n)` - only content is encoded/decoded), size is thus known in advance
    * `byteArray` - size can vary, so the size is also encoded/decoded alsongside with the content
* Compound types: 
    * `vector<D>` - List of objects of the some data type
    * `optional<D>` - Nullable container for other data type
    * `pair<D1, D2>`
    * `enum(D1, D2, D3...)` - like union in C, stores only one value at once, but this value can have different data type

### Custom Data Types
If the decoding/endcoding cannot be done using standart data types, you can create your own by extending `DataType<T>`:
``` kotlin
object Delimiter : DataType<Byte>() {
    override fun conformsType(value: Any?): Boolean {
        return value is Byte && value == 0
    }
    
    override fun read(reader: ScaleCodecReader): Byte {
        val read = reader.readByte()

        if (read != 0.toByte()) throw java.lang.IllegalArgumentException("Delimiter is not 0")

        return 0
    }

    override fun write(writer: ScaleCodecWriter, ignored: Byte) {
        writer.writeByte(0)
    }
}
```
And use it in yout schema using `custom()` keyword:
``` kotlin
object CustomTypeTest : Schema<CustomTypeTest>() {
    val delimiter by custom(Delimiter)
}
````

### Default values
You can supply and default values for each field in the schema:
``` kotlin
object DefaultValues : Schema<DefaultValues>() {
    val bytes by sizedByteArray(length = 10, default = ByteArray(10))
    val text by string(default = "Default")
    val bigInteger by uint128(default = BigInteger.TEN)
}
```

### Nullable fields

By default, all fields are non null. However, you can use `optional()` to change the default behavior:
``` kotlin
object Person : Schema<Person>() {
    val friendName by string().optional() // friendName now is Field<String?>
}
```

