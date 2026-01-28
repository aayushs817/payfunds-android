package com.payfunds.wallet.modules.bank

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.create_core_user.CreateCoreUserRequestModal
import com.payfunds.wallet.network.response_model.create_core_user.CreateCoreUserResponseModal
import com.google.gson.Gson
import android.util.Log
import com.payfunds.wallet.network.response_model.holobank.HolobankErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CreateBankUserViewModel : ViewModel() {

    var uiState by mutableStateOf<ViewState>(ViewState.Success)
        private set

    var createCoreUserResponse by mutableStateOf<CreateCoreUserResponseModal?>(null)
        private set

    private val tokenManager = CrateUserTokenManager(App.instance)

    fun createCoreUser(firstName: String, lastName: String, displayName: String, email: String, phoneNumber: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    val request = CreateCoreUserRequestModal(
                        firstName = firstName,
                        lastName = lastName,
                        displayName = displayName,
                        email = email,
                        phoneNumber = phoneNumber,
                        type = "Simple"
                    )
                    PayFundRetrofitInstance.holoBankApi.createCoreUser(token, request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d("KYC_DEBUG", "createCoreUser: Success - User created")
                        createCoreUserResponse = body
                        uiState = ViewState.Success
                        onSuccess()
                    } else {
                        Log.e("KYC_DEBUG", "createCoreUser: Failed - ${body.message}")
                        uiState = ViewState.Error(Exception(body.message))
                    }
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun uploadKYC(
        title: String,
        nationality: String,
        occupation: String,
        dateOfBirth: String,
        placeOfBirth: String,
        country: String,
        address: String,
        district: String,
        city: String,
        postalCode: String,
        isSameResidentialAddress: Boolean,
        personalIdentificationNumber: Int,
        passportImageFile: File?,
        nationalIdImageFile: File?,
        passportSelfieFile: File?,
        nationalIdSelfieImageFile: File?,
        digitalSignatureFile: File?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    Log.d("KYC_DEBUG", "uploadKYC: Token retrieved")
                    
                    val textType = "text/plain".toMediaTypeOrNull()
                    val imageType = "image/*".toMediaTypeOrNull()

                    val titlePart = title.toRequestBody(textType)
                    val nationalityPart = nationality.toRequestBody(textType)
                    val occupationPart = occupation.toRequestBody(textType)
                    val dateOfBirthPart = dateOfBirth.toRequestBody(textType)
                    val placeOfBirthPart = placeOfBirth.toRequestBody(textType)
                    val countryPart = country.toRequestBody(textType)
                    val addressPart = address.toRequestBody(textType)
                    val districtPart = district.toRequestBody(textType)
                    val cityPart = city.toRequestBody(textType)
                    val postalCodePart = postalCode.toRequestBody(textType)
                    val isSameResidentialAddressPart = isSameResidentialAddress.toString().toRequestBody(textType)
                    val personalIdentificationNumber = personalIdentificationNumber.toString().toRequestBody(textType)

                    val passportImagePart = passportImageFile?.let {
                        MultipartBody.Part.createFormData("passportImage", it.name, it.asRequestBody(imageType))
                    } ?: createEmptyPart("passportImage")

                    val nationalIdImagePart = nationalIdImageFile?.let {
                        MultipartBody.Part.createFormData("nationalIdImage", it.name, it.asRequestBody(imageType))
                    } ?: createEmptyPart("nationalIdImage")

                    val passportSelfiePart = passportSelfieFile?.let {
                        MultipartBody.Part.createFormData("passportSelfie", it.name, it.asRequestBody(imageType))
                    } ?: createEmptyPart("passportSelfie")

                    val nationalIdSelfieImagePart = nationalIdSelfieImageFile?.let {
                        MultipartBody.Part.createFormData("nationalIdSelfieImage", it.name, it.asRequestBody(imageType))
                    } ?: createEmptyPart("nationalIdSelfieImage")

                    val digitalSignaturePart = digitalSignatureFile?.let {
                        MultipartBody.Part.createFormData("digitalSignature", it.name, it.asRequestBody(imageType))
                    } ?: createEmptyPart("digitalSignature")

                    Log.d("KYC_DEBUG", "uploadKYC: Sending request...")
                    PayFundRetrofitInstance.holoBankApi.uploadKYC(
                        token,
                        titlePart,
                        nationalityPart,
                        occupationPart,
                        dateOfBirthPart,
                        placeOfBirthPart,
                        countryPart,
                        addressPart,
                        districtPart,
                        cityPart,
                        postalCodePart,
                        isSameResidentialAddressPart,
                        personalIdentificationNumber,
                        passportImagePart,
                        nationalIdImagePart,
                        passportSelfiePart,
                        nationalIdSelfieImagePart,
                        digitalSignaturePart
                    )
                }

                Log.d("KYC_DEBUG", "uploadKYC: Response received - Success: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                 Log.d("KYC_DEBUG", "uploadKYC: Body received - Success: ${body}")

                        uiState = ViewState.Success
                        onSuccess()
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    private fun createEmptyPart(name: String): MultipartBody.Part {
        val emptyBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, "", emptyBody)
    }

    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, HolobankErrorResponse::class.java)
            errorResponse?.error ?: errorResponse?.message ?: response.message()
        } catch (e: Exception) {
            response.message()
        }
    }
}
