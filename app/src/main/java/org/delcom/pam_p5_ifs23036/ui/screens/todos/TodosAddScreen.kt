package org.delcom.pam_p5_ifs23036.ui.screens.todos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23036.helper.AlertHelper
import org.delcom.pam_p5_ifs23036.helper.AlertState
import org.delcom.pam_p5_ifs23036.helper.AlertType
import org.delcom.pam_p5_ifs23036.helper.ConstHelper
import org.delcom.pam_p5_ifs23036.helper.RouteHelper
import org.delcom.pam_p5_ifs23036.helper.SuspendHelper
import org.delcom.pam_p5_ifs23036.helper.SuspendHelper.SnackBarType
import org.delcom.pam_p5_ifs23036.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23036.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23036.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23036.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyHigh
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyLow
import org.delcom.pam_p5_ifs23036.ui.theme.UrgencyMedium
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoViewModel

@Composable
fun TodosAddScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var tmpTodo by remember { mutableStateOf<ResponseTodoData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        todoViewModel.resetActionStates()
    }

    fun onSave(title: String, description: String, urgency: String) {
        if(authToken.value == null) return
        isLoading = true
        tmpTodo = ResponseTodoData(title = title, description = description, urgency = urgency)
        todoViewModel.postTodo(authToken = authToken.value!!, title = title, description = description, urgency = urgency)
    }

    LaunchedEffect(uiStateTodo.todoAdd) {
        when (val state = uiStateTodo.todoAdd) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.Todos.path, true)
                isLoading = false
            }
            is TodoActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, state.message)
                isLoading = false
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { TopAppBarComponent(navController, "Tambah Tugas Baru", true) },
        bottomBar = { BottomNavComponent(navController) },
        floatingActionButton = {
            if (!isLoading) {
                ExtendedFloatingActionButton(
                    onClick = { /* Handled by hidden overlay button */ },
                    icon = { Icon(Icons.Default.Save, null) },
                    text = { Text("Simpan Tugas") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TodosAddUI(tmpTodo = tmpTodo, onSave = ::onSave)
            if (isLoading) LoadingUI()
        }
    }
}

@Composable
fun TodosAddUI(
    tmpTodo: ResponseTodoData?,
    onSave: (String, String, String) -> Unit
) {
    val alertState = remember { mutableStateOf(AlertState()) }
    var dataTitle by remember { mutableStateOf(tmpTodo?.title ?: "") }
    var dataDescription by remember { mutableStateOf(tmpTodo?.description ?: "") }
    var dataUrgency by remember { mutableStateOf(tmpTodo?.urgency ?: "Low") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text("Judul Tugas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dataTitle,
                onValueChange = { dataTitle = it },
                placeholder = { Text("Apa yang perlu dilakukan?") },
                leadingIcon = { Icon(Icons.Default.Title, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        Column {
            Text("Prioritas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrioritySelectionCard(Modifier.weight(1f), "Rendah", dataUrgency == "Low", UrgencyLow) { dataUrgency = "Low" }
                PrioritySelectionCard(Modifier.weight(1f), "Sedang", dataUrgency == "Medium", UrgencyMedium) { dataUrgency = "Medium" }
                PrioritySelectionCard(Modifier.weight(1f), "Tinggi", dataUrgency == "High", UrgencyHigh) { dataUrgency = "High" }
            }
        }

        Column {
            Text("Deskripsi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dataDescription,
                onValueChange = { dataDescription = it },
                placeholder = { Text("Tambah detail tugas ini...") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                maxLines = 10
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // The actual trigger button (hidden overlay over FAB)
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        Button(
            onClick = {
                if(dataTitle.isEmpty()) {
                    AlertHelper.show(alertState, AlertType.ERROR, "Judul tidak boleh kosong!")
                    return@Button
                }
                onSave(dataTitle, dataDescription, dataUrgency)
            },
            modifier = Modifier.alpha(0f).size(150.dp, 56.dp)
        ) { }
    }

    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = { AlertHelper.dismiss(alertState) },
            title = { Text(alertState.value.type.title) },
            text = { Text(alertState.value.message) },
            confirmButton = { TextButton(onClick = { AlertHelper.dismiss(alertState) }) { Text("OK") } }
        )
    }
}

@Composable
fun PrioritySelectionCard(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected) BorderStroke(2.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
