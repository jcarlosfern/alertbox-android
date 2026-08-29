package app.alertbox.io.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

@Composable
fun AvatarCropDialog(bitmap: Bitmap, onDismiss: () -> Unit, onApply: (ByteArray, Bitmap) -> Unit) {
    var zoom by remember(bitmap) { mutableFloatStateOf(1f) }
    var imageOffset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    var frameSize by remember(bitmap) { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Ajustar foto", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Arrastra para encuadrar y usa el zoom.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged { frameSize = it.width.toFloat() }
                        .graphicsLayer { clip = true }
                        .pointerInputAvatarCrop(bitmap) { pan, gestureZoom ->
                            zoom = (zoom * gestureZoom).coerceIn(1f, 3f)
                            imageOffset += pan
                        },
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Vista previa de la foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = imageOffset.x
                                translationY = imageOffset.y
                            },
                    )
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val third = size.width / 3f
                        for (index in 1..2) {
                            drawLine(Color.White.copy(alpha = 0.45f), Offset(third * index, 0f), Offset(third * index, size.height), 1f)
                            drawLine(Color.White.copy(alpha = 0.45f), Offset(0f, third * index), Offset(size.width, third * index), 1f)
                        }
                    }
                }
                Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..3f)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                val ratio = 72.dp.toPx() / max(frameSize, 1f)
                                translationX = imageOffset.x * ratio
                                translationY = imageOffset.y * ratio
                            },
                        )
                    }
                    Text("Vista circular en la app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Recomendado: 600×600 px (mínimo 256×256 px). El resultado se guarda a 600×600 px en Cloudflare R2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    OutlinedButton(onClick = {
                        zoom = 1f
                        imageOffset = Offset.Zero
                    }) { Text("Restablecer") }
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        enabled = frameSize > 0f,
                        onClick = {
                            val cropped = cropAvatar(bitmap, frameSize, zoom, imageOffset)
                            val output = ByteArrayOutputStream()
                            cropped.compress(Bitmap.CompressFormat.JPEG, 92, output)
                            onApply(output.toByteArray(), cropped)
                        },
                    ) { Text("Aplicar") }
                }
            }
        }
    }
}

private fun Modifier.pointerInputAvatarCrop(
    key: Any,
    onTransform: (Offset, Float) -> Unit,
): Modifier = this.then(
    Modifier.pointerInput(key) {
        detectTransformGestures { _, pan, zoom, _ -> onTransform(pan, zoom) }
    },
)

private fun cropAvatar(bitmap: Bitmap, frameSize: Float, zoom: Float, offset: Offset): Bitmap {
    val outputSize = 600
    val result = createBitmap(outputSize, outputSize)
    val canvas = AndroidCanvas(result)
    canvas.drawColor(AndroidColor.WHITE)
    val cover = max(frameSize / bitmap.width, frameSize / bitmap.height)
    val outputScale = outputSize / frameSize
    val width = bitmap.width * cover * zoom * outputScale
    val height = bitmap.height * cover * zoom * outputScale
    val destination = RectF(
        outputSize / 2f + offset.x * outputScale - width / 2f,
        outputSize / 2f + offset.y * outputScale - height / 2f,
        outputSize / 2f + offset.x * outputScale + width / 2f,
        outputSize / 2f + offset.y * outputScale + height / 2f,
    )
    canvas.drawBitmap(bitmap, null, destination, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    return result
}

suspend fun loadAvatarBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
    } else {
        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
    } ?: return@withContext null
    val largest = max(decoded.width, decoded.height)
    if (largest <= 2048) decoded else {
        val scale = 2048f / largest
        decoded.scale((decoded.width * scale).toInt(), (decoded.height * scale).toInt())
    }
}
