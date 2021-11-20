package com.example.intelli_tags

import com.google.gson.annotations.SerializedName

data class SimpleJSONModel(
    @SerializedName("jobIdentifier")
    var jobId: String
)
