package lottiefiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastSumBy
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.DotLottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Url
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.github.alexzhirkevich.shared.generated.resources.Res
import io.ktor.http.encodeURLPath
import lottiefiles.icons.ArrowBackIos
import lottiefiles.icons.ArrowForwardIos
import lottiefiles.icons.FileDownload
import lottiefiles.icons.OpenInNew
import lottiefiles.icons.Sort
import lottiefiles.theme.LottieFilesTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.abs

@Composable
internal fun LottieFilesExample(
    viewModel: LottieFilesViewModel = viewModel { LottieFilesViewModel() }
) {
    LottieFilesTheme {
        DisposableEffect(0) {
            val l = Compottie.logger
            Compottie.compositionCacheLimit = 20 // page
            Compottie.logger = null
            onDispose {
                Compottie.logger = l
            }
        }

        val selectedFile = viewModel.selectedFile.collectAsState().value

        if (selectedFile != null) {
            Dialog(
                onDismissRequest = {
                    viewModel.onFileSelected(null)
                }
            ) {
                LottieDetails(
                    modifier = Modifier
                        .padding(vertical = 12.dp),
                    file = selectedFile,
                    onTagClicked = {
                        viewModel.onFileSelected(null)
                        viewModel.onSearch(it)
                    },
                    onDismiss = {
                        viewModel.onFileSelected(null)
                    }
                )
            }
        }

        val files by viewModel.files.collectAsState()
        val pageCount by viewModel.pageCount.collectAsState()
        val gridState = rememberLazyGridState()

        val keyboard = LocalSoftwareKeyboardController.current
        LaunchedEffect(gridState.isScrollInProgress){
            if (gridState.isScrollInProgress){
                keyboard?.hide()
            }
        }

        LaunchedEffect(files) {
            gridState.animateScrollToItem(0)
        }

        Surface() {
            BoxWithConstraints {

                val isWideScreen = constraints.maxWidth > LocalDensity.current.run {
                    400.dp.toPx()
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        SearchBar(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 8.dp),
                            viewModel = viewModel
                        )

                        var sortExpanded by rememberSaveable {
                            mutableStateOf(false)
                        }

                        val sort by viewModel.sortOrder.collectAsState()

                        Box() {
                            AssistChip(
                                onClick = {
                                    sortExpanded = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (isWideScreen) sort.name else sort.name.take(1),
                                        maxLines = 1,
                                        lineHeight = LocalTextStyle.current.fontSize,
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = {
                                    sortExpanded = false
                                }
                            ) {
                                SortOrder.entries.forEach {
                                    DropdownMenuItem(
                                        leadingIcon = if (it == sort) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Done,
                                                    contentDescription = "Selected"
                                                )
                                            }
                                        } else null,
                                        text = {
                                            Text(
                                                text = it.name,
                                                maxLines = 1,
                                                lineHeight = LocalTextStyle.current.fontSize,
                                            )
                                        },
                                        onClick = {
                                            sortExpanded = false
                                            viewModel.onSortOrderChanged(it)
                                        }
                                    )
                                }
                            }
                        }
                    }


                    when {
                        files.isEmpty() -> {
                            Landing(Modifier.fillMaxSize())
                        }

                        else -> {
                            HorizontalDivider()

                            LazyVerticalGrid(
                                state = gridState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(24.dp),
                                columns = GridCells.Adaptive(200.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                items(
                                    items = files,
                                    key = LottieFile::id
                                ) {
                                    LottieCard(
                                        file = it,
                                        visible = selectedFile != it,
                                        onClick = { viewModel.onFileSelected(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }


                            AnimatedVisibility(
                                visible = pageCount > 1,
                                enter = slideInVertically { it } + expandVertically(),
                                exit = slideOutVertically { it } + shrinkVertically()
                            ) {
                                HorizontalDivider()
                                PageSelector(
                                    page = viewModel.page.collectAsState().value,
                                    pageCount = pageCount,
                                    onPageSelected = {
                                        viewModel.onPageSelected(it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(
                                            horizontal = 24.dp,
                                            vertical = 12.dp
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    viewModel: LottieFilesViewModel
) {
    var searchBarActive by remember {
        mutableStateOf(false)
    }

    val query by viewModel.search.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    SearchBar(
        modifier = modifier,
        query = query,
        onQueryChange = viewModel::onSearch,
        onSearch = viewModel::onSearch,
        active = false,//searchBarActive && query.isNotBlank() && suggestions.isNotEmpty(),
        placeholder = {
            Text("Search...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    viewModel.onSearch("")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear"
                )
            }
        },
        onActiveChange = {
            searchBarActive = it
        }
    ) {
        suggestions.forEach {
            Text(
                text = it.query,
                modifier = Modifier.clickable {
                    viewModel.onSearch(it.query)
                }
            )
        }
    }
}

private val PageSelectorSize = 36.dp
private val LandingAnimDuration = 500
private val LandingAnimSlideFactor = 8

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun Landing(modifier: Modifier = Modifier) {

    val composition by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            archive = Res.readBytes("files/dotlottie/lottiefiles.lottie")
        )
    }

    with(LocalDensity.current) {
        BoxWithConstraints {
            if (constraints.maxWidth > 1.5 * constraints.maxHeight) {
                val fontScale = (constraints.maxHeight / 500.dp.toPx()).coerceAtMost(1f)
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = composition != null,
                            enter = fadeIn(tween(LandingAnimDuration)) +
                                    slideInHorizontally(tween(LandingAnimDuration)) { -it / LandingAnimSlideFactor }
                        ) {
                            LandingText(
                                modifier = Modifier
                                    .widthIn(max = 550.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 42.dp),
                                textAlign = TextAlign.Start,
                                leadingTextStyle = MaterialTheme.typography.displayLarge,
                                descriptionTextStyle = MaterialTheme.typography.bodyLarge,
                                fontScale = fontScale
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            modifier = Modifier.fillMaxSize(),
                            visible = composition != null,
                            enter = fadeIn(tween(LandingAnimDuration)) +
                                    slideInHorizontally(tween(LandingAnimDuration)) { it / LandingAnimSlideFactor }
                        ) {
                            LandingAnimation(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = this@BoxWithConstraints.constraints.maxHeight.toDp()/5),
                                composition = composition,
                            )
                        }
                    }
                }
            } else {
                val fontScale = (constraints.maxHeight / 800.dp.toPx()).coerceIn(.75f, 1f)
                val animWeight =
                    ((1.25f * constraints.maxWidth) / constraints.maxHeight).coerceAtMost(1f)

                val showAnim = constraints.maxHeight > 500.dp.toPx()
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showAnim) {
                        androidx.compose.animation.AnimatedVisibility(
                            modifier = Modifier.weight(animWeight),
                            visible = composition != null,
                            enter = fadeIn(tween(LandingAnimDuration)) +
                                    slideInVertically(tween(LandingAnimDuration)) { -it / LandingAnimSlideFactor }
                        ) {
                            LandingAnimation(
                                modifier = Modifier.fillMaxSize(),
                                composition = composition,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .fillMaxSize(),
                            visible = !showAnim || composition != null,
                            enter = fadeIn(tween(LandingAnimDuration)) +
                                    slideInVertically(tween(LandingAnimSlideFactor)) { it * 2 / LandingAnimSlideFactor }
                        ) {
                            LandingText(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                leadingTextStyle = MaterialTheme.typography.displayMedium,
                                descriptionTextStyle = MaterialTheme.typography.bodyLarge,
                                fontScale = if (showAnim) fontScale else 1f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandingAnimation(
    modifier: Modifier = Modifier,
    composition: LottieComposition?,
) {
    Image(
        modifier = modifier,
        painter = rememberLottiePainter(
            composition = composition,
            iterations = Compottie.IterateForever
        ),
        contentDescription = "LottieFiles animation"
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun LandingText(
    modifier: Modifier = Modifier,
    leadingTextStyle: TextStyle,
    descriptionTextStyle: TextStyle,
    textAlign: TextAlign,
    fontScale : Float
) {

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.Center)
            Alignment.CenterHorizontally
        else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Compottie example made with LottieFiles",
            style = leadingTextStyle.copy(
                fontSize = leadingTextStyle.fontSize * fontScale,
                lineHeight = leadingTextStyle.lineHeight * fontScale
            ),
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = textAlign
        )
        Text(
            text = "Here you can search for free Lottie animations, test them with Compottie renderer and download to use in your Compose Multiplatform app",
            style = descriptionTextStyle.copy(
                fontSize = descriptionTextStyle.fontSize * fontScale,
                lineHeight = descriptionTextStyle.lineHeight * fontScale
            ),
            textAlign = textAlign,
        )

        val coercedFontScale = fontScale.coerceIn(.75f, 1f)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    uriHandler.openUri("https://lottiefiles.com")
                },
                contentPadding = if (textAlign == TextAlign.Center) {
                    ButtonDefaults.ContentPadding
                } else {
                    PaddingValues(38.dp * coercedFontScale, 16.dp * coercedFontScale)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "LottieFiles.com",
                    style = if (textAlign == TextAlign.Center) {
                        LocalTextStyle.current
                    } else {
                        LocalTextStyle.current
                            .copy(fontSize = descriptionTextStyle.fontSize * coercedFontScale)
                    },
                    maxLines = 1,
                    lineHeight = LocalTextStyle.current.fontSize,
                )
            }

            Spacer(Modifier.width(18.dp))

            GithubButton()
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun GithubButton(modifier: Modifier = Modifier) {

    val uriHandler = LocalUriHandler.current
    val github by rememberLottieComposition {
        LottieCompositionSpec.DotLottie(
            Res.readBytes("files/dotlottie/github.lottie")
        )
    }

    Icon(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable {
                uriHandler.openUri("https://github.com/alexzhirkevich/compottie")
            }
            .padding(4.dp),
        painter = rememberLottiePainter(
            composition = github,
            iterations = Compottie.IterateForever
        ),
        tint = MaterialTheme.colorScheme.secondary,
        contentDescription = "Github"
    )
}

@Composable
private fun PageSelector(
    page : Int,
    pageCount : Int,
    onPageSelected : (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val pageSizePx = remember(density) {
        with(density) {
            PageSelectorSize.toPx()
        }
    }


    SubcomposeLayout(modifier) { constraints ->

        val spaceLeft = (constraints.maxWidth / pageSizePx).toInt().coerceAtMost(11)

        val items = buildList {
            addAll((1..pageCount).map { SelectorButton.Page(it) })
            if (page > 1) {
                add(0, SelectorButton.Backward)
            }
            if (page < pageCount){
                add(SelectorButton.Forward)
            }
        }.toMutableList()

        val indexOfFirst =  { if (page > 1) 2 else 1 }
        val indexOfLast = { if (page < pageCount) items.size - 3 else items.size - 2 }

        while (items.size > spaceLeft) {

            val start = if (page == 1){
                0
            } else {
                (items[indexOfFirst()] as SelectorButton.Page).i
            }
            val end = if (page == pageCount){
                0
            } else {
                (items[indexOfLast()] as SelectorButton.Page).i
            }

            if (abs(start-page) > abs(end - page)){
                items.removeAt(indexOfFirst())
            } else {
                items.removeAt(indexOfLast())
            }
        }

        if (items.size == spaceLeft) {

            if (spaceLeft > 7 && (items[indexOfFirst()] as? SelectorButton.Page)?.i != 2) {
                items[indexOfFirst()] = SelectorButton.Dots
            }
            if (spaceLeft > 8 && (items[indexOfLast()] as? SelectorButton.Page)?.i != pageCount - 1) {
                items[indexOfLast()] = SelectorButton.Dots
            }
        }

        val measurables : List<Measurable> = subcompose(null) {
            items.fastMap {
                when (it) {
                    SelectorButton.Backward -> PageButton(
                        onClick = { onPageSelected(page - 1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIos,
                            contentDescription = "back"
                        )
                    }

                    SelectorButton.Dots -> PageButton() {
                        Text(
                            text = "...",
                            maxLines = 1,
                            lineHeight = LocalTextStyle.current.fontSize,
                        )
                    }

                    SelectorButton.Forward -> PageButton(
                        onClick = { onPageSelected(page + 1) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = "back"
                        )
                    }

                    is SelectorButton.Page -> PageButton(
                        selected = it.i == page,
                        onClick = { onPageSelected(it.i) }
                    ) {
                        Text(
                            text = it.i.toString(),
                            maxLines = 1,
                            lineHeight = LocalTextStyle.current.fontSize,
                        )
                    }
                }
            }
        }

        val buttonConstraints = Constraints.fixed(pageSizePx.toInt(), pageSizePx.toInt())
        val placeables = measurables.fastMap {
            it.measure(buttonConstraints)
        }

        layout(placeables.fastSumBy { it.width }, pageSizePx.toInt()) {
            var x = 0

            placeables.forEach {
                it.place(x, 0)
                x += it.width
            }
        }
    }
}

private sealed interface SelectorButton {
    object Dots : SelectorButton
    object Forward : SelectorButton
    object Backward : SelectorButton
    class Page(val i : Int): SelectorButton
}

@Composable
private fun PageButton(
    selected : Boolean = false,
    onClick: (() -> Unit)? = null,
    content : @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .size(PageSelectorSize)
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            LocalTextStyle.current.copy(
                lineHeight = LocalTextStyle.current.lineHeight,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onBackground
            )
        ) {
            content()
        }
    }
}

@Composable
private fun LottieCard(
    file : LottieFile,
    visible : Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.Url(file.lottieSource ?: file.jsonSource ?: "")
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                onClick = onClick,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = file.bgColor?.let(::parseColorValue)
//                        ?.takeUnless { it == Color.White }
                        ?: MaterialTheme.colorScheme.surface
                )
            ) {
                AnimatedVisibility(
                    visible = composition != null && visible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = rememberLottiePainter(
                            composition = composition,
                            iterations = Compottie.IterateForever
                        ),
                        contentDescription = file.name
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                UserAvatar(
                    user = file.user,
                    size = 28.dp
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = file.user.name.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(16.dp))

                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        modifier = Modifier.size(18.dp),
                        contentDescription = "Downloads count"
                    )
                    Text(
                        text = file.downloadCount.let {
                            if (it < 1000) {
                                it.toString()
                            } else {
                                val s = (it / 1000f).toString()
                                s.substringBefore(".") + "." + s.substringAfter(".").take(2) + "k"
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun UserAvatar(
    user: User,
    size : Dp
) {
    val placeholder = rememberVectorPainter(Icons.Default.Person)

    val uriHandler = LocalUriHandler.current

    AsyncImage(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                enabled = user.username != null,
                onClick = {
                    uriHandler.openUri(
                        "https://lottiefiles.com/${user.username.orEmpty()}"
                            .encodeURLPath()
                    )
                }
            ),
        model = user.avatarUrl,
        contentDescription = user.name,
        placeholder = placeholder,
        error = placeholder
    )
}




private const val ALPHA_MASK = 0xFF000000.toInt()

internal fun parseColorValue(color: String): Color {

    val hex = color.lowercase().trimStart('#')

    return when (hex.length) {
        6 -> {
            // #RRGGBB
            hex.toUInt(16).toInt() or ALPHA_MASK
        }
        8 -> {
            // #AARRGGBB
            hex.toUInt(16).toInt()
        }
        3 -> {
            // #RGB
            val v = hex.toUInt(16).toInt()
            var k = (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        4 -> {
            // #ARGB
            val v = hex.toUInt(16).toInt()
            var k = (v shr 12 and 0xF) * 0x11000000
            k = k or (v shr 8 and 0xF) * 0x110000
            k = k or (v shr 4 and 0xF) * 0x1100
            k = k or (v and 0xF) * 0x11
            k or ALPHA_MASK
        }
        else -> ALPHA_MASK
    }.let { Color(it) }
}
