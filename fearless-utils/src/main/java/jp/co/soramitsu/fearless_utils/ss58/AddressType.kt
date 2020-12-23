package jp.co.soramitsu.fearless_utils.ss58

enum class AddressType(
    val addressByte: Byte,
    val genesisHash: String
) {
    KUSAMA(2.toByte(), "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe"),
    POLKADOT(0.toByte(), "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3"),
    WESTEND(42.toByte(), "e143f23803ac50e8f6f8e62695d1ce9e4e1d68aa36c1cd2cfd15340213f3423e"),
    SORA_TEST_NET(42.toByte(), "04cf75f9ac122df12e846ca93f5a352ff01a01b6605c21c05fd4f57320f3a41f");

    companion object {
        fun fromGenesis(genesisHash: String): AddressType? {
            val withoutPrefix = genesisHash.removePrefix("0x")

            return values().firstOrNull { it.genesisHash == withoutPrefix }
        }
    }
}