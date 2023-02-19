package com.hrblizz.fileapi.data.entities

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Entity {
    @Id
    lateinit var token: String
    lateinit var fileName: String
    var size: Long = 0
    lateinit var source: String
    var expireTime: String? = null
    lateinit var contentType: String
    lateinit var createTime: String
    lateinit var meta: Map<String, Any>
}
