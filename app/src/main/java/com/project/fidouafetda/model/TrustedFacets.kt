package com.project.fidouafetda.model

class TrustedFacets {
    private var version: Version? = null
    private var ids: Array<String>? = null

    fun TrustedFacets(){

    }

    fun getVersion(): Version? {
        return version
    }

    fun setVersion(version: Version) {
        this.version = version
    }

    fun getIds(): Array<String>? {
        return ids
    }

    fun setIds(ids: Array<String>) {
        this.ids = ids
    }
}