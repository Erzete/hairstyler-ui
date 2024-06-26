package com.dicoding.shavemax.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.shavemax.data.local.preference.SessionPreference
import com.dicoding.shavemax.data.local.preference.UserModel
import com.dicoding.shavemax.data.remote.request.SignInRequest
import com.dicoding.shavemax.data.remote.request.SignUpRequest
import com.dicoding.shavemax.data.remote.response.ErrorResponse
import com.dicoding.shavemax.data.remote.response.HairstyleResponseItem
import com.dicoding.shavemax.data.remote.response.NewsResponse
import com.dicoding.shavemax.data.remote.response.ResultResponse
import com.dicoding.shavemax.data.remote.response.SignUpSuccessResponse
import com.dicoding.shavemax.data.remote.retrofit.ApiService
import com.dicoding.shavemax.utils.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.SocketTimeoutException

class UserRepositoryImpl (private val sessionPreference: SessionPreference, private val apiServiceOne: ApiService, private val apiServiceTwo: ApiService, private val newsApiService : ApiService) : UserRepository{
    override suspend fun saveToken(userModel: UserModel) {
        sessionPreference.saveToken(userModel)
    }

    override fun getToken(): Flow<UserModel> {
        return sessionPreference.getToken()
    }

    override suspend fun signUp(name: String, email: String, password: String, gender: String): SignUpSuccessResponse {
        try {
            return apiServiceOne.signUp(SignUpRequest(name, email, password, gender))
        } catch (e: HttpException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun signIn(email: String, password: String): UserModel {
        try {
            val user = apiServiceOne.signIn(SignInRequest(email, password))
            return UserModel(user.gender, user.name, user.email, user.token)
        } catch (e: HttpException) {
            throw e
        } catch (e: SocketTimeoutException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun logOut() {
        sessionPreference.logOut()
    }

    override fun predict(image: File, gender: String): LiveData<ResultState<ResultResponse>> = liveData {
        emit(ResultState.Loading)
        val imageRequest = image.asRequestBody("image/jpeg".toMediaType())
        val genderRequest = gender.toRequestBody("text/plain".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            name = "image", filename = image.name, body = imageRequest
        )
        try {
            val resultResponse = apiServiceTwo.predict(multipartBody, genderRequest)
            emit(ResultState.Success(resultResponse))
        } catch (e: HttpException) {
            val errorMessage = extractErrorMessage(e)
            emit(ResultState.Error(errorMessage))
        } catch (e: SocketTimeoutException) {
            val errorMessage = "Request timed out. Please try again."
            emit(ResultState.Error(errorMessage))
        } catch (e: Exception) {
            val errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
            emit(ResultState.Error(errorMessage))
        }
    }

    override fun getAllHairstyle(): LiveData<ResultState<List<HairstyleResponseItem>>> = liveData {
        emit(ResultState.Loading)
        try {
            val hairstyleResponse = apiServiceOne.getAllHairstyle()
            emit(ResultState.Success(hairstyleResponse))
        } catch (e: HttpException) {
            val errorMessage = extractErrorMessage(e)
            emit(ResultState.Error(errorMessage))
        } catch (e: SocketTimeoutException) {
            val errorMessage = "Request timed out. Please try again."
            emit(ResultState.Error(errorMessage))
        } catch (e: Exception) {
            val errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
            emit(ResultState.Error(errorMessage))
        }
    }

    override fun getHairstyleNews(): LiveData<ResultState<NewsResponse>> = liveData {
        emit(ResultState.Loading)
        try {
            val newsResponse = newsApiService.getHairstyleNews()
            emit(ResultState.Success(newsResponse))
        } catch (e: HttpException) {
            val errorMessage = extractErrorMessage(e)
            emit(ResultState.Error(errorMessage))
        } catch (e: SocketTimeoutException) {
            val errorMessage = "Request timed out. Please try again."
            emit(ResultState.Error(errorMessage))
        } catch (e: Exception) {
            val errorMessage = "An unexpected error occurred: ${e.localizedMessage}"
            emit(ResultState.Error(errorMessage))
        }
    }

    private fun extractErrorMessage(e: HttpException): String {
        return try {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            errorBody.message
        } catch (ex: Exception) {
            "Unknown Error"
        }
    }

    companion object {
        @Volatile

        private var INSTANCE : UserRepositoryImpl? = null
        fun getRepositoryInstance(sessionPreference: SessionPreference, apiServiceOne: ApiService, apiServiceTwo: ApiService, newsApiService: ApiService) : UserRepositoryImpl{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: UserRepositoryImpl(sessionPreference, apiServiceOne, apiServiceTwo, newsApiService)
            }.also { INSTANCE = it }
        }
    }

}