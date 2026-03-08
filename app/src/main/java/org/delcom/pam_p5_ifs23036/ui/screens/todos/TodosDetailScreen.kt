package org.delcom.pam_p5_ifs23036.ui.screens.todos

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PhotoCamera
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
import org.delcom.pam_p5_ifs23036.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23036.ui.components.BottomDialog
import org.delcom.pam_p5_ifs23036.ui.components.BottomDialogType
import org.delcom.pam_p5_ifs23036.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23036.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23036.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23036.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyHigh
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyLow
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyMedium
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoViewModel

@Composable
fun TodosDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    todoId: String
) {
    val uiStateTodo by todoViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isConfirmDelete by remember { mutableStateOf(false) }
    var todo by remember { mutableStateOf<ResponseTodoData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        todoViewModel.resetActionStates()
        todoViewModel.getTodoById(authToken!!, todoId)
    }

    LaunchedEffect(uiStateTodo.todo) {
        if (uiStateTodo.todo is TodoUIState.Success) {
            todo = (uiStateTodo.todo as TodoUIState.Success).data
            isLoading = false
        } else if (uiStateTodo.todo is TodoUIState.Error) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, (uiStateTodo.todo as TodoUIState.Error).message)
            isLoading = false
        }
    }

    LaunchedEffect(uiStateTodo.todoDelete) {
        if (uiStateTodo.todoDelete is TodoActionUIState.Success) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, (uiStateTodo.todoDelete as TodoActionUIState.Success).message)
            RouteHelper.to(navController, ConstHelper.RouteNames.Todos.path, true)
            isLoading = false
        } else if (uiStateTodo.todoDelete is TodoActionUIState.Error) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, (uiStateTodo.todoDelete as TodoActionUIState.Error).message)
            isLoading = false
        }
    }

    LaunchedEffect(uiStateTodo.todoChangeCover) {
        if (uiStateTodo.todoChangeCover is TodoActionUIState.Success) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, (uiStateTodo.todoChangeCover as TodoActionUIState.Success).message)
            authToken?.let { todoViewModel.getTodoById(it, todoId) }
            isLoading = false
        } else if (uiStateTodo.todoChangeCover is TodoActionUIState.Error) {
            SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, (uiStateTodo.todoChangeCover as TodoActionUIState.Error).message)
            isLoading = false
        }
    }

    val detailMenuItems = listOf(
        TopAppBarMenuItem("Ubah Tugas", Icons.Filled.Edit, null, onClick = {
            RouteHelper.to(navController, ConstHelper.RouteNames.TodosEdit.path.replace("{todoId}", todo?.id ?: ""))
        }),
        TopAppBarMenuItem("Hapus Tugas", Icons.Filled.Delete, null, onClick = { isConfirmDelete = true }, isDestructive = true),
    )

    Scaffold(
        topBar = { TopAppBarComponent(navController, "Detail Tugas", true, customMenuItems = detailMenuItems) },
        bottomBar = { BottomNavComponent(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (todo != null) {
                TodosDetailUI(
                    todo = todo!!,
                    onChangeCover = { context, uri ->
                        isLoading = true
                        val filePart = uriToMultipart(context, uri, "file")
                        todoViewModel.putTodoCover(authToken!!, todoId, filePart)
                    },
                )
            }
            
            BottomDialog(
                type = BottomDialogType.ERROR,
                show = isConfirmDelete,
                onDismiss = { isConfirmDelete = false },
                title = "Hapus Tugas?",
                message = "Tindakan ini tidak dapat dibatalkan. Apakah Anda yakin?",
                confirmText = "Hapus",
                onConfirm = {
                    isLoading = true
                    todoViewModel.deleteTodo(authToken!!, todoId)
                },
                cancelText = "Batal",
                destructiveAction = true
            )
            
            if (isLoading || todo == null) LoadingUI()
        }
    }
}

@Composable
fun TodosDetailUI(
    todo: ResponseTodoData,
    onChangeCover: (context: Context, file: Uri) -> Unit,
) {
    var dataFile by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        dataFile = uri
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Image Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = dataFile ?: ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                contentDescription = null,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 400f
                        )
                    )
            )
            
            // Change Photo Button
            SmallFloatingActionButton(
                onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
            }
            
            if (dataFile != null) {
                Button(
                    onClick = { onChangeCover(context, dataFile!!); dataFile = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Gambar")
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            // Status and Urgency Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (urgencyColor, urgencyLabel) = when(todo.urgency?.lowercase()) {
                    "high" -> UrgencyHigh to "Prioritas Tinggi"
                    "medium" -> UrgencyMedium to "Sedang"
                    else -> UrgencyLow to "Rendah"
                }
                
                Surface(
                    color = urgencyColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(urgencyColor, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(urgencyLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Surface(
                    color = if (todo.isDone) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (todo.isDone) "Selesai" else "Dalam Proses",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (todo.isDone) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = todo.title ?: "Tanpa Judul", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoItem(Modifier.weight(1f), Icons.Default.CalendarToday, "Dibuat pada", ToolsHelper.formatDateTime(todo.createdAt))
                InfoItem(Modifier.weight(1f), Icons.Default.CheckCircle, "Status", if (todo.isDone) "Selesai" else "Tertunda")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = todo.description ?: "Tidak ada deskripsi untuk tugas ini.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun InfoItem(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
