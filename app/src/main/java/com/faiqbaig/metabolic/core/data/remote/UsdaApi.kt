package com.faiqbaig.metabolic.core.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApi {
    @GET("fdc/v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 20
    ): UsdaSearchResponse
}