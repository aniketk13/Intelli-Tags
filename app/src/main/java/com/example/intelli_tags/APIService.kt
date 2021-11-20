package com.example.intelli_tags

import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @POST("jobs")
    suspend fun getJobId(
        @HeaderMap headers: Map<String, String>, @Body body:JSONObject
    ): Response<SimpleJSONModel>
}