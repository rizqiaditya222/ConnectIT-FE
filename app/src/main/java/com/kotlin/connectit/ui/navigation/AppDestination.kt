package com.kotlin.connectit.navigation

object AppDestinations {
    const val SPLASH = "splash"
    const val GETTING_STARTED = "getting_started"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val COMPLETE_PROFILE = "complete_profile"
    const val MAIN_APP_FLOW = "main_app_flow"

    const val HOME_TAB = "home_tab"
    const val SEARCH_TAB = "search_tab"
    const val PROFILE_TAB = "profile_tab"

    const val EDIT_POST = "edit_post/{postId}" // Tambahkan ini
    fun editPostRoute(postId: String) = "edit_post/$postId" // Tambahkan ini

    const val USER_PROFILE_DETAIL = "user_profile_detail/{userId}"
    fun userProfileDetailRoute(userId: String) = "user_profile_detail/$userId"

    const val POST_DETAIL = "post_detail/{postId}"
    fun postDetailRoute(postId: String) = "post_detail/$postId"

    const val CREATE_POST = "create_post"
    const val EDIT_PROFILE = "edit_profile/{userId}"
    fun editProfileRoute(userId: String) = "edit_profile/$userId"
}

