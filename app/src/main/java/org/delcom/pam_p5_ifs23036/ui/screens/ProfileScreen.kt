package org.delcom.pam_p5_ifs23036.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23036.R
import org.delcom.pam_p5_ifs23036.helper.ConstHelper
import org.delcom.pam_p5_ifs23036.helper.RouteHelper
import org.delcom.pam_p5_ifs23036.helper.SuspendHelper
import org.delcom.pam_p5_ifs23036.helper.SuspendHelper.SnackBarType
import org.delcom.pam_p5_ifs23036.helper.ToolsHelper
import org.delcom.pam_p5_ifs23036.helper.ToolsHelper.uriToMultipart
import org.delcom.pam_p5_ifs23036.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23036.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23036.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23036.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23036.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23036.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isActionLoading by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth is AuthUIState.Success) {
            val token = (uiStateAuth.auth as AuthUIState.Success).data.authToken
            authToken = token
            todoViewModel.resetActionStates()
            todoViewModel.getProfile(token)
        } else {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    LaunchedEffect(uiStateTodo.profileUpdate) {
        when (val state = uiStateTodo.profileUpdate) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                authToken?.let { todoViewModel.getProfile(it) }
                todoViewModel.resetActionStates()
                isActionLoading = false
            }
            is TodoActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, state.message)
                isActionLoading = false
            }
            else -> {}
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout is AuthLogoutUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        } else if (uiStateAuth.authLogout is AuthLogoutUIState.Error) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, (uiStateAuth.authLogout as AuthLogoutUIState.Error).message)
            isActionLoading = false
        }
    }

    val menuItems = listOf(
        TopAppBarMenuItem("Keluar", Icons.AutoMirrored.Filled.Logout, null, onClick = { 
            isActionLoading = true
            authViewModel.logout(authToken ?: "") 
        }, isDestructive = true)
    )

    Scaffold(
        topBar = { TopAppBarComponent(navController, "Profil", false, customMenuItems = menuItems) },
        bottomBar = { BottomNavComponent(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val profileState = uiStateTodo.profile) {
                is ProfileUIState.Loading -> LoadingUI()
                is ProfileUIState.Success -> {
                    ProfileUI(
                        profile = profileState.data,
                        onUpdateInfo = { name, username, about ->
                            isActionLoading = true
                            todoViewModel.putUserMe(authToken!!, name, username, about)
                        },
                        onUpdatePassword = { old, new ->
                            isActionLoading = true
                            todoViewModel.putUserMePassword(authToken!!, old, new)
                        },
                        onUpdatePhoto = { context, uri ->
                            isActionLoading = true
                            val filePart = uriToMultipart(context, uri, "file")
                            todoViewModel.putUserMePhoto(authToken!!, filePart)
                        }
                    )
                }
                is ProfileUIState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(profileState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { authToken?.let { todoViewModel.getProfile(it) } }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            
            if (isActionLoading) LoadingUI()
        }
    }
}

@Composable
fun ProfileUI(
    profile: ResponseUserData,
    onUpdateInfo: (String, String, String?) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdatePhoto: (Context, Uri) -> Unit
){
    val context = LocalContext.current
    var name by remember { mutableStateOf(profile.name) }
    var username by remember { mutableStateOf(profile.username) }
    var about by remember { mutableStateOf(profile.about ?: "") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Modern Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Height increased to accommodate 'about'
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Box {
                    AsyncImage(
                        model = selectedPhotoUri ?: ToolsHelper.getUserImage(profile.id, profile.updatedAt),
                        contentDescription = "Foto Profil",
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        color = MaterialTheme.colorScheme.secondary,
                        tonalElevation = 4.dp
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text("@${profile.username}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                
                if (!profile.about.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        profile.about, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            if (selectedPhotoUri != null) {
                Button(
                    onClick = { 
                        onUpdatePhoto(context, selectedPhotoUri!!)
                        selectedPhotoUri = null
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Foto Baru")
                }
            }

            Text("Informasi Akun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            ModernTextField(value = name, onValueChange = { name = it }, label = "Nama Lengkap", icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            ModernTextField(value = username, onValueChange = { username = it }, label = "Nama Pengguna", icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            ModernTextField(value = about, onValueChange = { about = it }, label = "Tentang", icon = Icons.Default.Person, singleLine = false, minLines = 3)
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onUpdateInfo(name, username, about) }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Perbarui Profil", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Modern Password Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).clickable { showPasswordDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ganti Kata Sandi", fontWeight = FontWeight.Bold)
                        Text("Pastikan kata sandi minimal 8 karakter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Perbarui Kata Sandi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = oldPassword, onValueChange = { oldPassword = it }, label = { Text("Kata Sandi Lama") },
                        modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it }, label = { Text("Kata Sandi Baru") },
                        modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = { if(newPassword.isNotEmpty() && newPassword.length < 8) Text("Minimal 8 karakter", color = MaterialTheme.colorScheme.error) }
                    )
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Konfirmasi Kata Sandi Baru") },
                        modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePassword(oldPassword, newPassword)
                        showPasswordDialog = false
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    },
                    enabled = newPassword.isNotEmpty() && newPassword.length >= 8 && newPassword == confirmPassword
                ) {
                    Text("Perbarui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
