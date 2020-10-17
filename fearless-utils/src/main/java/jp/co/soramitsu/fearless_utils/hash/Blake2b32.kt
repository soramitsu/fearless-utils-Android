package jp.co.soramitsu.fearless_utils.hash

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.jcajce.provider.digest.BCMessageDigest

class Blake2b32 : BCMessageDigest(Blake2bDigest(32)),
    Cloneable {

    override fun clone(): Any {
        val d = super<BCMessageDigest>.clone() as Blake2b32

        d.digest = Blake2bDigest(digest as Blake2bDigest)

        return d
    }
}