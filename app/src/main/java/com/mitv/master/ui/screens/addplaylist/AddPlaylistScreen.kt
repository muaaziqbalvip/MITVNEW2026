package com.mitv.master.ui.screens.addplaylist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mitv.master.ui.theme.MitvRed
import com.mitv.master.ui.theme.MitvSurface
import com.mitv.master.ui.theme.MitvSurfaceElevated
import com.mitv.master.ui.theme.MitvTextSecondary
import com.mitv.master.viewmodel.AddPlaylistResult
import com.mitv.master.viewmodel.AddPlaylistViewModel

/**
 * 3-tab "Add Playlist" flow: paste an M3U/M3U8 URL, upload a local .m3u file,
 * or log in with Xtream Codes (server/username/password) which pulls Live TV,
 * Movies, and Series in one go.
 */
@Composable
fun AddPlaylistScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddPlaylistViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("M3U URL", "Upload File", "Xtream Codes")

    LaunchedEffect(result) {
        if (result is AddPlaylistResult.Success) {
            onSuccess()
            viewModel.resetResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Add Playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = MitvRed,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        viewModel.resetResult()
                    },
                    text = {
                        Text(
                            title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.padding(20.dp)) {
            when (selectedTab) {
                0 -> M3uUrlTab(viewModel)
                1 -> UploadFileTab(viewModel)
                2 -> XtreamTab(viewModel)
            }
        }

        when (val current = result) {
            is AddPlaylistResult.Loading -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MitvRed, modifier = Modifier.size(28.dp))
                }
            }
            is AddPlaylistResult.Error -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(Color(0xFF2A1414), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MitvRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = current.message,
                        color = Color(0xFFFF8A8A),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun M3uUrlTab(viewModel: AddPlaylistViewModel) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Column {
        Text(
            "Paste your M3U or M3U8 playlist link",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        MitvTextField(value = name, onValueChange = { name = it }, label = "Playlist name")
        MitvTextField(
            value = url,
            onValueChange = { url = it },
            label = "M3U / M3U8 URL",
            keyboardType = KeyboardType.Uri,
            leadingIcon = Icons.Filled.Link
        )
        MitvPrimaryButton(text = "Add Playlist") {
            viewModel.addFromM3uUrl(name.trim(), url.trim())
        }
    }
}

@Composable
private fun UploadFileTab(viewModel: AddPlaylistViewModel) {
    var name by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var fileError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fileError = null
            try {
                val text = context.contentResolver.openInputStream(it)
                    ?.bufferedReader()
                    ?.use { reader -> reader.readText() }
                if (text.isNullOrBlank()) {
                    fileError = "Selected file is empty or could not be read."
                    fileContent = null
                } else {
                    fileContent = text
                }
                fileName = it.lastPathSegment ?: "playlist.m3u"
            } catch (e: Exception) {
                fileError = "${e.javaClass.simpleName}: ${e.message ?: "Could not read file."}"
                fileContent = null
            }
        }
    }

    Column {
        Text(
            "Select an .m3u or .m3u8 file from your phone",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        MitvTextField(value = name, onValueChange = { name = it }, label = "Playlist name")

        Button(
            onClick = { filePicker.launch("*/*") },
            colors = ButtonDefaults.buttonColors(containerColor = MitvSurfaceElevated),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
            Text(
                text = fileName ?: "Choose File",
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (fileContent != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Text(
                    text = "File loaded",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        fileError?.let {
            Text(
                text = it,
                color = MitvRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        MitvPrimaryButton(text = "Add Playlist", enabled = fileContent != null) {
            fileContent?.let { viewModel.addFromUploadedContent(name.trim(), it) }
        }
    }
}

@Composable
private fun XtreamTab(viewModel: AddPlaylistViewModel) {
    var name by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        Text(
            "Log in with your Xtream Codes panel details",
            color = MitvTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        MitvTextField(value = name, onValueChange = { name = it }, label = "Playlist name")
        MitvTextField(
            value = server,
            onValueChange = { server = it },
            label = "Server (e.g. http://server.com:8080)",
            keyboardType = KeyboardType.Uri,
            leadingIcon = Icons.Filled.Link
        )
        MitvTextField(value = username, onValueChange = { username = it }, label = "Username")
        MitvTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            leadingIcon = Icons.Filled.VpnKey
        )

        MitvPrimaryButton(text = "Connect & Add") {
            viewModel.addFromXtream(name.trim(), server.trim(), username.trim(), password)
        }
    }
}

@Composable
private fun MitvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, tint = MitvTextSecondary) }
        },
        visualTransformation = if (isPassword) {
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MitvRed,
            unfocusedBorderColor = Color(0xFF444444),
            focusedContainerColor = MitvSurface,
            unfocusedContainerColor = MitvSurface,
            focusedLabelColor = MitvRed,
            unfocusedLabelColor = MitvTextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = MitvRed
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
private fun MitvPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MitvRed,
            contentColor = Color.White,
            disabledContainerColor = MitvSurfaceElevated,
            disabledContentColor = MitvTextSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(top = 8.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
