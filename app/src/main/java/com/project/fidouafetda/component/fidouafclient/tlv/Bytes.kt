package com.project.fidouafetda.component.fidouafclient.tlv


class Bytes {

    fun bytesToHex(bytes: ByteArray): String {
        try {
            val hexString = StringBuilder()
            for (i in bytes.indices) {
                val hex = Integer.toHexString(0xff and bytes[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }

    fun hexToBytes(s: String): ByteArray {
        try {
            val b = ByteArray(s.length / 2)
            for (i in b.indices) {
                val index = i * 2
                val v = Integer.parseInt(s.substring(index, index + 2), 16)
                b[i] = v.toByte()
            }
            return b
        } catch (ex: Exception) {
            throw Exception(ex.message)
        }
    }
}