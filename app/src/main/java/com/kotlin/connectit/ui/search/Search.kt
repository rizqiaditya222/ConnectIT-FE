package com.kotlin.connectit.ui.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kotlin.connectit.ui.home.PostItem
import com.kotlin.connectit.ui.home.PostOptionsBottomSheetContent
import com.kotlin.connectit.util.ResultWrapper
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToUserProfile: (userId: String) -> Unit,
    onNavigateToPostDetail: (postId: String) -> Unit,
    onNavigateToEditPost: (postId: String) -> Unit // Tambahkan callback ini
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorMessage()
        }
    }

    LaunchedEffect(key1 = uiState.postSearchResult) {
        when (uiState.postSearchResult) {
            is ResultWrapper.Error -> {
                val result = uiState.postSearchResult as ResultWrapper.Error
                Toast.makeText(context, result.message ?: "Gagal mencari post", Toast.LENGTH_LONG).show()
                viewModel.consumePostSearchResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(key1 = uiState.userSearchResult) {
        when (uiState.userSearchResult) {
            is ResultWrapper.Error -> {
                val result = uiState.userSearchResult as ResultWrapper.Error
                Toast.makeText(context, result.message ?: "Gagal mencari pengguna", Toast.LENGTH_LONG).show()
                viewModel.consumeUserSearchResult()
            }
            else -> {}
        }
    }

    LaunchedEffect(key1 = uiState.deletePostResult) {
        when (val result = uiState.deletePostResult) {
            is ResultWrapper.Success -> {
                Toast.makeText(context, result.data.data?.message ?: "Post berhasil dihapus", Toast.LENGTH_SHORT).show()
                viewModel.onDismissBottomSheet() // Tutup bottom sheet setelah delete
            }
            is ResultWrapper.Error -> {
                Toast.makeText(context, result.message ?: "Gagal menghapus post", Toast.LENGTH_LONG).show()
            }
            null -> {}
        }
        viewModel.consumeDeletePostResult()
    }


    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                shadowElevation = 4.dp,
                color = Color(0xFF191A1F)
            ) {
                OutlinedTextField(
                    value = uiState.searchText,
                    onValueChange = { viewModel.onSearchTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search users or posts...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search Icon", tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (uiState.searchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchTextChanged("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear Search", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2A2A2F),
                        unfocusedContainerColor = Color(0xFF2A2A2F),
                        cursorColor = Color(0xFF8B5CF6),
                        focusedIndicatorColor = Color(0xFF8B5CF6),
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.performSearch()
                        keyboardController?.hide()
                    })
                )
            }
        },
        containerColor = Color(0xFF191A1F)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchTabButton(
                    text = "Posts",
                    isSelected = uiState.selectedTab == SearchTab.POSTS,
                    onClick = { viewModel.onTabSelected(SearchTab.POSTS) },
                    modifier = Modifier.weight(1f)
                )
                SearchTabButton(
                    text = "Users",
                    isSelected = uiState.selectedTab == SearchTab.USERS,
                    onClick = { viewModel.onTabSelected(SearchTab.USERS) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B5CF6))
                }
            } else {
                when (uiState.selectedTab) {
                    SearchTab.POSTS -> {
                        val postResult = uiState.postSearchResult
                        if (postResult is ResultWrapper.Success) {
                            if (postResult.data.isEmpty() && uiState.searchPerformed && uiState.searchText.isNotEmpty()) {
                                EmptySearchResult(message = "No posts found for \"${uiState.searchText}\"")
                            } else if (postResult.data.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().weight(1f),
                                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                ) {
                                    items(postResult.data, key = { it.postId }) { post ->
                                        PostItem(
                                            postData = post,
                                            onMoreOptionsClick = { selectedPost ->
                                                if (selectedPost.userId == uiState.loggedInUserId) {
                                                    viewModel.onShowBottomSheet(selectedPost)
                                                } else {
                                                    Toast.makeText(context, "You can only manage your own posts.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onUsernameClick = { userId ->
                                                onNavigateToUserProfile(userId)
                                            },
                                            currentLoggedInUserId = uiState.loggedInUserId
                                        )
                                    }
                                }
                            } else if (uiState.searchText.isBlank() && !uiState.searchPerformed) {
                                InitialSearchPrompt()
                            } else if (uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                                EmptySearchResult(message = "No posts found for \"${uiState.searchText}\"")
                            } else if (!uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                                Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("Press search or keep typing to see results...", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                Spacer(modifier = Modifier.fillMaxSize().weight(1f))
                            }
                        } else if (postResult is ResultWrapper.Error && uiState.searchPerformed) {
                            EmptySearchResult(message = postResult.message ?: "Error searching posts.")
                        } else if (uiState.searchText.isBlank() && !uiState.searchPerformed) {
                            InitialSearchPrompt()
                        } else if (!uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                            Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Press search or keep typing to see results...", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
                            }
                        }
                        else {
                            Spacer(modifier = Modifier.fillMaxSize().weight(1f))
                        }
                    }
                    SearchTab.USERS -> {
                        val userResult = uiState.userSearchResult
                        if (userResult is ResultWrapper.Success) {
                            if (userResult.data.isEmpty() && uiState.searchPerformed && uiState.searchText.isNotEmpty()) {
                                EmptySearchResult(message = "No users found for \"${uiState.searchText}\"")
                            } else if (userResult.data.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().weight(1f),
                                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                ) {
                                    items(userResult.data, key = { it.id }) { user ->
                                        UserSearchResultItem(
                                            userData = user,
                                            onClick = { userId -> onNavigateToUserProfile(userId) }
                                        )
                                    }
                                }
                            } else if (uiState.searchText.isBlank() && !uiState.searchPerformed) {
                                InitialSearchPrompt()
                            } else if (uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                                EmptySearchResult(message = "No users found for \"${uiState.searchText}\"")
                            } else if (!uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                                Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("Press search or keep typing to see results...", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                Spacer(modifier = Modifier.fillMaxSize().weight(1f))
                            }
                        } else if (userResult is ResultWrapper.Error && uiState.searchPerformed) {
                            EmptySearchResult(message = userResult.message ?: "Error searching users.")
                        } else if (uiState.searchText.isBlank() && !uiState.searchPerformed) {
                            InitialSearchPrompt()
                        } else if (!uiState.searchPerformed && uiState.searchText.isNotEmpty()){
                            Box(modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("Press search or keep typing to see results...", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            Spacer(modifier = Modifier.fillMaxSize().weight(1f))
                        }
                    }
                }
            }
        }

        if (uiState.showBottomSheet && uiState.selectedPostForOptions != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onDismissBottomSheet() },
                sheetState = sheetState,
                containerColor = Color(0xFF2A2A2F),
                contentColor = Color.White
            ) {
                PostOptionsBottomSheetContent(
                    selectedPostCaption = uiState.selectedPostForOptions?.caption ?: "Selected Post",
                    onEditClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.onDismissBottomSheet()
                                uiState.selectedPostForOptions?.postId?.let { postId ->
                                    onNavigateToEditPost(postId)
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.onDismissBottomSheet()
                                uiState.selectedPostForOptions?.postId?.let { postId ->
                                    viewModel.attemptDeletePost(postId)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Box(
            modifier = Modifier
                .height(if (isSelected) 3.dp else 1.dp)
                .fillMaxWidth(0.7f)
                .background(
                    if (isSelected) Color(0xFF8B5CF6) else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun EmptySearchResult(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun InitialSearchPrompt() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Prompt",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Search for amazing posts or interesting users.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF191A1F)
@Composable
fun PreviewSearchScreen() {
    MaterialTheme {
        SearchScreen(
            onNavigateToUserProfile = {},
            onNavigateToPostDetail = {},
            onNavigateToEditPost = {}
        )
    }
}