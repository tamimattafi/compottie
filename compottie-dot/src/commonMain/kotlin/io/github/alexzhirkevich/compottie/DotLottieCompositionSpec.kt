package io.github.alexzhirkevich.compottie

import androidx.compose.runtime.Stable
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem



private var _useStableWasmMemoryManagement : Boolean = false

/**
 * Stable memory management will be much slower but more compatible with Kotlin compiler
 * versions.
 *
 * It is disabled by default. Turn this on if you have problems with dotLottie decompression on wasm
 * */
@ExperimentalCompottieApi
var L.useStableWasmMemoryManagement by ::_useStableWasmMemoryManagement

/**
 * [LottieComposition] from a dotLottie zip archive.
 *
 * @param archive dotLottie or zip archive file
 * @param animId animation id (if dotLottie contains multiple animations)
 * */
@Stable
fun LottieCompositionSpec.Companion.DotLottie(
    archive: ByteArray,
    animId: String? = null
) : LottieCompositionSpec = DotLottieCompositionSpec(archive, animId)


@OptIn(ExperimentalSerializationApi::class)
private val DotLottieJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowTrailingComma = true
}

private class DotLottieCompositionSpec(
    private val archive : ByteArray,
    private val animationId : String?,
) : LottieCompositionSpec {

    @OptIn(InternalCompottieApi::class)
    override suspend fun load(cacheKey : Any?): LottieComposition {
        return withContext(ioDispatcher()) {
            LottieComposition.getOrCreate(cacheKey) {
                val fileSystem = FakeFileSystem()
                val path = "anim".toPath()

                fileSystem.write(path) {
                    write(archive)
                }

                val entries = fileSystem.listZipEntries(path)

                val zipSystem = ZipFileSystem(fileSystem, entries, path)

                val manifestPath = entries.keys.firstOrNull { it.name == "manifest.json" }

                if (manifestPath != null) {


                    val manifest = DotLottieJson.decodeFromString<DotLottieManifest>(
                        zipSystem.read(manifestPath).decodeToString()
                    )

                    val animation = checkNotNull(manifest.animations.firstOrNull()){
                        "dotLottie animation folder is empty"
                    }

                    val anim = zipSystem.read("animations/${animationId ?: animation.id}.json".toPath())

                    LottieComposition.parse(anim.decodeToString()).apply {
                        speed = animation.speed
                        if (animation.loop) {
                            iterations = LottieConstants.IterateForever
                        }
                        prepareAssets(DotLottieAssetsManager(zipSystem, manifestPath.parent))
                    }
                } else {
                    val animPath = entries.keys.first { it.name.endsWith(".json", true) }
                    val anim = zipSystem.read(animPath)

                    LottieComposition.parse(anim.decodeToString()).apply {
                        prepareAssets(
                            DotLottieAssetsManager(
                                zipFileSystem = zipSystem,
                                root = animPath.parent
                            )
                        )
                    }
                }
            }
        }
    }

    override fun hashCode(): Int {
        return 31 * archive.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? DotLottieCompositionSpec)?.let {
            it.archive.contentEquals(archive)
        } == true
    }
}
