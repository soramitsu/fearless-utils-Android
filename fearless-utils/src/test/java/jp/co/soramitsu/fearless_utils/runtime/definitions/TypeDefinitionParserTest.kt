package jp.co.soramitsu.fearless_utils.runtime.definitions

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.composite.Struct
import jp.co.soramitsu.fearless_utils.runtime.definitions.types.fromHex
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.FileReader
import java.math.BigInteger


@RunWith(JUnit4::class)
class TypeDefinitionParserTest {


    @Test
    fun `should parse substrate data`() {
        val gson = Gson()
        val reader =
            JsonReader(FileReader(".\\src\\test\\java\\jp\\co\\soramitsu\\fearless_utils\\runtime\\test.json"))
        val tree = gson.fromJson<TypeDefinitionsTree>(reader, TypeDefinitionsTree::class.java)

        val parser = TypeDefinitionParser()

        val result = parser.parseTypeDefinitions(tree)
        val registry = result.typeRegistry

        val registryEntries = registry.all()
        val types = registryEntries.map { it.second }

        print("Parsed Types[${types.size}] ${types}\n\n")
        print("Unknown Types[${result.unknownTypes.size}] ${result.unknownTypes}\n\n")

        val encoded = "0x2d00000002000000c3a7c2dc960d000000000000000000000000000000000000000000000000000080a2cae568050000000000000000000080a2cae5680500000000000000000000"
        val type = registry.get<Struct>("AccountInfo")!!

        val decoded = type.fromHex(encoded)

        assertEquals(0, result.unknownTypes.size)
        assertEquals(decoded.get<Struct.Instance>("data")!!.get<BigInteger>("free"), BigInteger("14941599999939"))
    }
}