package com.example.composemlkit.common.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.composemlkit.common.enums.Examples
import com.example.composemlkit.common.models.FileData
import com.example.composemlkit.common.nav.param.FileDataNavType
import com.example.composemlkit.ui.FaceDetection
import com.example.composemlkit.ui.Home
import com.example.composemlkit.ui.PhotoResult
import com.google.gson.Gson

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.HOME,
        modifier = modifier
    ) {
        homeViewNav(navController)
        faceDetectionNav(navController)
        photoResultNav()
    }
}

fun NavGraphBuilder.homeViewNav(navController: NavHostController) {
    composable(AppRoute.HOME) {
        Home(onExampleSelected = {
            when (it) {
                Examples.FACE_DETECTION -> navController.navigate(AppRoute.FACE_DETECTION)
            }
        })
    }
}

fun NavGraphBuilder.faceDetectionNav(navController: NavHostController) {
    composable(AppRoute.FACE_DETECTION) {
        FaceDetection(onImageSaved = {
            val json = Uri.encode(Gson().toJson(it))
            navController.navigate("${AppRoute.POTHO_RESULT}/$json")
        })
    }
}

fun NavGraphBuilder.photoResultNav() {
    composable(
        route = "${AppRoute.POTHO_RESULT}/{${AppRouteParam.FILE}}",
        arguments = listOf(
            navArgument(AppRouteParam.FILE) { type = FileDataNavType() }
        )
    ) { backStackEntry ->
        val fileData = backStackEntry.arguments?.getParcelable<FileData>(AppRouteParam.FILE)
        requireNotNull(fileData)
        PhotoResult(fileData.absolutePath)
    }
}
