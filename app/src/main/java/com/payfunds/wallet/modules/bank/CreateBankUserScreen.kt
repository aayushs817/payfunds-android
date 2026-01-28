package com.payfunds.wallet.modules.bank

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.ui.Alignment
import android.net.Uri
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.hbb20.CCPCountry // Import from the library
import com.payfunds.wallet.R
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.bank.data.CountryModal
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.*
import com.payfunds.wallet.ui.compose.components.subhead1_grey
import com.payfunds.wallet.ui.compose.components.title3_leah
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import io.payfunds.core.helpers.HudHelper
import androidx.compose.ui.platform.LocalView


@Composable
fun FormsUploadItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ComposeAppTheme.colors.steel20, RoundedCornerShape(12.dp))
            .background(ComposeAppTheme.colors.lawrence)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        body_leah(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_medium2_up_24),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey,
            modifier = Modifier.size(20.dp)
        )
    }
}

fun getCountriesFromLibrary(context: Context): List<CountryModal> {
    val libraryList = CCPCountry.getLibraryMasterCountryList(context, null)

    return libraryList.map { ccpCountry ->
        CountryModal(
            name = ccpCountry.name,
            code = "+${ccpCountry.phoneCode}",
            flagRes = ccpCountry.flagID,
            isoCode = ccpCountry.nameCode // Extract the unique ISO code (e.g. "US", "IN")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CreateCardScreen(
    navController: NavController,
    initialStep: Int = 1,
    viewModel: CreateBankUserViewModel = viewModel(factory = CreateBankUserModule.Factory())
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(initialStep) }

    val uiState = viewModel.uiState

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var selectedVerificationType by remember { mutableStateOf<VerificationType?>(VerificationType.KYC) }

    // Step 3 State
    var nationality by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var idDocumentType by remember { mutableStateOf("") }
    var idDocumentNumber by remember { mutableStateOf("") }
    var idDocumentIssueDate by remember { mutableStateOf("") }
    var idDocumentExpiryDate by remember { mutableStateOf("") }
    var idDocumentFrontUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var idDocumentBackUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var passportSelfieUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var nationalIdSelfieUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var digitalSignatureUri by remember { mutableStateOf<android.net.Uri?>(null) }

    var title by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var placeOfBirth by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var employmentStatus by remember { mutableStateOf("") }
    var sourceOfFunds by remember { mutableStateOf("") }
    var personalIdentificationNumber by remember { mutableStateOf("") }
    var isSameResidentialAddress by remember { mutableStateOf(true) }
    val view = LocalView.current

    LaunchedEffect(uiState) {
        if (uiState is ViewState.Error) {
            HudHelper.showErrorMessage(view, uiState.t.message ?: "An error occurred")
        }
    }

    // Auto-transition to Step 2 when user is correctly created or identified
    LaunchedEffect(viewModel.createCoreUserResponse) {
        if (viewModel.createCoreUserResponse?.success == true && currentStep == 1) {
            Log.d("KYC_DEBUG", "Auto-transitioning to Step 2: User successfully identified/created")
            currentStep = 2
        }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }

    val idFrontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { idDocumentFrontUri = it }
    val idBackLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { idDocumentBackUri = it }
    val passportSelfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { passportSelfieUri = it }
    val nationalIdSelfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { nationalIdSelfieUri = it }
    val signatureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { digitalSignatureUri = it }

    fun uriToFile(uri: Uri?, context: Context): File? {
        if (uri == null) return null
        val file = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    var showGenderSelector by remember { mutableStateOf(false) }
    var showIdTypeSelector by remember { mutableStateOf(false) }
    var showEmploymentStatusSelector by remember { mutableStateOf(false) }
    var showOccupationSelector by remember { mutableStateOf(false) }
    var showTitleSelector by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Other")
    val idTypeOptions = listOf("Passport", "Identity Card", "Driver's License")
    val employmentStatusOptions = listOf("Employed", "Unemployed", "Self-Employed", "Student", "Retired")
    val occupationOptions = listOf("Employee", "Public Staff", "Staff", "General Customer", "Special Customer", "Agent")
    val titleOptions = listOf("Mr.", "Mrs.", "Dr.")

    var showDatePickerForIssueDate by remember { mutableStateOf(false) }
    var showDatePickerForExpiryDate by remember { mutableStateOf(false) }
    var showDatePickerForDOB by remember { mutableStateOf(false) }

    if (showDatePickerForIssueDate || showDatePickerForExpiryDate || showDatePickerForDOB) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerForIssueDate = false
                showDatePickerForExpiryDate = false
                showDatePickerForDOB = false
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val formattedDate = sdf.format(Date(it))
                        if (showDatePickerForIssueDate) idDocumentIssueDate = formattedDate
                        if (showDatePickerForExpiryDate) idDocumentExpiryDate = formattedDate
                        if (showDatePickerForDOB) dateOfBirth = formattedDate
                    }
                    showDatePickerForIssueDate = false
                    showDatePickerForExpiryDate = false
                    showDatePickerForDOB = false
                }) {
                    Text("OK", color = ComposeAppTheme.colors.jacob)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePickerForIssueDate = false
                    showDatePickerForExpiryDate = false
                    showDatePickerForDOB = false
                }) {
                    Text("Cancel", color = ComposeAppTheme.colors.jacob)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Load countries
    val allCountries = remember(context) {
        getCountriesFromLibrary(context)
    }

    // STATE: Default to USA using ISO code "US"
    var selectedCountry by remember {
        mutableStateOf(
            allCountries.firstOrNull { it.isoCode.equals("US", ignoreCase = true) }
                ?: allCountries.firstOrNull() ?: CountryModal(
                    "United States",
                    "+1",
                    R.drawable.icon_32_flag_usa,
                    "US"
                )
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        sheetBackgroundColor = ComposeAppTheme.colors.transparent,
        sheetContent = {
            CountrySelectorBottomSheet(
                countryModalList = allCountries,
                onCountrySelected = {
                    selectedCountry = it
                    coroutineScope.launch { sheetState.hide() }
                },
                onClose = {
                    coroutineScope.launch { sheetState.hide() }
                }
            )
        }
    ) {
        if (showSuccessDialog) {
            SuccessDialog(
                onDismiss = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }
            )
        }

        if (showGenderSelector) {
            SelectorDialogCompose(
                title = "Select Gender",
                items = genderOptions.map { SelectorItem(it, it == gender, it) },
                onDismissRequest = { showGenderSelector = false },
                onSelectItem = { gender = it }
            )
        }

        if (showIdTypeSelector) {
            SelectorDialogCompose(
                title = "Select ID Document Type",
                items = idTypeOptions.map { SelectorItem(it, it == idDocumentType, it) },
                onDismissRequest = { showIdTypeSelector = false },
                onSelectItem = { idDocumentType = it }
            )
        }

        if (showEmploymentStatusSelector) {
            SelectorDialogCompose(
                title = "Select Employment Status",
                items = employmentStatusOptions.map { SelectorItem(it, it == employmentStatus, it) },
                onDismissRequest = { showEmploymentStatusSelector = false },
                onSelectItem = { employmentStatus = it }
            )
        }

        if (showOccupationSelector) {
            SelectorDialogCompose(
                title = "Select Occupation",
                items = occupationOptions.map { SelectorItem(it, it == occupation, it) },
                onDismissRequest = { showOccupationSelector = false },
                onSelectItem = { occupation = it }
            )
        }

        if (showTitleSelector) {
            SelectorDialogCompose(
                title = "Select Title",
                items = titleOptions.map { SelectorItem(it, it == title, it) },
                onDismissRequest = { showTitleSelector = false },
                onSelectItem = { title = it }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .background(ComposeAppTheme.colors.tyler)
        ) {
            // AppBar

            AppBar(
                title = stringResource(R.string.Bank_KycVerification),
                navigationIcon = {
                    HsBackButton(onClick = {
                        if (currentStep > 1) currentStep-- else navController.popBackStack()
                    })
                }
            )

            // Progress Bar
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ComposeAppTheme.colors.steel20)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            when (currentStep) {
                                1 -> 0.5f
                                2 -> 1f
                                else -> 1f
                            }
                        )
                        .fillMaxHeight()
                        .background(ComposeAppTheme.colors.jacob)
                )
            }

            when (currentStep) {
                1 -> {
                    // Step 1: Information Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        VSpacer(24.dp)

                        subhead1_grey(text = "First Name")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your first name",
                            pasteEnabled = false,
                            onValueChange = { firstName = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Last Name")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your last name",
                            pasteEnabled = false,
                            onValueChange = { lastName = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Display Name")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter display name",
                            pasteEnabled = false,
                            onValueChange = { displayName = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Email")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your email",
                            pasteEnabled = false,
                            onValueChange = { email = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Phone Number")
                        VSpacer(8.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Country Code Selector
                            Row(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        ComposeAppTheme.colors.steel20,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(ComposeAppTheme.colors.lawrence)
                                    .padding(horizontal = 12.dp)
                                    .clickable { coroutineScope.launch { sheetState.show() } },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = selectedCountry.flagRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedCountry.code,
                                    style = ComposeAppTheme.typography.body,
                                    color = ComposeAppTheme.colors.leah
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = null,
                                    tint = ComposeAppTheme.colors.grey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                FormsInput(
                                    hint = "Enter phone number",
                                    pasteEnabled = false,
                                    onValueChange = { phoneNumber = it }
                                )
                            }
                        }

                        VSpacer(32.dp)
                    }


                    ButtonPrimaryRed(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        title = stringResource(R.string.Button_Continue),
                        loadingIndicator = uiState is ViewState.Loading,
                        enabled = uiState !is ViewState.Loading,
                        onClick = {
                            viewModel.createCoreUser(firstName, lastName, displayName, email, selectedCountry.code + phoneNumber) {
                            }
                        }
                    )
                    VSpacer(16.dp)
                }



                2 -> {
                    // Step 2: Verification Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        VSpacer(32.dp)
                        headline1_leah(text = if (selectedVerificationType == VerificationType.KYB) "KYB Verification" else "KYC Verification")
                        VSpacer(24.dp)

                        VSpacer(16.dp)
                        subhead1_grey(text = "Title")
                        VSpacer(8.dp)
                        FormsInputSearch(
                            enabled = false,
                            hint = title.ifEmpty { "Select Title" },
                            hintColor = if (title.isEmpty()) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.leah,
                            onValueChange = {},
                            endContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = null,
                                    tint = ComposeAppTheme.colors.grey,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.clickable { showTitleSelector = true }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Nationality")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your nationality",
                            pasteEnabled = false,
                            onValueChange = { nationality = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Occupation")
                        VSpacer(8.dp)
                        FormsInputSearch(
                            enabled = false,
                            hint = occupation.ifEmpty { "Select Occupation" },
                            hintColor = if (occupation.isEmpty()) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.leah,
                            onValueChange = {},
                            endContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = null,
                                    tint = ComposeAppTheme.colors.grey,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.clickable { showOccupationSelector = true }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Personal Identification Number")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter identification number",
                            pasteEnabled = false,
                            onValueChange = { personalIdentificationNumber = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Place of Birth")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your place of birth",
                            pasteEnabled = false,
                            onValueChange = { placeOfBirth = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Residential Address")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your residential address",
                            pasteEnabled = false,
                            onValueChange = { address = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "District")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your district",
                            pasteEnabled = false,
                            onValueChange = { district = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "City")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your city",
                            pasteEnabled = false,
                            onValueChange = { city = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Postal Code")
                        VSpacer(8.dp)
                        FormsInput(
                            hint = "Enter your postal code",
                            pasteEnabled = false,
                            onValueChange = { postalCode = it }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Date of Birth")
                        VSpacer(8.dp)
                        FormsInputSearch(
                            enabled = false,
                            hint = dateOfBirth.ifEmpty { "Select Date of Birth" },
                            hintColor = if (dateOfBirth.isEmpty()) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.leah,
                            onValueChange = {},
                            endContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_calender),
                                    contentDescription = null,
                                    tint = ComposeAppTheme.colors.grey,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.clickable { showDatePickerForDOB = true }
                        )

                        VSpacer(24.dp)
                        headline1_leah(text = "Identity Documents")
                        VSpacer(16.dp)

                        subhead1_grey(text = "Passport Image")
                        VSpacer(8.dp)
                        FormsUploadItem(
                            text = idDocumentFrontUri?.path?.split("/")?.lastOrNull() ?: "Tap to upload Passport Image",
                            onClick = { idFrontLauncher.launch("image/*") }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "National ID Image")
                        VSpacer(8.dp)
                        FormsUploadItem(
                            text = idDocumentBackUri?.path?.split("/")?.lastOrNull() ?: "Tap to upload National ID Image",
                            onClick = { idBackLauncher.launch("image/*") }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Passport Selfie")
                        VSpacer(8.dp)
                        FormsUploadItem(
                            text = passportSelfieUri?.path?.split("/")?.lastOrNull() ?: "Tap to upload Passport Selfie",
                            onClick = { passportSelfieLauncher.launch("image/*") }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "National ID Selfie")
                        VSpacer(8.dp)
                        FormsUploadItem(
                            text = nationalIdSelfieUri?.path?.split("/")?.lastOrNull() ?: "Tap to upload National ID Selfie",
                            onClick = { nationalIdSelfieLauncher.launch("image/*") }
                        )

                        VSpacer(16.dp)
                        subhead1_grey(text = "Digital Signature")
                        VSpacer(8.dp)
                        FormsUploadItem(
                            text = digitalSignatureUri?.path?.split("/")?.lastOrNull() ?: "Tap to upload Digital Signature",
                            onClick = { signatureLauncher.launch("image/*") }
                        )

                        VSpacer(32.dp)

                        ButtonPrimaryRed(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            title = if (selectedVerificationType == VerificationType.KYB) "Submit KYB" else "Submit KYC",
                            loadingIndicator = uiState is ViewState.Loading,
                            enabled = uiState !is ViewState.Loading,
                            onClick = {
                                Log.d("KYC_DEBUG", "Submit Button Clicked - Type: ${selectedVerificationType}")
                                if (selectedVerificationType == VerificationType.KYC) {
                                    Log.d("KYC_DEBUG", "Submitting KYC with fields: title=$title, firstName=$firstName, lastName=$lastName")
                                    viewModel.uploadKYC(
                                        title = title,
                                        nationality = nationality,
                                        occupation = occupation,
                                        dateOfBirth = dateOfBirth,
                                        placeOfBirth = placeOfBirth,
                                        country = selectedCountry.isoCode,
                                        address = address,
                                        district = district,
                                        city = city,
                                        postalCode = postalCode,
                                        isSameResidentialAddress = isSameResidentialAddress,
                                        personalIdentificationNumber = personalIdentificationNumber.toIntOrNull() ?: 0,
                                        passportImageFile = uriToFile(idDocumentFrontUri, context),
                                        nationalIdImageFile = uriToFile(idDocumentBackUri, context),
                                        passportSelfieFile = uriToFile(passportSelfieUri, context),
                                        nationalIdSelfieImageFile = uriToFile(nationalIdSelfieUri, context),
                                        digitalSignatureFile = uriToFile(digitalSignatureUri, context)
                                    ) {
                                        showSuccessDialog = true
                                    }
                                } else {
                                    // Handle KYB if needed
                                    HudHelper.showErrorMessage(view, "KYB submission not implemented yet")
                                }
                            }
                        )
                        VSpacer(16.dp)
                    }
                }
            }
        }
    }
}




enum class VerificationType {
    KYC, KYB
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(ComposeAppTheme.colors.lawrence)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(ComposeAppTheme.colors.jacob),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.checkbox_active_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            VSpacer(24.dp)
            title3_leah(
                text = "Success",
                textAlign = TextAlign.Center
            )
            VSpacer(8.dp)
            subhead2_grey(
                text = "Your verification request has been submitted successfully.",
                textAlign = TextAlign.Center
            )
            VSpacer(32.dp)
            ButtonPrimaryRed(
                modifier = Modifier.fillMaxWidth(),
                title = "Done",
                onClick = onDismiss
            )
        }
    }
}
