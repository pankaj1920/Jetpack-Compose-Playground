package com.steleot.jetpackcompose.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.steleot.jetpackcompose.playground.datastore.ProtoManager
import com.steleot.jetpackcompose.playground.helpers.InAppReviewHelper
import com.steleot.jetpackcompose.playground.navigation.*
import com.steleot.jetpackcompose.playground.theme.JetpackComposePlaygroundTheme
import com.steleot.jetpackcompose.playground.theme.ThemeState
import com.steleot.jetpackcompose.playground.theme.getMaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var inAppReviewHelper: InAppReviewHelper

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var firebaseInstallations: FirebaseInstallations

    @Inject
    lateinit var protoManager: ProtoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleFirebase()
        MobileAds.initialize(this)
        setContent {
            JetpackComposeApp(inAppReviewHelper, firebaseAnalytics, protoManager)
        }
    }

    private fun handleFirebase() {
        if (BuildConfig.DEBUG) {
            lifecycleScope.launchWhenCreated {
                val token = try {
                    firebaseMessaging.token.await()
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
                Timber.d("Token retrieved: $token")
            }
            lifecycleScope.launchWhenCreated {
                val id = try {
                    firebaseInstallations.id.await()
                } catch (e: Exception) {
                    Timber.e(e)
                    null
                }
                Timber.d("Id retrieved: $id")
            }
        }
    }
}

private const val NavigationDuration = 600

@Suppress("ControlFlowWithEmptyBody")
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun JetpackComposeApp(
    inAppReviewHelper: InAppReviewHelper,
    firebaseAnalytics: FirebaseAnalytics,
    protoManager: ProtoManager,
) {
    val isDarkTheme = isSystemInDarkTheme()
    var isLoaded by rememberSaveable { mutableStateOf(false) }
    var themeState by rememberSaveable {
        mutableStateOf(ThemeState(isDarkTheme = isDarkTheme))
    }
    val systemUiController = rememberSystemUiController()
    SideEffect {

        if (isLoaded) {
            systemUiController.setSystemBarsColor(
                themeState.colorPalette.getMaterialColors(themeState.isDarkTheme).primaryVariant
            )
        }
    }
    val screenWidth = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.roundToPx()
    }
    LaunchedEffect(Unit) {
        if (themeState.isDarkTheme != isDarkTheme) {
            themeState = themeState.copy(isDarkTheme = isDarkTheme)
        }
        if (!isLoaded) {
            protoManager.colorPalette.collect { colorPalette ->
                themeState = themeState.copy(colorPalette = colorPalette)
                delay(250L)
                isLoaded = true
            }
        }
    }
    if (isLoaded) {
        JetpackComposePlaygroundTheme(
            colorPalette = themeState.colorPalette
        ) {
            ProvideWindowInsets {
                CompositionLocalProvider(LocalInAppReviewer provides inAppReviewHelper) {
                    CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
                        val navController = rememberAnimatedNavController()
                        DisposableEffect(Unit) {
                            val listener =
                                NavController.OnDestinationChangedListener { _, destination, _ ->
                                    destination.route?.let { route ->
                                        Timber.d("Route : $route")
                                        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                                            param(FirebaseAnalytics.Param.SCREEN_NAME, route)
                                        }
                                    }
                                }
                            navController.addOnDestinationChangedListener(listener)
                            onDispose {
                                navController.removeOnDestinationChangedListener(listener)
                            }
                        }
                        AnimatedNavHost(
                            navController = navController,
                            startDestination = MainNavRoutes.Main,
                            enterTransition = { _, target ->
                                when (target.destination.route) {
                                    MainNavRoutes.Popular,
                                    MainNavRoutes.Search,
                                    MainNavRoutes.Settings ->
                                        fadeIn(animationSpec = tween(NavigationDuration))
                                    else -> slideInHorizontally(
                                        initialOffsetX = { screenWidth },
                                        animationSpec = tween(NavigationDuration)
                                    )
                                }
                            },
                            exitTransition = { _, target ->
                                when (target.destination.route) {
                                    MainNavRoutes.Popular,
                                    MainNavRoutes.Search,
                                    MainNavRoutes.Settings ->
                                        fadeOut(animationSpec = tween(NavigationDuration))
                                    else ->
                                        slideOutHorizontally(
                                            targetOffsetX = { -screenWidth },
                                            animationSpec = tween(NavigationDuration)
                                        )
                                }
                            },
                            popEnterTransition = { initial, _ ->
                                when (initial.destination.route) {
                                    MainNavRoutes.Popular,
                                    MainNavRoutes.Search,
                                    MainNavRoutes.Settings ->
                                        fadeIn(animationSpec = tween(NavigationDuration))
                                    else ->
                                        slideInHorizontally(
                                            initialOffsetX = { -screenWidth },
                                            animationSpec = tween(NavigationDuration)
                                        )
                                }
                            },
                            popExitTransition = { initial, _ ->
                                when (initial.destination.route) {
                                    MainNavRoutes.Popular,
                                    MainNavRoutes.Search,
                                    MainNavRoutes.Settings ->
                                        fadeOut(animationSpec = tween(NavigationDuration))
                                    else ->
                                        slideOutHorizontally(
                                            targetOffsetX = { screenWidth },
                                            animationSpec = tween(NavigationDuration)
                                        )
                                }
                            }
                        ) {
                            /* main */
                            addMainRoutes(navController, themeState) { newThemeState ->
                                themeState = newThemeState
                            }
                            /* activity */
                            addActivityRoutes(navController)
                            /* animation */
                            addAnimationRoutes()
                            /* constraint layout */
                            addConstraintLayoutRoutes()
                            /* foundation */
                            addFoundationRoutes()
                            /* foundation layout */
                            addFoundationLayoutRoutes()
                            /* material */
                            addMaterialRoutes()
                            /* material icons */
                            addMaterialIconsRoutes()
                            /* material icons extended */
                            addMaterialIconsExtended()
                            /* runtime */
                            addRuntimeRoutes()
                            /* ui */
                            addUiRoutes()
                            /* view model */
                            addViewModelRoutes()
                            /* custom examples */
                            addCustomExamples()
                            /* external */
                            addExternalLibraries(navController, systemUiController)
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

val LocalInAppReviewer = staticCompositionLocalOf<InAppReviewHelper> {
    error("CompositionLocal InAppReviewHelper not present")
}
