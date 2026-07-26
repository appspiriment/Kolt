package io.github.appspiriment.kolt.locationpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image as SkiaImage
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

private const val TILE_SIZE = 256
private val mapTileClient = HttpClient()

// Web-Mercator <-> pixel-space conversions, in "world pixels" at the given zoom level
// (the standard slippy-map tile projection: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames).
private fun lonToWorldX(lon: Double, zoom: Int): Double =
    (lon + 180.0) / 360.0 * TILE_SIZE * (1 shl zoom)

private fun latToWorldY(lat: Double, zoom: Int): Double {
    val latRad = lat * PI / 180.0
    return (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * TILE_SIZE * (1 shl zoom)
}

private fun worldXToLon(x: Double, zoom: Int): Double =
    x / (TILE_SIZE * (1 shl zoom)) * 360.0 - 180.0

private fun worldYToLat(y: Double, zoom: Int): Double {
    val n = PI - 2.0 * PI * y / (TILE_SIZE * (1 shl zoom))
    return 180.0 / PI * atan(0.5 * (exp(n) - exp(-n)))
}

@Composable
actual fun MapPickerContent(
    modifier: Modifier,
    initialLatitude: Double,
    initialLongitude: Double,
    onPicked: (PickedMapLocation) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var zoom by remember { mutableStateOf(5) }
    var centerLat by remember { mutableStateOf(initialLatitude) }
    var centerLon by remember { mutableStateOf(initialLongitude) }
    var pinLat by remember { mutableStateOf(initialLatitude) }
    var pinLon by remember { mutableStateOf(initialLongitude) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var loadingCount by remember { mutableStateOf(0) }
    var isResolvingLabel by remember { mutableStateOf(false) }
    val tileBitmaps = remember { mutableStateMapOf<String, ImageBitmap>() }
    val pendingTiles = remember { mutableStateOf(mutableSetOf<String>()) }

    suspend fun ensureTilesLoaded(z: Int, originX: Double, originY: Double, width: Int, height: Int) {
        val n = 1 shl z
        val firstTileX = floor(originX / TILE_SIZE).toInt()
        val firstTileY = floor(originY / TILE_SIZE).toInt()
        val tilesAcross = ceil(width.toFloat() / TILE_SIZE).toInt() + 2
        val tilesDown = ceil(height.toFloat() / TILE_SIZE).toInt() + 2

        for (tx in firstTileX..(firstTileX + tilesAcross)) {
            for (ty in firstTileY..(firstTileY + tilesDown)) {
                if (ty < 0 || ty >= n) continue
                val wrappedX = ((tx % n) + n) % n
                val key = "$z/$wrappedX/$ty"
                if (tileBitmaps.containsKey(key) || pendingTiles.value.contains(key)) continue
                pendingTiles.value.add(key)
                scope.launch {
                    loadingCount++
                    try {
                        val bytes = mapTileClient.get("https://tile.openstreetmap.org/$z/$wrappedX/$ty.png") {
                            header("User-Agent", "Kolt-LocationPicker/1.0 (+location picker; no contact set)")
                        }.bodyAsBytes()
                        tileBitmaps[key] = SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
                    } catch (e: Exception) {
                        // Leave the tile blank on failure (rate limit, offline, etc.) — the
                        // user can still drop a pin from the surrounding loaded tiles.
                    } finally {
                        pendingTiles.value.remove(key)
                        loadingCount--
                    }
                }
            }
        }
    }

    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFDDDDDD))
                .pointerInput(zoom) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        centerLon = worldXToLon(lonToWorldX(centerLon, zoom) - dragAmount.x, zoom)
                        centerLat = worldYToLat(latToWorldY(centerLat, zoom) - dragAmount.y, zoom)
                    }
                }
                .pointerInput(zoom, centerLat, centerLon, canvasSize) {
                    detectTapGestures { tapOffset ->
                        val originX = lonToWorldX(centerLon, zoom) - canvasSize.width / 2.0
                        val originY = latToWorldY(centerLat, zoom) - canvasSize.height / 2.0
                        val tappedLon = worldXToLon(originX + tapOffset.x, zoom)
                        val tappedLat = worldYToLat(originY + tapOffset.y, zoom)
                        pinLon = tappedLon
                        pinLat = tappedLat
                        scope.launch {
                            isResolvingLabel = true
                            onPicked(resolveMapPick(tappedLat, tappedLon))
                            isResolvingLabel = false
                        }
                    }
                }
        ) {
            LaunchedTileLoader(
                zoom = zoom, centerLat = centerLat, centerLon = centerLon, canvasSize = canvasSize,
                ensureTilesLoaded = ::ensureTilesLoaded
            )

            Canvas(Modifier.fillMaxSize().onSizeChanged { canvasSize = it }) {
                val originX = lonToWorldX(centerLon, zoom) - size.width / 2.0
                val originY = latToWorldY(centerLat, zoom) - size.height / 2.0
                val firstTileX = floor(originX / TILE_SIZE).toInt()
                val firstTileY = floor(originY / TILE_SIZE).toInt()
                val tilesAcross = ceil(size.width / TILE_SIZE).toInt() + 2
                val tilesDown = ceil(size.height / TILE_SIZE).toInt() + 2
                val n = 1 shl zoom

                for (tx in firstTileX..(firstTileX + tilesAcross)) {
                    for (ty in firstTileY..(firstTileY + tilesDown)) {
                        if (ty < 0 || ty >= n) continue
                        val wrappedX = ((tx % n) + n) % n
                        val bitmap = tileBitmaps["$zoom/$wrappedX/$ty"] ?: continue
                        val screenX = (tx * TILE_SIZE - originX).toFloat()
                        val screenY = (ty * TILE_SIZE - originY).toFloat()
                        drawImage(bitmap, topLeft = Offset(screenX, screenY))
                    }
                }

                val pinScreenX = (lonToWorldX(pinLon, zoom) - originX).toFloat()
                val pinScreenY = (latToWorldY(pinLat, zoom) - originY).toFloat()
                drawCircle(Color(0xFFF0A500), radius = 9f, center = Offset(pinScreenX, pinScreenY))
                drawCircle(
                    Color(0xFF08080F), radius = 9f, center = Offset(pinScreenX, pinScreenY),
                    style = Stroke(width = 2f)
                )
            }

            if (loadingCount > 0) {
                CircularProgressIndicator(
                    Modifier.align(Alignment.TopEnd).padding(10.dp).size(18.dp),
                    strokeWidth = 2.dp
                )
            }

            Row(
                Modifier.align(Alignment.BottomEnd).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZoomButton("−") { zoom = (zoom - 1).coerceIn(2, 18) }
                ZoomButton("+") { zoom = (zoom + 1).coerceIn(2, 18) }
            }

            Text(
                "© OpenStreetMap contributors",
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${formatCoord(pinLat)}, ${formatCoord(pinLon)}")
            if (isResolvingLabel) {
                CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
            }
        }
    }
}

// Kicks off tile loads for the currently visible region whenever the viewport changes.
// Side effects (network calls) live here rather than in the Canvas draw block, which must
// stay pure since it can run multiple times per frame.
@Composable
private fun LaunchedTileLoader(
    zoom: Int,
    centerLat: Double,
    centerLon: Double,
    canvasSize: IntSize,
    ensureTilesLoaded: suspend (Int, Double, Double, Int, Int) -> Unit,
) {
    LaunchedEffect(zoom, centerLat, centerLon, canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@LaunchedEffect
        val originX = lonToWorldX(centerLon, zoom) - canvasSize.width / 2.0
        val originY = latToWorldY(centerLat, zoom) - canvasSize.height / 2.0
        ensureTilesLoaded(zoom, originX, originY, canvasSize.width, canvasSize.height)
    }
}

private fun formatCoord(value: Double): String {
    val rounded = kotlin.math.round(value * 100000.0) / 100000.0
    return rounded.toString()
}

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFFFFF))
            .border(0.5.dp, Color(0xFFAAAAAA), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label)
    }
}
