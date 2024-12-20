package com.pardeep.rtdbwithsbwithbundlepassing

import com.google.firebase.database.Exclude

data class StudentData(
    var id : String?="",
    var name : String?="",
    var image : String?=""
){
    @Exclude
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "id" to id,
            "name" to name,
            "image" to image
        )
    }
}
