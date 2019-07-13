package com.ucsdextandroid2.petfinder

import android.util.Log
import androidx.paging.PageKeyedDataSource

/**
 * Created by rjaylward on 2019-07-13
 */

class PetsDataSourceFactory(private val lat: Double?, private val lng: Double?) : androidx.paging.DataSource.Factory<Int, PetModel>(){
    override fun create(): androidx.paging.DataSource<Int, PetModel> {
        return PetsDataSource(lat, lng)
    }

}

class PetsDataSource(private val lat: Double?, private val lng: Double?) : PageKeyedDataSource<Int, PetModel>(){
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, PetModel>) {
        Log.d("PetsDataSource","loadInitial ${params.requestedLoadSize}")
        DataSource.findAnimals(lat, lng, 1, 10){result ->
            val resultWithPagination: ResultWithPaginationInfo = processResult(result)

            callback.onResult(resultWithPagination.data, resultWithPagination.previousPage, resultWithPagination.nextPage)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, PetModel>) {
        Log.d("PetsDataSource","loadAter ${params.key}")
        DataSource.findAnimals(lat, lng, params.key, 10){result ->
            val resultWithPagination: ResultWithPaginationInfo = processResult(result)

            callback.onResult(resultWithPagination.data, resultWithPagination.nextPage)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, PetModel>) {
        if(params.key <= 1){
            callback.onResult(emptyList(), null)
        }
        DataSource.findAnimals(lat, lng, params.key, 10){result ->
            val resultWithPagination: ResultWithPaginationInfo = processResult(result)

            callback.onResult(resultWithPagination.data, resultWithPagination.previousPage)
        }
    }

    class ResultWithPaginationInfo(val data: List<PetModel>, val previousPage: Int?, val nextPage: Int?)

    private fun processResult(apiResult: ApiResult<AnimalsResponse>) : ResultWithPaginationInfo {

        val totalPages: Int = apiResult.data?.pagination?.totalPages ?: 0
        val currentPage: Int? = apiResult.data?.pagination?.currentPage

        val nextPage: Int? = if(currentPage != null && currentPage < totalPages) currentPage + 1 else null
        val previousPage: Int? = if(currentPage != null && currentPage > 1) currentPage - 1 else null

        val data = apiResult.data?.animals?.map{
            return@map PetModel(
                name = "${it.name} (Page $currentPage)",
                imageUrl = it.photos.firstOrNull()?.large,
                breed = it.breeds.primary,
                id = it.id,
                location = "${it.contact.address.city}, ${it.contact.address.state}"
            )
        }.orEmpty()

        return ResultWithPaginationInfo(data, previousPage, nextPage)
    }
}