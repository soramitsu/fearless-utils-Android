/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.soramitsu.fearless_utils_android.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Utils {

    private var digest: MessageDigest? = null

    init {
        digest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun doubleDigest(input: ByteArray): ByteArray? {
        return doubleDigest(input, 0, input.size)
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. The resulting hash is in big endian form.
     */
    /**
     * See [Utils.doubleDigest].
     */
    fun doubleDigest(
        input: ByteArray,
        offset: Int = 0,
        length: Int = input.size
    ): ByteArray {
        synchronized(digest!!) {
            digest!!.reset()
            digest!!.update(input, offset, length)
            val first = digest!!.digest()
            return digest!!.digest(first)
        }
    }
}