package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RepeatState
import com.example.data.model.Song
import com.example.ui.components.SalimIcons
import com.example.ui.components.SquircleArtwork
import com.example.ui.theme.LocalAdaptiveAccent
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SalimBlack
import com.example.ui.theme.SalimNeutralAccent
import com.example.ui.theme.SalimSurface1
import com.example.ui.theme.SquircleShape
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatState: RepeatState,
    isShuffle: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current
    val accent = LocalAdaptiveAccent.current

    // Scrubbing state
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPositionFraction by remember { mutableFloatStateOf(0f) }

    val actualFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayFraction = if (isScrubbing) scrubPositionFraction else actualFraction
    val displayedMs = (displayFraction * durationMs).toLong()

    // Artwork horizontal swipe-to-skip with spring rubber-banding
    val dragOffsetX = remember { Animatable(0f) }

    // Physical album art scale bounce on play/pause
    val artworkScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "artwork_scale"
    )

    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                accent.copy(alpha = 0.22f),
                SalimSurface1.copy(alpha = 0.85f),
                SalimBlack
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                accent.copy(alpha = 0.15f),
                Color(0xFFF2F2F7),
                Color(0xFFFFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: dismiss handle & screen title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCollapse()
                    },
                    modifier = Modifier.size(44.dp).testTag("now_playing_collapse")
                ) {
                    SalimIcons.ChevronDown(size = 24.dp, tint = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAddToPlaylist()
                    },
                    modifier = Modifier.size(44.dp).testTag("now_playing_add_playlist")
                ) {
                    SalimIcons.Plus(size = 22.dp, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Large embedded album art with swipe-to-skip and spring rubber-banding
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val artSize = minOf(maxWidth * 0.88f, 320.dp)

                // Ambient glow behind artwork
                Box(
                    modifier = Modifier
                        .size(artSize)
                        .scale(artworkScale * 1.05f)
                        .blur(36.dp)
                        .background(accent.copy(alpha = 0.35f), shape = SquircleShape(40.dp))
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                        .scale(artworkScale)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                coroutineScope.launch {
                                    // Rubber-band resistance if at end of queue
                                    val resistance = if ((delta > 0 && !hasPrevious) || (delta < 0 && !hasNext)) 0.35f else 1.0f
                                    dragOffsetX.snapTo(dragOffsetX.value + delta * resistance)
                                }
                            },
                            onDragStopped = { velocity ->
                                val threshold = 180f
                                if (dragOffsetX.value > threshold || velocity > 800f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSkipPrevious()
                                } else if (dragOffsetX.value < -threshold || velocity < -800f) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSkipNext()
                                }
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        )
                        .testTag("now_playing_art")
                ) {
                    SquircleArtwork(
                        uri = song.albumArtUri,
                        size = artSize,
                        cornerRadius = (artSize.value * 0.16f).dp,
                        elevation = 18.dp
                    )
                }
            }

            // Typography hierarchy: Track title carries the primary fact
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("now_playing_title")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${song.artist} — ${song.album}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scrubbable Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = displayFraction,
                    onValueChange = { frac ->
                        isScrubbing = true
                        scrubPositionFraction = frac
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        onSeekTo((scrubPositionFraction * durationMs).toLong())
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = accent,
                        inactiveTrackColor = if (isDark) Color(0x28FFFFFF) else Color(0x1F000000)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("now_playing_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(displayedMs),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-${formatMs((durationMs - displayedMs).coerceAtLeast(0L))}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary Playback Controls: Prev, Play/Pause, Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleShuffle()
                    },
                    modifier = Modifier.size(48.dp).testTag("shuffle_button")
                ) {
                    SalimIcons.Shuffle(
                        size = 22.dp,
                        tint = if (isShuffle) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSkipPrevious()
                    },
                    modifier = Modifier.size(54.dp).testTag("skip_prev_button")
                ) {
                    SalimIcons.SkipPrevious(size = 28.dp, tint = MaterialTheme.colorScheme.onSurface)
                }

                // Big Physical Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(12.dp, SquircleShape(26.dp), ambientColor = Color.Black.copy(alpha = 0.3f))
                        .background(MaterialTheme.colorScheme.onSurface, shape = SquircleShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePlayPause()
                        },
                        modifier = Modifier.size(72.dp).testTag("now_playing_play_pause")
                    ) {
                        val iconTint = MaterialTheme.colorScheme.surface
                        if (isPlaying) {
                            SalimIcons.Pause(size = 30.dp, tint = iconTint)
                        } else {
                            SalimIcons.Play(size = 30.dp, tint = iconTint)
                        }
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSkipNext()
                    },
                    modifier = Modifier.size(54.dp).testTag("skip_next_button")
                ) {
                    SalimIcons.SkipNext(size = 28.dp, tint = MaterialTheme.colorScheme.onSurface)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleRepeat()
                    },
                    modifier = Modifier.size(48.dp).testTag("repeat_button")
                ) {
                    SalimIcons.Repeat(
                        size = 22.dp,
                        tint = if (repeatState != RepeatState.OFF) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        isOne = repeatState == RepeatState.ONE
                    )
                }
            }

            // Secondary functional toolstrip: Equalizer, Timer, Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenEqualizer()
                    },
                    modifier = Modifier.size(44.dp).testTag("equalizer_button")
                ) {
                    SalimIcons.Equalizer(size = 20.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSleepTimer()
                    },
                    modifier = Modifier.size(44.dp).testTag("sleep_timer_button")
                ) {
                    SalimIcons.Timer(size = 20.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenQueue()
                    },
                    modifier = Modifier.size(44.dp).testTag("queue_button")
                ) {
                    SalimIcons.Queue(size = 20.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
