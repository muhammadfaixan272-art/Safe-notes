package com.example.ui

import android.widget.Toast
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.DecryptedNote
import com.example.data.GeminiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Active Theme state variables for seamless peaceful color combination transition
var ColorSlateBackground by mutableStateOf(Color(0xFF141C16))
var ColorSlateCard by mutableStateOf(Color(0xFF1E2821))
var ColorEmeraldNeon by mutableStateOf(Color(0xFF6B8F71))
var ColorEmeraldGlow by mutableStateOf(Color(0x336B8F71))

val ColorAmberWarning = Color(0xFFF59E0B)
val ColorCrimsonDanger = Color(0xFFEF4444)

val NoteColors = listOf(
    "#FF1B1C22", // Slate
    "#FF1E293B", // Steel Blue
    "#FF064E3B", // Forest
    "#FF451A03", // Bronze
    "#FF311042", // Deep Grape
    "#FF180E29", // Midnight
    "#FF5C1D24"  // Rust
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesApp(
    viewModel: NotesViewModel,
    onTriggerBiometric: () -> Unit
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val hasSetPin by viewModel.hasSetPin.collectAsStateWithLifecycle()
    val overrideDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val themeSelection by viewModel.themeSelection.collectAsStateWithLifecycle()

    val darkTheme = overrideDarkMode ?: true // Dark Theme First default

    // Side-effect to keep global legacy color states in sync with active selected theme
    LaunchedEffect(themeSelection, darkTheme) {
        val (p, bg, cd) = when (themeSelection) {
            "emerald" -> {
                Triple(Color(0xFF10B981), if (darkTheme) Color(0xFF0F1013) else Color(0xFFFAFAFA), if (darkTheme) Color(0xFF1B1C22) else Color(0xFFFFFFFF))
            }
            "ocean" -> {
                Triple(if (darkTheme) Color(0xFF0EA5E9) else Color(0xFF0369A1), if (darkTheme) Color(0xFF0C131D) else Color(0xFFF0F4F8), if (darkTheme) Color(0xFF152130) else Color(0xFFFFFFFF))
            }
            "lavender" -> {
                Triple(if (darkTheme) Color(0xFFB497FF) else Color(0xFF7C5CFF), if (darkTheme) Color(0xFF0F0E17) else Color(0xFFFAF8FF), if (darkTheme) Color(0xFF1C1A27) else Color(0xFFFFFFFF))
            }
            else -> { // "sage"
                Triple(if (darkTheme) Color(0xFF6B8F71) else Color(0xFF425F48), if (darkTheme) Color(0xFF141C16) else Color(0xFFFBF9F2), if (darkTheme) Color(0xFF1E2821) else Color(0xFFFFFFFF))
            }
        }
        ColorEmeraldNeon = p
        ColorSlateBackground = bg
        ColorSlateCard = cd
        ColorEmeraldGlow = p.copy(alpha = 0.2f)
    }

    val (primaryColor, backgroundColor, cardColor) = when (themeSelection) {
        "emerald" -> { // Emerald Vault (Original)
            val primary = Color(0xFF10B981)
            val background = if (darkTheme) Color(0xFF0F1013) else Color(0xFFFAFAFA)
            val card = if (darkTheme) Color(0xFF1B1C22) else Color(0xFFFFFFFF)
            Triple(primary, background, card)
        }
        "ocean" -> { // Ocean Breeze (Peaceful Calming Indigo / Sky Blue)
            val primary = if (darkTheme) Color(0xFF0EA5E9) else Color(0xFF0369A1)
            val background = if (darkTheme) Color(0xFF0C131D) else Color(0xFFF0F4F8)
            val card = if (darkTheme) Color(0xFF152130) else Color(0xFFFFFFFF)
            Triple(primary, background, card)
        }
        "lavender" -> { // Lavender Twilight (Restful Dusk Violet)
            val primary = if (darkTheme) Color(0xFFB497FF) else Color(0xFF7C5CFF)
            val background = if (darkTheme) Color(0xFF0F0E17) else Color(0xFFFAF8FF)
            val card = if (darkTheme) Color(0xFF1C1A27) else Color(0xFFFFFFFF)
            Triple(primary, background, card)
        }
        else -> { // "sage" - Sage Garden (Peaceful Sage Green & Creams/Sands)
            val primary = if (darkTheme) Color(0xFF6B8F71) else Color(0xFF425F48)
            val background = if (darkTheme) Color(0xFF141C16) else Color(0xFFFBF9F2)
            val card = if (darkTheme) Color(0xFF1E2821) else Color(0xFFFFFFFF)
            Triple(primary, background, card)
        }
    }

    // Dynamic, peaceful theme builder
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = primaryColor,
                background = backgroundColor,
                surface = cardColor,
                onPrimary = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                background = backgroundColor,
                surface = cardColor,
                onPrimary = Color.White,
                onBackground = if (themeSelection == "sage") Color(0xFF2D3C31) else Color(0xFF1E293B),
                onSurface = if (themeSelection == "sage") Color(0xFF2D3C31) else Color(0xFF1E293B)
            )
        },
        typography = MaterialTheme.typography
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = if (isLocked) ScreenState.LockScreen else screenState,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) with
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                },
                label = "ScreenTransition"
            ) { targetState ->
                when (targetState) {
                    is ScreenState.Splash -> {
                        SplashScreen()
                    }
                    is ScreenState.OnboardingIntro -> {
                        OnboardingIntroScreen(
                            onNext = { viewModel.navigateTo(ScreenState.CreatePin) }
                        )
                    }
                    is ScreenState.CreatePin -> {
                        PinEntryScreen(
                            title = "Choose New PIN",
                            subtitle = "Create a secure 4 to 8-digit unlock passcode to encrypt your safe space.",
                            isVerification = false,
                            onPinEntered = { pin -> viewModel.setProposedPin(pin) }
                        )
                    }
                    is ScreenState.ConfirmPin -> {
                        val proposed = (targetState as ScreenState.ConfirmPin).proposedPin
                        PinEntryScreen(
                            title = "Confirm Saved PIN",
                            subtitle = "Re-enter the passcode to matches the hardware parameters.",
                            isVerification = true,
                            onPinEntered = { pin ->
                                val ok = viewModel.confirmAndSavePin(proposed, pin)
                                if (ok) {
                                    Toast.makeText(context, "Encrypted Vault Protected Successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Passcodes did not match. Please try again.", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateTo(ScreenState.CreatePin)
                                }
                            },
                            onCancel = { viewModel.navigateTo(ScreenState.CreatePin) }
                        )
                    }
                    is ScreenState.LockScreen -> {
                        val loginError by viewModel.loginErrorMessage.collectAsStateWithLifecycle()
                        val failedAttempts by viewModel.failedAttempts.collectAsStateWithLifecycle()
                        val isDestructionActive by viewModel.selfDestructEnabled.collectAsStateWithLifecycle()

                        LockScreenCover(
                            errorMessage = loginError,
                            failedCount = failedAttempts,
                            selfDestructEnabled = isDestructionActive,
                            onPinEntered = { pin -> viewModel.attemptUnlock(pin) },
                            biometricEnabled = viewModel.biometricsEnabled.value,
                            onTriggerBiometric = onTriggerBiometric
                        )
                    }
                    is ScreenState.NotesList -> {
                        NotesListScreen(viewModel = viewModel)
                    }
                    is ScreenState.EditNote -> {
                        EditNoteScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ColorEmeraldNeon)
    }
}

@Composable
fun OnboardingIntroScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Illustration generated dynamically
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .size(240.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .testTag("onboarding_hero_image"),
                colors = CardDefaults.cardColors(containerColor = ColorSlateCard)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_hero),
                    contentDescription = "Onboarding Illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Safe Notes Vault",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your journal is encrypted with modern military-grade AES-256 GCM in local hardware memory. No remote keys, no trackers, completely serverless & offline.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorEmeraldNeon,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_next_button")
        ) {
            Text(
                text = "Get Started",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun PinEntryScreen(
    title: String,
    subtitle: String,
    isVerification: Boolean,
    onPinEntered: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock",
                tint = ColorEmeraldNeon,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Passcode Indicators (Dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine length of dots (standard 4 is minimum, up to length entered)
                val displayCount = maxOf(4, enteredPin.length)
                for (i in 0 until displayCount) {
                    val isActive = i < enteredPin.length
                    val dotColor = if (isActive) ColorEmeraldNeon else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(width = 1.dp, color = if (isActive) ColorEmeraldNeon else Color.Transparent, shape = CircleShape)
                    )
                }
            }

            if (pinErrorText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pinErrorText!!,
                    color = ColorCrimsonDanger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Numerical Secure Pad Grid Layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(if (isVerification) "Cancel" else "", "0", "Back")
                    ).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.3f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (char.isNotEmpty()) ColorSlateCard else Color.Transparent)
                                        .clickable(enabled = char.isNotEmpty()) {
                                            when (char) {
                                                "Back" -> {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                }
                                                "Cancel" -> {
                                                    onCancel?.invoke()
                                                }
                                                else -> {
                                                    if (enteredPin.length < 8) {
                                                        enteredPin += char
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("pin_key_$char"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (char == "Back") {
                                        Icon(
                                            imageVector = Icons.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    } else {
                                        Text(
                                            text = char,
                                            fontSize = if (char.length > 1) 14.sp else 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (char == "Cancel") ColorCrimsonDanger else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (enteredPin.length < 4) {
                        pinErrorText = "PIN must be at least 4 digits long!"
                    } else {
                        onPinEntered(enteredPin)
                    }
                },
                enabled = enteredPin.length >= 4,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorEmeraldNeon,
                    contentColor = Color.Black,
                    disabledContainerColor = ColorSlateCard.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("pin_submit_button")
            ) {
                Text(
                    text = if (isVerification) "Confirm Setup" else "Proceed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun LockScreenCover(
    errorMessage: String?,
    failedCount: Int,
    selfDestructEnabled: Boolean,
    onPinEntered: (String) -> Unit,
    biometricEnabled: Boolean,
    onTriggerBiometric: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var triggerShake by remember { mutableStateOf(false) }

    // Keypad shake calculation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            triggerShake = true
            shakeOffset.animateTo(
                targetValue = 15f,
                animationSpec = keyframes {
                    durationMillis = 300
                    0f at 0
                    -10f at 50
                    10f at 100
                    -10f at 150
                    10f at 200
                    -5f at 250
                    0f at 300
                }
            )
            triggerShake = false
        }
    }

    // Automatically trigger biometrics when Lock screen mounts
    LaunchedEffect(Unit) {
        if (biometricEnabled) {
            onTriggerBiometric()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Safe Lock Icon",
                tint = if (failedCount > 0) ColorAmberWarning else ColorEmeraldNeon,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Secure Vault Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter secure credentials to open personal details.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN entry indicators with shake animations
            Row(
                modifier = Modifier.offset(x = shakeOffset.value.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayCount = maxOf(4, enteredPin.length)
                for (i in 0 until displayCount) {
                    val isActive = i < enteredPin.length
                    val activeColor = if (failedCount > 0) ColorAmberWarning else ColorEmeraldNeon
                    val dotColor = if (isActive) activeColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selfDestructEnabled) ColorCrimsonDanger.copy(alpha = 0.15f) else ColorAmberWarning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = if (selfDestructEnabled) ColorCrimsonDanger else ColorAmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = if (selfDestructEnabled) ColorCrimsonDanger else ColorAmberWarning,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Lock screen Secure Keyboard
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Bio", "0", "Back")
                    ).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.3f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (char == "Bio") {
                                                if (biometricEnabled) ColorEmeraldGlow else Color.Transparent
                                            } else {
                                                ColorSlateCard
                                            }
                                        )
                                        .border(
                                            width = if (char == "Bio" && biometricEnabled) 1.dp else 0.dp,
                                            color = if (char == "Bio" && biometricEnabled) ColorEmeraldNeon else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            when (char) {
                                                "Back" -> {
                                                    if (enteredPin.isNotEmpty()) {
                                                        enteredPin = enteredPin.dropLast(1)
                                                    }
                                                }
                                                "Bio" -> {
                                                    if (biometricEnabled) {
                                                        onTriggerBiometric()
                                                    }
                                                }
                                                else -> {
                                                    if (enteredPin.length < 8) {
                                                        enteredPin += char
                                                        // Automatically invoke when length reaches standard threshold if it's set
                                                        if (enteredPin.length >= 4) {
                                                            // We let user press enter or submit automatically if suitable
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("lock_key_$char"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (char) {
                                        "Back" -> {
                                            Icon(
                                                imageVector = Icons.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        "Bio" -> {
                                            if (biometricEnabled) {
                                                Icon(
                                                    imageVector = Icons.Filled.Fingerprint,
                                                    contentDescription = "Biometric Auth",
                                                    tint = ColorEmeraldNeon,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = char,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onPinEntered(enteredPin)
                    enteredPin = ""
                },
                enabled = enteredPin.length >= 4,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorEmeraldNeon,
                    contentColor = Color.Black,
                    disabledContainerColor = ColorSlateCard.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("lock_submit_button")
            ) {
                Text(
                    text = "Unlock Notes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(viewModel: NotesViewModel) {
    val notes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Multi-Select and Export capabilities
    var selectedNoteIds by remember { mutableStateOf(emptySet<Int>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                if (isSelectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedNoteIds = emptySet() },
                                modifier = Modifier.testTag("selection_cancel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Selection",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selectedNoteIds.size} Selected",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEmeraldNeon
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val context = LocalContext.current
                            IconButton(
                                onClick = {
                                    val selectedNotes = notes.filter { it.id in selectedNoteIds }
                                    val file = NoteExporter.exportToPdf(context, selectedNotes)
                                    if (file != null) {
                                        NoteExporter.shareFile(context, file, "application/pdf")
                                        selectedNoteIds = emptySet()
                                    } else {
                                        Toast.makeText(context, "PDF export failed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("export_selected_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Export Selected as PDF",
                                    tint = ColorEmeraldNeon
                                )
                            }

                            IconButton(
                                onClick = {
                                    val selectedNotes = notes.filter { it.id in selectedNoteIds }
                                    val file = NoteExporter.exportToTxt(context, selectedNotes)
                                    if (file != null) {
                                        NoteExporter.shareFile(context, file, "text/plain")
                                        selectedNoteIds = emptySet()
                                    } else {
                                        Toast.makeText(context, "Text export failed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("export_selected_txt_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Export Selected as Plain Text",
                                    tint = ColorEmeraldNeon
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AddModerator,
                                contentDescription = "App Icon Logo",
                                tint = ColorEmeraldNeon,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Safe Notes",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { viewModel.lockApp() },
                                modifier = Modifier.testTag("lock_app_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Lock Notes",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            IconButton(
                                onClick = { isSettingsOpen = true },
                                modifier = Modifier.testTag("settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Security Settings",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                // Cyber Emerald styled search panel
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search encrypted records...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorSlateCard,
                        unfocusedContainerColor = ColorSlateCard,
                        focusedBorderColor = ColorEmeraldNeon,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = ColorEmeraldNeon
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { viewModel.startNewNote() },
                    containerColor = ColorEmeraldNeon,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Encrypted Note",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("notes_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NoteAlt,
                        contentDescription = "No Notes",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Encrypted Archive Empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All personal files are encrypted locally in SQLite. Tap '+' below to store safe notes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalItemSpacing = 10.dp
            ) {
                items(notes, key = { it.id }) { note ->
                    val isSelected = note.id in selectedNoteIds
                    NoteGridCard(
                        note = note,
                        isSelected = isSelected,
                        onNoteClick = {
                            if (isSelectionMode) {
                                selectedNoteIds = if (isSelected) {
                                    selectedNoteIds - note.id
                                } else {
                                    selectedNoteIds + note.id
                                }
                            } else {
                                viewModel.startEditNote(note)
                            }
                        },
                        onNoteLongClick = {
                            if (!isSelectionMode) {
                                selectedNoteIds = setOf(note.id)
                            }
                        },
                        onTogglePin = {
                            if (!isSelectionMode) {
                                viewModel.startEditNote(note)
                                viewModel.updateActiveNote(isPinned = !note.isPinned)
                                viewModel.saveActiveNote()
                            }
                        }
                    )
                }
            }
        }

        if (isSettingsOpen) {
            SecuritySettingsModal(
                viewModel = viewModel,
                onClose = { isSettingsOpen = false }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NoteGridCard(
    note: DecryptedNote,
    isSelected: Boolean = false,
    onNoteClick: () -> Unit,
    onNoteLongClick: () -> Unit = {},
    onTogglePin: () -> Unit
) {
    val dateString = remember(note.updatedAt) {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(Date(note.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onNoteClick,
                onLongClick = onNoteLongClick
            )
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(note.colorHex)).copy(alpha = 0.85f)
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = if (isSelected) ColorEmeraldNeon else if (note.isSensitive) ColorCrimsonDanger.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sensitive badge icon or custom check icon
                if (note.isSensitive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColorCrimsonDanger.copy(alpha = 0.15f))
                                .border(width = 0.5.dp, color = ColorCrimsonDanger, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SECURE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ColorCrimsonDanger
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected Sensitive Note",
                                tint = ColorEmeraldNeon,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorEmeraldNeon.copy(alpha = 0.2f))
                            .border(width = 1.dp, color = ColorEmeraldNeon, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = ColorEmeraldNeon,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SELECTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEmeraldNeon
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Pin toggle button
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pin Icon",
                        tint = if (note.isPinned) ColorEmeraldNeon else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (note.title.isBlank()) "Untitled Record" else note.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (note.title.isBlank()) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (note.content.isBlank()) "No details stored" else note.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = dateString,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EditNoteScreen(viewModel: NotesViewModel) {
    val note by viewModel.activeNote.collectAsStateWithLifecycle()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    if (note == null) return

    val currentNote = note!!

    // Auto-save debouncing tracking
    var lastSavedTitle by remember(currentNote.id) { mutableStateOf(currentNote.title) }
    var lastSavedContent by remember(currentNote.id) { mutableStateOf(currentNote.content) }
    var saveStatus by remember(currentNote.id) { mutableStateOf("Saved") }

    // State for Rich Text / Preview tab selection (0 = Edit, 1 = Preview)
    var editorMode by remember { mutableStateOf(0) }

    // Gemini Co-pilot UI states
    var isGeminiDialogOpen by remember { mutableStateOf(false) }
    
    // Track text value state including selection ranges for rich tools
    var contentValue by remember(currentNote.id) {
        mutableStateOf(TextFieldValue(currentNote.content))
    }

    // Sync contentValue text when note externally updates (e.g. key-decrypted or first opened)
    LaunchedEffect(currentNote.content) {
        if (currentNote.content != contentValue.text) {
            contentValue = contentValue.copy(text = currentNote.content)
        }
    }

    // Debounced Secure Auto-save
    LaunchedEffect(currentNote.title, contentValue.text) {
        if (currentNote.title != lastSavedTitle || contentValue.text != lastSavedContent) {
            saveStatus = "Unsaved Changes"
            delay(2000)
            saveStatus = "Saving secure draft..."
            viewModel.autoSaveActiveNoteSilently {
                lastSavedTitle = currentNote.title
                lastSavedContent = contentValue.text
                saveStatus = "Saved"
            }
        }
    }

    fun insertFormat(tagStart: String, tagEnd: String = "") {
        val text = contentValue.text
        val selection = contentValue.selection
        val start = selection.start
        val end = selection.end
        
        val newText = text.substring(0, start) + tagStart + text.substring(start, end) + tagEnd + text.substring(end)
        val newCursorPosition = if (start == end) {
            start + tagStart.length
        } else {
            end + tagStart.length + tagEnd.length
        }
        
        contentValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
        viewModel.updateActiveNote(content = newText)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(android.graphics.Color.parseColor(currentNote.colorHex)))
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.saveActiveNote() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Return & Save Details",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Safe title indicator
                        Icon(
                            imageVector = Icons.Filled.EnhancedEncryption,
                            contentDescription = "Symmetric Encryption Icon",
                            tint = ColorEmeraldNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorEmeraldNeon
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Smooth Visual Auto-Save Status Badge
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (saveStatus) {
                                            "Saved" -> ColorEmeraldNeon.copy(alpha = 0.15f)
                                            "Saving secure draft..." -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                            else -> ColorAmberWarning.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (saveStatus) {
                                        "Saved" -> Icons.Default.CheckCircle
                                        "Saving secure draft..." -> Icons.Default.Sync
                                        else -> Icons.Default.Edit
                                    },
                                    contentDescription = saveStatus,
                                    tint = when (saveStatus) {
                                        "Saved" -> ColorEmeraldNeon
                                        "Saving secure draft..." -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        else -> ColorAmberWarning
                                    },
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = saveStatus,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (saveStatus) {
                                        "Saved" -> ColorEmeraldNeon
                                        "Saving secure draft..." -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        else -> ColorAmberWarning
                                    }
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { isGeminiDialogOpen = true },
                            modifier = Modifier.testTag("gemini_copilot_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Copilot",
                                tint = ColorEmeraldNeon
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateActiveNote(isPinned = !currentNote.isPinned) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pin Note",
                                tint = if (currentNote.isPinned) ColorEmeraldNeon else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateActiveNote(isSensitive = !currentNote.isSensitive) }
                        ) {
                            Icon(
                                imageVector = if (currentNote.isSensitive) Icons.Filled.EnhancedEncryption else Icons.Filled.LockOpen,
                                contentDescription = "Toggle Sensitive Status",
                                tint = if (currentNote.isSensitive) ColorCrimsonDanger else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }

                        // Export dropdown choices trigger
                        val context = LocalContext.current
                        var isExportMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { isExportMenuExpanded = true },
                                modifier = Modifier.testTag("single_note_export_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Export choices",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            DropdownMenu(
                                expanded = isExportMenuExpanded,
                                onDismissRequest = { isExportMenuExpanded = false },
                                modifier = Modifier.background(ColorSlateCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export as formatted PDF", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF icon", tint = ColorEmeraldNeon) },
                                    onClick = {
                                        isExportMenuExpanded = false
                                        val file = NoteExporter.exportToPdf(context, listOf(currentNote))
                                        if (file != null) {
                                            NoteExporter.shareFile(context, file, "application/pdf")
                                        } else {
                                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("single_note_export_pdf_item")
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as Plain Text", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = "TXT icon", tint = ColorEmeraldNeon) },
                                    onClick = {
                                        isExportMenuExpanded = false
                                        val file = NoteExporter.exportToTxt(context, listOf(currentNote))
                                        if (file != null) {
                                            NoteExporter.shareFile(context, file, "text/plain")
                                        } else {
                                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.testTag("single_note_export_txt_item")
                                )
                            }
                        }

                        if (currentNote.id != 0) {
                            IconButton(
                                onClick = { isDeleteDialogVisible = true },
                                modifier = Modifier.testTag("delete_note_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Destroy Note",
                                    tint = ColorCrimsonDanger
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(android.graphics.Color.parseColor(currentNote.colorHex)))
                .padding(20.dp)
        ) {
            // Color row selection
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(NoteColors) { originalColor ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(originalColor)))
                            .border(
                                width = if (currentNote.colorHex == originalColor) 2.dp else 0.dp,
                                color = if (currentNote.colorHex == originalColor) ColorEmeraldNeon else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateActiveNote(colorHex = originalColor) }
                            .testTag("select_color_$originalColor")
                    )
                }
            }

            // Interactive Editor Title Label Input
            OutlinedTextField(
                value = currentNote.title,
                onValueChange = { viewModel.updateActiveNote(title = it) },
                placeholder = { Text("Title of record", fontSize = 21.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("edit_title_input"),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )

            // Segmented Mode Switcher (Edit vs Previews)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Write/Edit button
                Button(
                    onClick = { editorMode = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (editorMode == 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Mode", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Write & Format", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Rendered Preview button
                Button(
                    onClick = { editorMode = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (editorMode == 1) ColorEmeraldNeon.copy(alpha = 0.2f) else Color.Transparent,
                        contentColor = if (editorMode == 1) ColorEmeraldNeon else MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Visibility, contentDescription = "Preview Mode", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rendered Preview", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Rich Formatting Toolbar (Edit mode only)
            if (editorMode == 0) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bold Action Button
                    item {
                        IconButton(
                            onClick = { insertFormat("**", "**") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = "Insert Bold Text",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Italic Action Button
                    item {
                        IconButton(
                            onClick = { insertFormat("*", "*") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatItalic,
                                contentDescription = "Insert Italic Text",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Underline Action Button
                    item {
                        IconButton(
                            onClick = { insertFormat("<u>", "</u>") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatUnderlined,
                                contentDescription = "Insert Underlined Text",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Divider segment
                    item {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                        )
                    }

                    // Headers Action buttons
                    item {
                        Button(
                            onClick = { insertFormat("# ") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("H1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        Button(
                            onClick = { insertFormat("## ") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("H2", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        IconButton(
                            onClick = { insertFormat("• ") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Insert Bullet Point",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    item {
                        IconButton(
                            onClick = { insertFormat("1. ") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = "Insert Numbered List",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Colored tag markers divider
                    item {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                        )
                    }

                    // quick insert colors
                    item {
                        IconButton(
                            onClick = { insertFormat("<color=emerald>", "</color>") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorEmeraldNeon.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(ColorEmeraldNeon, CircleShape)
                            )
                        }
                    }

                    item {
                        IconButton(
                            onClick = { insertFormat("<color=amber>", "</color>") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorAmberWarning.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(ColorAmberWarning, CircleShape)
                            )
                        }
                    }

                    item {
                        IconButton(
                            onClick = { insertFormat("<color=red>", "</color>") },
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorCrimsonDanger.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(ColorCrimsonDanger, CircleShape)
                            )
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 12.dp))

            // Body Paragraph Workspaces
            if (editorMode == 0) {
                OutlinedTextField(
                    value = contentValue,
                    onValueChange = {
                        contentValue = it
                        viewModel.updateActiveNote(content = it.text)
                    },
                    placeholder = { Text("Start typing secure thoughts...", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("edit_content_input"),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            } else {
                // Rendered Preview Pane
                val parsedContent = parseRichTextToAnnotatedString(contentValue.text, MaterialTheme.colorScheme.onBackground)
                SelectionContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (contentValue.text.trim().isEmpty()) {
                        Text(
                            text = "No content to render. Start formatting thoughts on the Edit tab to see live preview updates here.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp, 
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        )
                    } else {
                        Text(
                            text = parsedContent,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }

            // Interactive Dynamic Word and Character Metrics Counter Bar
            val textToCount = contentValue.text
            val charCount = textToCount.length
            val wordCount = if (textToCount.isBlank()) 0 else textToCount.trim().split("\\s+".toRegex()).size
            val readingTime = (wordCount / 200).coerceAtLeast(1)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("editor_metrics_counter"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Word Count Icon",
                            tint = ColorEmeraldNeon,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$wordCount ${if (wordCount == 1) "word" else "words"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spellcheck,
                            contentDescription = "Character Count Icon",
                            tint = ColorEmeraldNeon,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$charCount ${if (charCount == 1) "char" else "chars"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Estimated Reading Time",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "~$readingTime min read",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            title = { Text("Destroy Document?", fontWeight = FontWeight.Bold) },
            text = { Text("This will decrypt and permanently shred the document from primary memory. It cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteActiveNote()
                        isDeleteDialogVisible = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ColorCrimsonDanger)
                ) {
                    Text("Confirm Destruction")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogVisible = false }) {
                    Text("Cancel")
                }
            },
            containerColor = ColorSlateCard,
            textContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    }

    if (isGeminiDialogOpen) {
        GeminiCopilotDialog(
            activeContent = contentValue.text,
            onDismiss = { isGeminiDialogOpen = false },
            onApply = { newResultText, insertionType ->
                val updatedText = when (insertionType) {
                    0 -> { // Insert at cursor
                        val text = contentValue.text
                        val selection = contentValue.selection
                        val start = selection.start
                        val end = selection.end
                        val base = text.substring(0, start) + newResultText + text.substring(end)
                        base
                    }
                    1 -> { // Append to note
                        if (contentValue.text.isNotEmpty()) {
                            contentValue.text + "\n\n" + newResultText
                        } else {
                            newResultText
                        }
                    }
                    else -> { // Replace entire note
                        newResultText
                    }
                }
                
                contentValue = TextFieldValue(
                    text = updatedText,
                    selection = TextRange(updatedText.length)
                )
                viewModel.updateActiveNote(content = updatedText)
                isGeminiDialogOpen = false
            }
        )
    }
}

@Composable
fun SecuritySettingsModal(
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    val biometricsActive by viewModel.biometricsEnabled.collectAsStateWithLifecycle()
    val selfDestructActive by viewModel.selfDestructEnabled.collectAsStateWithLifecycle()
    val overrideDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    var isWipeDialogVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSlateCard),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vault Config",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorEmeraldNeon
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close settings",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bio preference configuration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Biometrics icon",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Biometric Bypass",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Use fingerprint/face scan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Switch(
                        checked = biometricsActive,
                        onCheckedChange = { viewModel.setBiometricsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorEmeraldNeon,
                            checkedTrackColor = ColorEmeraldNeon.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("toggle_biometrics")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                // Direct Self-Destruct preference
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Self destruct icon",
                            tint = if (selfDestructActive) ColorAmberWarning else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Anti-Intrusion Wipe",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Wipe Database after 5 failures.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Switch(
                        checked = selfDestructActive,
                        onCheckedChange = { viewModel.setSelfDestructEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorAmberWarning,
                            checkedTrackColor = ColorAmberWarning.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("toggle_self_destruct")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                // Color Style Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = "Appearance icon",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Color Appearance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Switch appearance modes easily.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Box {
                        TextButton(
                            onClick = {
                                val next = when (overrideDarkMode) {
                                    true -> false
                                    false -> null
                                    null -> true
                                }
                                viewModel.toggleDarkMode(next)
                            }
                        ) {
                            Text(
                                text = when (overrideDarkMode) {
                                    true -> "FORCE DARK"
                                    false -> "FORCE LIGHT"
                                    null -> "SYSTEM DEFAULT"
                                },
                                fontSize = 11.sp,
                                color = ColorEmeraldNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                // Peaceful Aesthetic Theme Switcher block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = "Theme palette icon",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Aesthetic Theme",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Pick a relaxing, secure shade for your eyes.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    val activeTheme by viewModel.themeSelection.collectAsStateWithLifecycle()
                    val themes = listOf(
                        Triple("sage", "Sage Garden", Color(0xFF6B8F71)),
                        Triple("emerald", "Emerald Vault", Color(0xFF10B981)),
                        Triple("ocean", "Ocean Breeze", Color(0xFF0EA5E9)),
                        Triple("lavender", "Lavender", Color(0xFFB497FF))
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEach { (id, label, colorHex) ->
                            val isSelected = activeTheme == id
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colorHex.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) colorHex else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setThemeSelection(id) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(colorHex, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colorHex else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { isWipeDialogVisible = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorCrimsonDanger.copy(alpha = 0.15f), contentColor = ColorCrimsonDanger),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("wipe_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Wipe",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Factory Format Device",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (isWipeDialogVisible) {
        AlertDialog(
            onDismissRequest = { isWipeDialogVisible = false },
            title = { Text("Format Secure database?", fontWeight = FontWeight.Bold, color = ColorCrimsonDanger) },
            text = { Text("This is a severe option. Executing this choice instantly obliterates all encrypted entries and clears preferences offline. This choice is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleSignOutWipe()
                        isWipeDialogVisible = false
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ColorCrimsonDanger)
                ) {
                    Text("Execute Obliteration")
                }
            },
            dismissButton = {
                TextButton(onClick = { isWipeDialogVisible = false }) {
                    Text("Keep Safe")
                }
            },
            containerColor = ColorSlateCard,
            textContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    }
}

fun parseRichTextToAnnotatedString(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            val isH1 = line.startsWith("# ")
            val isH2 = line.startsWith("## ")
            val isBullet = line.startsWith("• ") || line.startsWith("* ") || line.startsWith("- ")
            val isNumList = line.trim().isNotEmpty() && line.first().isDigit() && line.contains(". ")

            val cleanLine = when {
                isH1 -> line.substring(2)
                isH2 -> line.substring(3)
                else -> line
            }

            val startIdx = this.length

            // Parse formatted content of this line recursively
            parseInlineFormatting(cleanLine, defaultColor)

            val endIdx = this.length

            // Apply block decoration
            if (isH1) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = ColorEmeraldNeon
                    ),
                    startIdx,
                    endIdx
                )
            } else if (isH2) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = ColorEmeraldNeon.copy(alpha = 0.9f)
                    ),
                    startIdx,
                    endIdx
                )
            } else if (isBullet) {
                addStyle(
                    SpanStyle(fontSize = 16.sp),
                    startIdx,
                    endIdx
                )
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, color = ColorEmeraldNeon),
                    startIdx,
                    minOf(startIdx + 2, endIdx)
                )
            } else if (isNumList) {
                // styled numbering prefix
                val dotIndex = cleanLine.indexOf(". ")
                if (dotIndex != -1) {
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, color = ColorEmeraldNeon),
                        startIdx,
                        minOf(startIdx + dotIndex + 2, endIdx)
                    )
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

fun AnnotatedString.Builder.parseInlineFormatting(line: String, defaultColor: Color) {
    var i = 0
    val n = line.length
    while (i < n) {
        if (i < n - 1 && line[i] == '*' && line[i+1] == '*') {
            val endBold = line.indexOf("**", i + 2)
            if (endBold != -1) {
                val boldContent = line.substring(i + 2, endBold)
                val start = this.length
                parseInlineFormatting(boldContent, defaultColor)
                val end = this.length
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                i = endBold + 2
                continue
            }
        }
        
        if (line[i] == '*') {
            val endItalic = line.indexOf('*', i + 1)
            if (endItalic != -1) {
                val italicContent = line.substring(i + 1, endItalic)
                val start = this.length
                parseInlineFormatting(italicContent, defaultColor)
                val end = this.length
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                i = endItalic + 1
                continue
            }
        }

        if (i < n - 2 && line.substring(i, minOf(i + 3, n)) == "<u>") {
            val endUnderline = line.indexOf("</u>", i + 3)
            if (endUnderline != -1) {
                val uContent = line.substring(i + 3, endUnderline)
                val start = this.length
                parseInlineFormatting(uContent, defaultColor)
                val end = this.length
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                i = endUnderline + 4
                continue
            }
        }

        // Color tag parser: <color=emerald>text</color>
        if (i < n - 6 && line.substring(i, minOf(i + 7, n)) == "<color=") {
            val closingBracket = line.indexOf('>', i + 7)
            if (closingBracket != -1) {
                val colorVal = line.substring(i + 7, closingBracket)
                val endColorTag = line.indexOf("</color>", closingBracket + 1)
                if (endColorTag != -1) {
                    val colorContent = line.substring(closingBracket + 1, endColorTag)
                    
                    val parsedColor = try {
                        if (colorVal.startsWith("#")) {
                            Color(android.graphics.Color.parseColor(colorVal))
                        } else {
                            when (colorVal.lowercase()) {
                                "emerald" -> ColorEmeraldNeon
                                "neon" -> ColorEmeraldNeon
                                "red" -> ColorCrimsonDanger
                                "amber" -> ColorAmberWarning
                                "slate" -> Color(0xFF64748B)
                                "blue" -> Color(0xFF3B82F6)
                                "gold" -> Color(0xFFFBBF24)
                                "purple" -> Color(0xFFA855F7)
                                else -> defaultColor
                            }
                        }
                    } catch (e: Exception) {
                        defaultColor
                    }

                    val start = this.length
                    parseInlineFormatting(colorContent, defaultColor)
                    val end = this.length
                    addStyle(SpanStyle(color = parsedColor), start, end)
                    
                    i = endColorTag + 8
                    continue
                }
            }
        }

        append(line[i])
        i++
    }
}

@Composable
fun GeminiCopilotDialog(
    activeContent: String,
    onDismiss: () -> Unit,
    onApply: (String, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val quickPrompts = remember {
        listOf(
            "Summarize this note" to "Summarize the active note details into logical, bulleted takeaway points with clear sub-headers.",
            "Improve and Proofread" to "Please proofread my note, polish the writing flow, correct any grammar or spelling mistakes, and make it crisp and engaging, maintaining its core message.",
            "Transform into a Checklist" to "Convert my note's details into a clean markdown checklist, categorizing tasks neatly with markdown bullet check boxes.",
            "Expand and Elaborate" to "Elaborate on the key concepts mentioned in my note, adding more helpful insights, details, and context structured beautifully with sub-headers."
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = ColorSlateCard,
            border = BorderStroke(1.dp, ColorEmeraldNeon.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Sparkle Icon",
                            tint = ColorEmeraldNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Safe Notes AI Copilot",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (resultText.isEmpty()) {
                    // Instruction Tagline
                    Text(
                        text = "Use Gemini 3.5 Flash to automatically rewrite, expand, or generate ideas for your secure note.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Suggestion Chips (Vertical/Flow Grid style)
                    Text(
                        text = "Quick Assistant Actions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEmeraldNeon,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPrompts.forEach { (label, promptAction) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (activeContent.isBlank() && (label != "Transform into a Checklist" && label != "Expand and Elaborate")) {
                                            Toast.makeText(context, "Note is empty. Please enter custom prompt below.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            prompt = label
                                            isLoading = true
                                            errorMessage = null
                                            scope.launch {
                                                try {
                                                    val combinedPrompt = if (activeContent.isNotBlank()) {
                                                        "Active Note Content:\n\"\"\"\n$activeContent\n\"\"\"\n\nInstruction: $promptAction"
                                                    } else {
                                                        promptAction
                                                    }
                                                    val systemPrompt = "You are a professional, sleek AI writing assistant integrated into Safe Notes, a secure and beautiful markdown notes editor. Generate content focusing on great spacing, structured formatting, logical sub-header segments, and standard markdown styles."
                                                    val response = com.example.data.GeminiClient.generate(combinedPrompt, systemPrompt)
                                                    resultText = response
                                                } catch (e: Exception) {
                                                    errorMessage = e.message ?: "An unknown error has occurred."
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Quick Action icon",
                                    tint = ColorEmeraldNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Or enter custom instruction / topic:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEmeraldNeon,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("What would you like to write or generate...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("gemini_custom_prompt_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorEmeraldNeon,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                if (prompt.isBlank()) {
                                    Toast.makeText(context, "Please enter a prompt first.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val combinedPrompt = if (activeContent.isNotBlank()) {
                                            "Active Note Content:\n\"\"\"\n$activeContent\n\"\"\"\n\nInstruction/Question: $prompt"
                                        } else {
                                            prompt
                                        }
                                        val systemPrompt = "You are a professional, sleek AI writing assistant integrated into Safe Notes, a secure and beautiful markdown notes editor. Generate content focusing on great spacing, structured formatting, logical sub-header segments, and standard markdown styles."
                                        val response = com.example.data.GeminiClient.generate(combinedPrompt, systemPrompt)
                                        resultText = response
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "An unknown error has occurred."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorEmeraldNeon,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("gemini_generate_button"),
                            enabled = !isLoading
                        ) {
                            Text("Generate", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Result Page
                    Text(
                        text = "Suggested Content:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEmeraldNeon,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 280.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = resultText,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dialog Apply Choices
                    Text(
                        text = "Choose action to execute on active draft:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Option Buttons:
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onApply(resultText, 0) }, // insert at cursor
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("apply_gemini_cursor"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorEmeraldNeon.copy(alpha = 0.15f),
                                    contentColor = ColorEmeraldNeon
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("At Cursor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onApply(resultText, 1) }, // append
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("apply_gemini_append"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorEmeraldNeon.copy(alpha = 0.15f),
                                    contentColor = ColorEmeraldNeon
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Append End", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onApply(resultText, 2) }, // replace all
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("apply_gemini_replace"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorCrimsonDanger.copy(alpha = 0.15f),
                                    contentColor = ColorCrimsonDanger
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Replace Entire", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Gemini Copilot Output", resultText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy raw text", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    resultText = ""
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Try Another", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Loading overlay state
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            color = ColorEmeraldNeon,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Formulating secure thoughts with Gemini...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorEmeraldNeon
                        )
                    }
                }

                // Error reporting block
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorCrimsonDanger.copy(alpha = 0.15f))
                            .border(0.5.dp, ColorCrimsonDanger.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error Icon",
                            tint = ColorCrimsonDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = error,
                            fontSize = 11.sp,
                            color = ColorCrimsonDanger,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
