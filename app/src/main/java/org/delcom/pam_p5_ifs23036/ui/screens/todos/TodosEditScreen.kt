package org.delcom.pam_p5_ifs23036.ui.screens.todos

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
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
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23036.ui.viewmodels.TodoViewModel

@Composable
fun TodosEditScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    todoId: String
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var todo by remember { mutableStateOf<ResponseTodoData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        todoViewModel.resetActionStates()
        todoViewModel.getTodoById(authToken.value!!, todoId)
    }

    LaunchedEffect(uiStateTodo.todo) {
        if (uiStateTodo.todo !is TodoUIState.Loading) {
            if (uiStateTodo.todo is TodoUIState.Success) {
                todo = (uiStateTodo.todo as TodoUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
                isLoading = false
            }
        }
    }

    fun onSave(title: String, description: String, isDone: Boolean, urgency: String) {
        isLoading = true
        todoViewModel.putTodo(authToken = authToken.value!!, todoId = todoId, title = title, description = description, isDone = isDone, urgency = urgency)
    }

    LaunchedEffect(uiStateTodo.todoChange) {
        when (val state = uiStateTodo.todoChange) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.TodosDetail.path.replace("{todoId}", todoId), true)
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
        topBar = { TopAppBarComponent(navController, "Ubah Tugas", true) },
        bottomBar = { BottomNavComponent(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (todo != null) {
                TodosEditUI(todo = todo!!, onSave = ::onSave)
            }
            if (isLoading || todo == null) LoadingUI()
        }
    }
}

@Composable
fun TodosEditUI(
    todo: ResponseTodoData,
    onSave: (String, String, Boolean, String) -> Unit
) {
    val alertState = remember { mutableStateOf(AlertState()) }
    var dataTitle by remember { mutableStateOf(todo.title ?: "") }
    var dataDescription by remember { mutableStateOf(todo.description ?: "") }
    var dataIsDone by remember { mutableStateOf(todo.isDone) }
    var dataUrgency by remember { mutableStateOf(todo.urgency ?: "Low") }

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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Title, null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tandai Selesai", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Switch(
                checked = dataIsDone,
                onCheckedChange = { dataIsDone = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
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
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (dataTitle.isEmpty()) {
                    AlertHelper.show(alertState, AlertType.ERROR, "Judul tidak boleh kosong!")
                    return@Button
                }
                onSave(dataTitle, dataDescription, dataIsDone, dataUrgency)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
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
