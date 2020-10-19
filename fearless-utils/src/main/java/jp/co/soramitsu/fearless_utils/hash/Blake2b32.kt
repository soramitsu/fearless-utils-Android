package jp.co.soramitsu.fearless_utils.hash

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest

class Blake2b32 : BCMessageDigest(Blake2bDigest(32)),
    Cloneable {

    /**
     * Parent method is hidden from consumers because implementation directive is used,
     * meaning there is no transitive visibility
     */
    @Suppress("RedundantOverride")
    override fun digest(input: ByteArray): ByteArray {
        return super.digest(input)
    }

    override fun clone(): Any {
        val d = super<BCMessageDigest>.clone() as Blake2b32

        d.digest = Blake2bDigest(digest as Blake2bDigest)

        return d
    }
}