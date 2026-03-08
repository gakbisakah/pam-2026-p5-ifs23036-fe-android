package org.delcom.pam_p5_ifs23036.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23036.R
import org.delcom.pam_p5_ifs23036.helper.ConstHelper
import org.delcom.pam_p5_ifs23036.helper.RouteHelper
import org.delcom.pam_p5_ifs23036.helper.ToolsHelper
import org.delcom.pam_p5_ifs23036.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23036.ui.components.*
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyHigh
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyLow
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyMedium
import org.delcom.pam_p5_ifs23036.ui.viewmodels.*

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isInitialLoading by remember { mutableStateOf(true) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.loadTokenFromPreferences()
    }

    LaunchedEffect(uiStateAuth.auth) {
        when (val authState = uiStateAuth.auth) {
            is AuthUIState.Success -> {
                authToken = authState.data.authToken
                isInitialLoading = false
                todoViewModel.getTodoStats(authState.data.authToken)
                todoViewModel.getAllTodos(authState.data.authToken)
                todoViewModel.getProfile(authState.data.authToken)
            }
            is AuthUIState.Error -> {
                isInitialLoading = false
                RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
            }
            is AuthUIState.Idle -> {
                isInitialLoading = false
                RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
            }
            is AuthUIState.Loading -> {}
        }
    }

    fun onLogout(token: String){
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout is AuthLogoutUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isInitialLoading || uiStateAuth.auth is AuthUIState.Loading) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem("Profil", Icons.Filled.Person, ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem("Keluar", Icons.AutoMirrored.Filled.Logout, null, onClick = { onLogout(authToken ?: "") }, isDestructive = true)
    )

    Scaffold(
        topBar = { TopAppBarComponent(navController, "Beranda", false, customMenuItems = menuItems) },
        bottomBar = { BottomNavComponent(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            HomeUI(uiStateTodo.stats, uiStateTodo.todos, uiStateTodo.profile, navController)
        }
    }
}

@Composable
fun HomeUI(
    statsState: TodoStatsUIState, 
    todosState: TodosUIState,
    profileState: ProfileUIState,
    navController: NavHostController
) {
    val userName = if (profileState is ProfileUIState.Success) profileState.data.name else "Pengguna"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Header with Gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "Selamat datang,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$userName!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kelola harimu dengan produktif",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).alpha(0.2f),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Statistics Section
        Text(
            "Statistik Singkat", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        when (statsState) {
            is TodoStatsUIState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TodoStatsUIState.Error -> Text("Gagal memuat statistik", color = MaterialTheme.colorScheme.error)
            is TodoStatsUIState.Success -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusCardCompact(modifier = Modifier.weight(1f), title = "Total", value = (statsState.data.total ?: 0).toString(), icon = Icons.AutoMirrored.Filled.List, bgColor = MaterialTheme.colorScheme.primaryContainer)
                    StatusCardCompact(modifier = Modifier.weight(1f), title = "Selesai", value = (statsState.data.completed ?: 0).toString(), icon = Icons.Default.CheckCircle, bgColor = Color(0xFFE8F5E9))
                    StatusCardCompact(modifier = Modifier.weight(1f), title = "Tertunda", value = (statsState.data.pending ?: 0).toString(), icon = Icons.Default.Schedule, bgColor = Color(0xFFFFF3E0))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tugas Terbaru", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.Todos.path) }) {
                Text("Lihat Semua")
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        when (todosState) {
            is TodosUIState.Loading -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TodosUIState.Error -> Text("Gagal memuat tugas", color = MaterialTheme.colorScheme.error)
            is TodosUIState.Success -> {
                if (todosState.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(48.dp).alpha(0.3f), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada tugas", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    todosState.data.take(5).forEach { todo ->
                        TodoHomeItemUI(todo) { 
                            navController.navigate(ConstHelper.RouteNames.TodosDetail.path.replace("{todoId}", todo.id))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatusCardCompact(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, bgColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TodoHomeItemUI(todo: ResponseTodoData, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onOpen() },
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                contentDescription = null,
                placeholder = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title ?: "Tanpa Judul", 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    todo.description ?: "Tidak ada deskripsi", 
                    style = MaterialTheme.typography.bodySmall, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val (urgencyColor, urgencyLabel) = when(todo.urgency?.lowercase()) {
                "high" -> UrgencyHigh to "Tinggi"
                "medium" -> UrgencyMedium to "Sedang"
                else -> UrgencyLow to "Rendah"
            }
            
            Surface(
                color = urgencyColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = urgencyLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
