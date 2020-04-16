package com.project.fidouafetda.component.fidouafclient.tlv

class Hex {

    fun hex8(hexLen: String): String {
        try {
            val sb = StringBuilder()
            sb.append(hexLen)
            if (sb.length == 1) {
                sb.insert(0, '0')
            } else if (sb.length == 2) {
                return sb.toString()
            }
            return sb.toString()
        } catch (e: Exception) {
            throw e
        }
    }

    fun hex16(hexLen: String): String {
        try {
            val sb = StringBuilder()
            sb.append(hexLen)
            when {
                sb.length == 1 -> sb.insert(0, "000")
                sb.length == 2 -> sb.insert(0, "00")
                sb.length == 3 -> sb.insert(0, "0")
                sb.length == 4 -> return sb.toString()
            }
            return sb.toString()
        } catch (e: Exception) {
            throw e
        }
    }

    fun hex32(hexLen: String): String {
        try {
            val sb = StringBuilder()
            sb.append(hexLen)
            when {
                sb.length == 1 -> sb.insert(0, "0000000")
                sb.length == 2 -> sb.insert(0, "000000")
                sb.length == 3 -> sb.insert(0, "000000")
                sb.length == 4 -> sb.insert(0, "0000")
                sb.length == 5 -> sb.insert(0, "000")
                sb.length == 6 -> sb.insert(0, "00")
                sb.length == 7 -> sb.insert(0, "0")
                sb.length == 8 -> return sb.toString()
            }
            return sb.toString()
        } catch (e: Exception) {
            throw e
        }
    }
}