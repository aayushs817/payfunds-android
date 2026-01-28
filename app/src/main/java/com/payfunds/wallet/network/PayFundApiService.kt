package com.payfunds.wallet.network

import com.payfunds.wallet.network.request_model.add_token.AddTokenToWallet
import com.payfunds.wallet.network.request_model.alert.cancel.CancelAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.create.CreateAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.list.ListAlertRequestModel
import com.payfunds.wallet.network.request_model.card_freeze.CardFreezeRequestModal
import com.payfunds.wallet.network.request_model.check_wallet_address.TwoFactorAuthCheckWalletAddressRequestModel
import com.payfunds.wallet.network.request_model.request_card.RequestCardRequestModal
import com.payfunds.wallet.network.request_model.create_core_user.CreateCoreUserRequestModal
import com.payfunds.wallet.network.request_model.fcm.FCMRegisterRequestModel
import com.payfunds.wallet.network.request_model.notification_mark_as_read.NotificationMarkAsReadRequestModel
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.network.request_model.send_transaction_details.SendTransactionDetailsRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.disable.TwoFADisableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.enable.TwoFAEnableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.recover.TwoFARecoverRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.verify.TwoFAVerifyRequestModel
import com.payfunds.wallet.network.request_model.update_card_3ds.UpdateCard3dsForwardingRequestModal
import com.payfunds.wallet.network.request_model.update_card_label.UpdateCardLabelRequestModal
import com.payfunds.wallet.network.request_model.update_card_pin.UpdateCardPinRequestModal
import com.payfunds.wallet.network.request_model.withdraw.WithdrawRequestModal
import com.payfunds.wallet.network.response_model.alert.cancel.CancelAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.create.CreateAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.list.ListAlertResponseModel
import com.payfunds.wallet.network.response_model.card_balance.GetAccountBalanceResponseModal
import com.payfunds.wallet.network.response_model.card_freeze.CardFreezeResponeModal
import com.payfunds.wallet.network.response_model.request_card.RequestCardResponseModal
import com.payfunds.wallet.network.response_model.create_core_user.CreateCoreUserResponseModal
import com.payfunds.wallet.network.response_model.delete_card.DeleteCardResponseModal
import com.payfunds.wallet.network.response_model.deposit_info.DepositInfoResponseModal
import com.payfunds.wallet.network.response_model.fcm.FCMRegisterResponseModel
import com.payfunds.wallet.network.response_model.get_all_notification.GetAllNotificationResponseModel
import com.payfunds.wallet.network.response_model.get_card_3ds.GetCard3dsForwardingResponseModal
import com.payfunds.wallet.network.response_model.get_card_details.GetCardDetailsResponseModal
import com.payfunds.wallet.network.response_model.get_card_info.GetCardInfoResponseModal
import com.payfunds.wallet.network.response_model.get_card_transactions.GetCardTransactionsResponseModal
import com.payfunds.wallet.network.response_model.get_user_details.GetUserDetailsResponseModal
import com.payfunds.wallet.network.response_model.notification_mark_as_read.NotificationMarkAsReadResponseModel
import com.payfunds.wallet.network.response_model.remove_fcm.RemoveFcmTokenResponseModel
import com.payfunds.wallet.network.response_model.send_transaction_details.SendTransactionDetailsResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.check_wallet_address.TwoFactorAuthCheckWalletAddressResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.disable.TwoFADisableResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.enable.TwoFAEnableResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.qr.TwoFAQRCodeResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.recover.TwoFARecoverResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.verify.TwoFAVerifyResponseModel
import com.payfunds.wallet.network.response_model.update_card_3ds.UpdateCard3dsForwardingResponseModal
import com.payfunds.wallet.network.response_model.update_card_lable.UpdateCardLabelResponseModal
import com.payfunds.wallet.network.response_model.update_card_pin.UpdateCardPinResponseModal
import com.payfunds.wallet.network.response_model.upload_kyc.UploadKYCResponseModal
import com.payfunds.wallet.network.response_model.withdraw.WithdrawResponseModal
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface PayFundApiService {

    @POST("alert/create")
    suspend fun alertCreate(
        @Body alertCreateRequestModel: CreateAlertRequestModel
    ): Response<CreateAlertResponseModel>

    @POST("alert/list")
    suspend fun alertList(
        @Query("page") page: String? = null,
        @Query("limit") limit: String? = null,
        @Body alertListRequestModel: ListAlertRequestModel,
    ): Response<ListAlertResponseModel>

    @POST("alert/cancel")
    suspend fun alertCancel(
        @Body alertCancelRequestModel: CancelAlertRequestModel
    ): Response<CancelAlertResponseModel>

    @POST("account/fcm/register")
    suspend fun fcmRegister(
        @Body fcmRegisterRequestModel: FCMRegisterRequestModel
    ): Response<FCMRegisterResponseModel>


    @POST("account/fcm/remove")
    suspend fun removeFcmToken(
        @Body removeFcmRequestModel: RemoveFcmRequestModel,
    ): Response<RemoveFcmTokenResponseModel>

    @POST("user")
    suspend fun createUser(
        @Body createUserRequestModel: CreateUserRequestModel
    ): Response<CreateUserResponseModel>

    @POST("user/addToken")
    suspend fun addToken(
        @Header("Authorization") token: String,
        @Body addTokenToWallet: AddTokenToWallet
    ): Response<CreateUserResponseModel>

    //2FA
    @POST("user/verify-multi-factor")
    suspend fun twoFactorAuthVerify(
        @Body twoFAVerifyRequestModel: TwoFAVerifyRequestModel
    ): Response<TwoFAVerifyResponseModel>

    @POST("user/enable-multi-factor")
    suspend fun twoFactorAuthEnable(
        @Header("Authorization") token: String,
        @Body twoFAEnableRequestModel: TwoFAEnableRequestModel
    ): Response<TwoFAEnableResponseModel>

    @POST("user/recover-multi-factor")
    suspend fun twoFactorAuthRecover(
        @Body twoFARecoverRequestModel: TwoFARecoverRequestModel
    ): Response<TwoFARecoverResponseModel>

    @POST("user/disable-multi-factor")
    suspend fun twoFactorAuthDisable(
        @Header("Authorization") token: String,
        @Body twoFADisableRequestModel: TwoFADisableRequestModel
    ): Response<TwoFADisableResponseModel>

    @POST("user/multi-factor-wallet-addresses")
    suspend fun twoFactorAuthWalletsCheck(
        @Body twoFactorAuthCheckWalletAddressRequestModel: TwoFactorAuthCheckWalletAddressRequestModel
    ): Response<TwoFactorAuthCheckWalletAddressResponseModel>

    @GET("notification/get-all")
    suspend fun getAllNotifications(
        @Header("Authorization") token: String,
        @Query("startIndex") startIndex: Int? = null,
        @Query("itemsPerPage") itemsPerPage: Int? = null,
    ): Response<GetAllNotificationResponseModel>


    @POST("notification/mark-as-read")
    suspend fun notificationMarkAsRead(
        @Header("Authorization") token: String,
        @Body notificationMarkAsReadRequestModel: NotificationMarkAsReadRequestModel
    ): Response<NotificationMarkAsReadResponseModel>

    @POST("user/generate-qr")
    suspend fun twoFactorAuthGenerateQR(
        @Header("Authorization") token: String
    ): Response<TwoFAQRCodeResponseModel>

    @POST("walletTransaction/create")
    suspend fun sendTransactionDetails(
        @Header("Authorization") token: String,
        @Body sendTransactionDetailsRequestModel: SendTransactionDetailsRequestModel
    ): Response<SendTransactionDetailsResponseModel>


    // Holobank User
    @POST("holobank/user")
    suspend fun createCoreUser(
        @Header("Authorization") token: String,
        @Body createCoreUserRequestModal: CreateCoreUserRequestModal
    ): Response<CreateCoreUserResponseModal>

    @GET("holobank/user")
    suspend fun getUserDetails(
        @Header("Authorization") token: String,
    ): Response<GetUserDetailsResponseModal>

    @Multipart
    @POST("holobank/kyc")
    suspend fun uploadKYC(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("nationality") nationality: RequestBody,
        @Part("occupation") occupation: RequestBody,
        @Part("dateOfBirth") dateOfBirth: RequestBody,
        @Part("placeOfBirth") placeOfBirth: RequestBody,
        @Part("country") country: RequestBody,
        @Part("address") address: RequestBody,
        @Part("district") district: RequestBody,
        @Part("city") city: RequestBody,
        @Part("postalCode") postalCode: RequestBody,
        @Part("isSameResidentialAddress") isSameResidentialAddress: RequestBody,
        @Part("personalIdentificationNumber") personalIdentificationNumber: RequestBody,
        @Part passportImage: MultipartBody.Part,
        @Part nationalIdImage: MultipartBody.Part,
        @Part passportSelfie: MultipartBody.Part,
        @Part nationalIdSelfieImage: MultipartBody.Part,
        @Part digitalSignature: MultipartBody.Part,
    ): Response<UploadKYCResponseModal>

    // Holobank Card
    @POST("holobank/card")
    suspend fun requestCard(
        @Header("Authorization") token: String,
        @Body requestCardRequestModal: RequestCardRequestModal,
    ): Response<RequestCardResponseModal>

    @GET("holobank/card")
    suspend fun getCardDetails(
        @Header("Authorization") token: String,
    ): Response<GetCardDetailsResponseModal>

    @PUT("holobank/card/pin")
    suspend fun updateCardPin(
        @Header("Authorization") token: String,
        @Body updateCardPinRequestModal: UpdateCardPinRequestModal,
    ): Response<UpdateCardPinResponseModal>

    @PUT("holobank/card/freeze")
    suspend fun cardFreeze(
        @Header("Authorization") token: String,
        @Body cardFreezeRequestModal: CardFreezeRequestModal,
    ): Response<CardFreezeResponeModal>

    @GET("holobank/card/3ds-forwarding")
    suspend fun getCard3dsForwarding(
        @Header("Authorization") token: String,
    ): Response<GetCard3dsForwardingResponseModal>

    @PUT("holobank/card/3ds-forwarding")
    suspend fun updateCard3dsForwarding(
        @Header("Authorization") token: String,
        @Body updateCard3dsForwardingRequestModal: UpdateCard3dsForwardingRequestModal,
    ): Response<UpdateCard3dsForwardingResponseModal>

    @DELETE("holobank/card")
    suspend fun deleteCard(
        @Header("Authorization") token: String,
    ): Response<DeleteCardResponseModal>

    @GET("holobank/transactions")
    suspend fun getTransaction(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<GetCardTransactionsResponseModal>

    @POST("holobank/withdraw")
    suspend fun withdraw(
        @Header("Authorization") token: String,
        @Body withdrawRequestModal: WithdrawRequestModal
    ): Response<WithdrawResponseModal>

    @PATCH("holobank/card/label")
    suspend fun updateCardLabel(
        @Header("Authorization") token: String,
        @Body updateCardLabelRequestModal: UpdateCardLabelRequestModal
    ): Response<UpdateCardLabelResponseModal>

    @GET("holobank/card/html")
    suspend fun getCardHtml(
        @Header("Authorization") token: String,
    ): Response<GetCardInfoResponseModal>

    @GET("holobank/reap-account/balance")
    suspend fun getAccountBalance(
        @Header("Authorization") token: String,
    ): Response<GetAccountBalanceResponseModal>

    @GET("holobank/deposit")
    suspend fun depositInfo(
        @Header("Authorization") token: String,
        @Query("type") type: String
    ): Response<DepositInfoResponseModal>

}