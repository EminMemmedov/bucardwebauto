package az.bucard.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes QR code images (PNG/JPG bytes, URLs, or rendered WebElements) using ZXing.
 */
public final class QrCodeDecoder {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private QrCodeDecoder() {}

    /**
     * Decodes QR payload text/URL from raw image bytes.
     */
    public static String decodeFromBytes(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("QR image bytes must not be null or empty");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new IllegalArgumentException("Unable to parse image bytes as a supported raster image");
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));

            Result result = new MultiFormatReader().decode(bitmap, hints);
            if (result == null || result.getText() == null || result.getText().isBlank()) {
                throw new IllegalArgumentException("Decoded QR code contained empty payload");
            }
            return result.getText().trim();
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("No QR code found in the provided image", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read QR image bytes", e);
        }
    }

    /**
     * Downloads a QR image from an HTTP(S) or Base64 data URL and decodes its payload.
     */
    public static String decodeFromImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("QR image URL must not be blank");
        }
        String trimmed = imageUrl.trim();
        if (trimmed.startsWith("data:") && trimmed.contains(",")) {
            String base64Part = trimmed.substring(trimmed.indexOf(',') + 1);
            return decodeFromBytes(Base64.getDecoder().decode(base64Part));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimmed))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 (BucardWebAuto QR Decoder)")
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException(
                        "Failed to download QR image (HTTP " + response.statusCode() + "): " + trimmed);
            }
            return decodeFromBytes(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while downloading QR image: " + trimmed, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("I/O error downloading QR image: " + trimmed, e);
        }
    }

    /**
     * Captures a screenshot of a rendered QR code element and decodes its payload.
     */
    public static String decodeFromElement(WebElement qrElement) {
        if (qrElement == null) {
            throw new IllegalArgumentException("QR element must not be null");
        }
        byte[] bytes = qrElement.getScreenshotAs(OutputType.BYTES);
        return decodeFromBytes(bytes);
    }
}
