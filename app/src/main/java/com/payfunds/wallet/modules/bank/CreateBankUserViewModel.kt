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
import com.payfunds.wallet.network.response_model.get_user_details.GetUserDetailsResponseModal
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

    var userDetails by mutableStateOf<GetUserDetailsResponseModal?>(null)
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
        firstName: String?,
        lastName: String?,
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
        personalIdentificationNumber: String,
        passportImageFile: File?,
        nationalIdImageFile: File?,
        passportSelfieFile: File?,
        nationalIdSelfieImageFile: File?,
        digitalSignatureFile: File,
        onSuccess: () -> Unit
    ) {
    viewModelScope.launch {
        uiState = ViewState.Loading
        Log.d("KYC_DEBUG", "uploadKYC started: title=$title, firstName=$firstName, lastName=$lastName")
        try {
            val response = withContext(Dispatchers.IO) {
                val token = "Bearer " + tokenManager.crateUserGetToken()
                Log.d("KYC_DEBUG", "uploadKYC: Token retrieved")

                val textType = "text/plain".toMediaTypeOrNull()
                val imageType = "image/*".toMediaTypeOrNull()

                // Safe null handling for name parts
                val fName = firstName ?: ""
                val lName = lastName ?: ""

                val titlePart = title.toRequestBody(textType)
                val firstNamePart = fName.toRequestBody(textType)
                val lastNamePart = lName.toRequestBody(textType)
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
                val personalIdentificationNumberPart = personalIdentificationNumber.toRequestBody(textType)

                Log.d(
                    "KYC_DEBUG",
                    "uploadKYC files: passport=${passportImageFile?.length()}, nationalId=${nationalIdImageFile?.length()}, passportSelfie=${passportSelfieFile?.length()}, nationalIdSelfie=${nationalIdSelfieImageFile?.length()}, signature=${digitalSignatureFile.length()}"
                )

                val passportImagePart = passportImageFile?.let {
                    MultipartBody.Part.createFormData("passportImage", it.name, it.asRequestBody(imageType))
                }
                val nationalIdImagePart = nationalIdImageFile?.let {
                    MultipartBody.Part.createFormData("nationalIdImage", it.name, it.asRequestBody(imageType))
                }
                val passportSelfiePart = passportSelfieFile?.let {
                    MultipartBody.Part.createFormData("passportSelfie", it.name, it.asRequestBody(imageType))
                }
                val nationalIdSelfieImagePart = nationalIdSelfieImageFile?.let {
                    MultipartBody.Part.createFormData("nationalIdSelfieImage", it.name, it.asRequestBody(imageType))
                }
                val digitalSignaturePart = MultipartBody.Part.createFormData(
                    "digitalSignature", digitalSignatureFile.name, digitalSignatureFile.asRequestBody(imageType)
                )

                Log.d("KYC_DEBUG", "uploadKYC: Sending request to holoBankApi...")
                PayFundRetrofitInstance.holoBankApi.uploadKYC(
                    token, titlePart, firstNamePart, lastNamePart, nationalityPart, occupationPart,
                    dateOfBirthPart, placeOfBirthPart, countryPart, addressPart, districtPart, cityPart,
                    postalCodePart, isSameResidentialAddressPart, personalIdentificationNumberPart,
                    passportImagePart, nationalIdImagePart, passportSelfiePart, nationalIdSelfieImagePart,
                    digitalSignaturePart
                )
            }

            Log.d("KYC_DEBUG", "uploadKYC: Response received. Code=${response.code()}, isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("KYC_DEBUG", "uploadKYC: Body received - isSuccess=${body.isSuccess}, message=${body.message}")

                if (body.isSuccess) {
                    uiState = ViewState.Success
                    Log.d("KYC_DEBUG", "uploadKYC: Calling onSuccess callback")
                   onSuccess()
                } else {
                    Log.e("KYC_DEBUG", "uploadKYC: Backend returned business error - ${body.message}")
                    uiState = ViewState.Error(Exception(body.message))
                }
            } else {
                val errorMsg = getErrorMessage(response)
                Log.e("KYC_DEBUG", "uploadKYC: Request failed or empty body. Error: $errorMsg")
                uiState = ViewState.Error(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("KYC_DEBUG", "uploadKYC: Exception caught", e)
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
