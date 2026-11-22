package az.bucard.unit;

import az.bucard.utils.QrCodeDecoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Epic("Unit Tests")
@Feature("QR Code Decoding")
public class QrCodeDecoderTest {

    @Test(description = "Verify QrCodeDecoder decodes generated QR PNG payload accurately", groups = "unit")
    public void testDecodeGeneratedQrPng() throws Exception {
        String expectedUrl = "https://bucard.az/shared-user/pro_demo_user";
        BitMatrix matrix = new QRCodeWriter().encode(expectedUrl, BarcodeFormat.QR_CODE, 240, 240);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);

        String decoded = QrCodeDecoder.decodeFromBytes(baos.toByteArray());
        Assert.assertEquals(decoded, expectedUrl, "Decoded QR URL should match original payload");
    }

    @Test(description = "Verify QrCodeDecoder rejects null bytes", groups = "unit",
            expectedExceptions = IllegalArgumentException.class)
    public void testDecodeRejectsNullBytes() {
        QrCodeDecoder.decodeFromBytes(null);
    }

    @Test(description = "Verify QrCodeDecoder rejects non-image bytes", groups = "unit",
            expectedExceptions = IllegalArgumentException.class)
    public void testDecodeRejectsInvalidImageBytes() {
        QrCodeDecoder.decodeFromBytes("not-a-qr-image".getBytes(StandardCharsets.UTF_8));
    }
}
