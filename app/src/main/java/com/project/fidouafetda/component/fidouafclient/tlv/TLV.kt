package com.project.fidouafetda.component.fidouafclient.tlv

import java.lang.StringBuilder
import java.util.*
import java.util.stream.Collectors

class TLV {

    private val sethex = Hex()

    fun setTLV(tag: Int, len: Int, value: String):String{
        try {

            val hexTag = parsing(Integer.toHexString(tag))
            val hexLen = parsing(sethex.hex16(Integer.toHexString(len)))

            val tlv = StringBuilder()
            tlv.append(hexTag)
            tlv.append(hexLen)
            tlv.append(value)

            return tlv.toString()
        } catch (e: Exception) {
            throw e
        }
    }

    private fun parsing(input: String): String {
        try {
            //var input = "ABCDEFGHIJKL"

            val str_array = arrayOfNulls<String>(input.length / 2)
            var x = 0
            var y = 2
            for (i in str_array.size - 1 downTo 0) {
                var filter: String?
                filter = input.substring(x, y)
                x += 2
                y += 2
                str_array[i] = filter
            }
            return Arrays.stream(str_array).collect(Collectors.joining())
        } catch (e: Exception) {
            throw e
        }
    }
}