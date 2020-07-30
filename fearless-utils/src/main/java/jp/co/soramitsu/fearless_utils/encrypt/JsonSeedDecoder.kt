package jp.co.soramitsu.fearless_utils.encrypt

import com.google.gson.Gson
import jp.co.soramitsu.fearless_utils.encrypt.model.JsonAccountData

class JsonSeedDecoder(private val gson: Gson) {

    fun decode(json: String): JsonAccountData {
        return gson.fromJson(json, JsonAccountData::class.java)
    }

}