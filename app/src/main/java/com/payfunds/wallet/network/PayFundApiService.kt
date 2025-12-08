package com.payfunds.wallet.network

import com.payfunds.wallet.network.request_model.add_token.AddTokenToWallet
import com.payfunds.wallet.network.request_model.alert.cancel.CancelAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.create.CreateAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.list.ListAlertRequestModel
import com.payfunds.wallet.network.request_model.check_wallet_address.TwoFactorAuthCheckWalletAddressRequestModel
import com.payfunds.wallet.network.request_model.fcm.FCMRegisterRequestModel
import com.payfunds.wallet.network.request_model.notification_mark_as_read.NotificationMarkAsReadRequestModel
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.network.request_model.send_transaction_details.SendTransactionDetailsRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.disable.TwoFADisableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.enable.TwoFAEnableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.recover.TwoFARecoverRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.verify.TwoFAVerifyRequestModel
import com.payfunds.wallet.network.response_model.alert.cancel.CancelAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.create.CreateAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.list.ListAlertResponseModel
import com.payfunds.wallet.network.response_model.fcm.FCMRegisterResponseModel
import com.payfunds.wallet.network.response_model.get_all_notification.GetAllNotificationResponseModel
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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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
}