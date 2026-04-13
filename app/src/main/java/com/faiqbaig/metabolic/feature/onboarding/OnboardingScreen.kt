package com.faiqbaig.metabolic.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.faiqbaig.metabolic.core.ui.theme.*
import com.faiqbaig.metabolic.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished : () -> Unit,
    viewModel  : AuthViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope      = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF0D2A20))
                )
            )
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Skip button ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onFinished()
                        }
                    ) {
                        Text(
                            text       = "Skip",
                            color      = MetabolicGreen.copy(alpha = 0.7f),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Pager ────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPage(page = onboardingPages[pageIndex])
            }

            // ── Page indicators ──────────────────────────────────
            Row(
                modifier              = Modifier.padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                onboardingPages.forEachIndexed { index, _ ->
                    PageIndicator(isSelected = index == pagerState.currentPage)
                }
            }

            // ── Next / Get Started button ─────────────────────────
            Button(
                onClick = {
                    if (isLastPage) {
                        viewModel.completeOnboarding()
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetabolicGreen,
                    contentColor   = DarkBackground
                )
            ) {
                Text(
                    text       = if (isLastPage) "Get Started" else "Next",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    val alpha = animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label         = "pageAlpha"
    )
    LaunchedEffect(page) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(alpha.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MetabolicGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = page.emoji, fontSize = 52.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text       = page.title,
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = DarkTextPrimary,
            textAlign  = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text       = page.description,
            fontSize   = 15.sp,
            color      = DarkTextSecondary,
            textAlign  = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PageIndicator(isSelected: Boolean) {
    val width = animateDpAsState(
        targetValue   = if (isSelected) 24.dp else 8.dp,
        animationSpec = tween(300),
        label         = "indicatorWidth"
    )
    val color = animateColorAsState(
        targetValue   = if (isSelected) MetabolicGreen else MetabolicGreen.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label         = "indicatorColor"
    )
    Box(
        modifier = Modifier
            .height(8.dp)
            .width(width.value)
            .clip(CircleShape)
            .background(color.value)
    )
}