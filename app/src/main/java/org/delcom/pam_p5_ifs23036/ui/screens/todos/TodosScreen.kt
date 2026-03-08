package org.delcom.pam_p5_ifs23036.ui.screens.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
fun TodosScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var authToken by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    
    var currentPage by remember { mutableStateOf(1) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }
    val allLoadedTodos = remember { mutableStateListOf<ResponseTodoData>() }
    var isLastPage by remember { mutableStateOf(false) }

    LaunchedEffect(uiStateAuth.auth) {
        if (uiStateAuth.auth is AuthUIState.Success) {
            authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
            currentPage = 1
            allLoadedTodos.clear()
            isLastPage = false
            todoViewModel.getAllTodos(authToken!!, currentPage, 10, selectedStatus, selectedUrgency)
        }
    }

    LaunchedEffect(uiStateTodo.todos) {
        if (uiStateTodo.todos is TodosUIState.Success) {
            val newTodos = (uiStateTodo.todos as TodosUIState.Success).data
            if (currentPage == 1) allLoadedTodos.clear()
            newTodos.forEach { newTodo ->
                if (allLoadedTodos.none { it.id == newTodo.id }) allLoadedTodos.add(newTodo)
            }
            if (newTodos.isEmpty() || newTodos.size < 10) isLastPage = true
        }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= allLoadedTodos.size - 2 && !isLastPage && uiStateTodo.todos !is TodosUIState.Loading
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && authToken != null && !isLastPage) {
            currentPage++
            todoViewModel.getAllTodos(authToken!!, currentPage, 10, selectedStatus, selectedUrgency)
        }
    }

    fun resetAndFetch() {
        currentPage = 1
        allLoadedTodos.clear()
        isLastPage = false
        authToken?.let { todoViewModel.getAllTodos(it, currentPage, 10, selectedStatus, selectedUrgency) }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                navController = navController,
                title = "Tugas Saya",
                showBackButton = false,
                withSearch = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchAction = { resetAndFetch() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.TodosAdd.path) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Tambah Tugas")
            }
        },
        bottomBar = { BottomNavComponent(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Filter Section
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Status Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null; resetAndFetch() },
                        label = { Text("Semua Status") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedStatus == "done",
                        onClick = { selectedStatus = "done"; resetAndFetch() },
                        label = { Text("Selesai") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedStatus == "pending",
                        onClick = { selectedStatus = "pending"; resetAndFetch() },
                        label = { Text("Tertunda") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Urgency Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    
                    FilterChip(
                        selected = selectedUrgency == null,
                        onClick = { selectedUrgency = null; resetAndFetch() },
                        label = { Text("Semua Prioritas") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedUrgency == "Low",
                        onClick = { selectedUrgency = "Low"; resetAndFetch() },
                        label = { Text("Rendah") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedUrgency == "Medium",
                        onClick = { selectedUrgency = "Medium"; resetAndFetch() },
                        label = { Text("Sedang") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = selectedUrgency == "High",
                        onClick = { selectedUrgency = "High"; resetAndFetch() },
                        label = { Text("Tinggi") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (allLoadedTodos.isEmpty() && uiStateTodo.todos !is TodosUIState.Loading) {
                    EmptyStateUI()
                } else {
                    LazyColumn(
                        state = listState, 
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allLoadedTodos, key = { it.id }) { todo ->
                            TodoItemModernUI(todo) { 
                                navController.navigate(ConstHelper.RouteNames.TodosDetail.path.replace("{todoId}", todo.id))
                            }
                        }
                        if (uiStateTodo.todos is TodosUIState.Loading) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateUI() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search, 
                contentDescription = null, 
                modifier = Modifier.size(64.dp).alpha(0.3f),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tidak ada tugas ditemukan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Coba ubah filter atau tambah tugas baru", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun TodoItemModernUI(todo: ResponseTodoData, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                contentDescription = null,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title ?: "Tanpa Judul", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    todo.description ?: "Tidak ada deskripsi", 
                    style = MaterialTheme.typography.bodySmall, 
                    maxLines = 2, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (urgencyColor, urgencyLabel) = when(todo.urgency?.lowercase()) {
                        "high" -> UrgencyHigh to "Prioritas Tinggi"
                        "medium" -> UrgencyMedium to "Sedang"
                        else -> UrgencyLow to "Rendah"
                    }
                    
                    Box(modifier = Modifier.size(8.dp).background(urgencyColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(urgencyLabel, style = MaterialTheme.typography.labelSmall, color = urgencyColor, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Surface(
                color = if (todo.isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape,
                modifier = Modifier.size(32.dp).border(
                    width = 1.dp, 
                    color = if (todo.isDone) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = CircleShape
                )
            ) {
                if (todo.isDone) {
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = null, 
                        modifier = Modifier.padding(6.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
