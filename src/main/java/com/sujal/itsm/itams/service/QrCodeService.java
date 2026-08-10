package com.sujal.itsm.itams.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sujal.itsm.itams.model.Asset;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QrCodeService {

  @Value("${app.qr-code.directory:uploads/qrcodes}")
  private String qrCodeDirectory;

  @Value("${app.qr-code.size:300}")
  private int qrCodeSize;

  public String generateQrCode(Asset asset) throws IOException {
    // Create directory if it doesn't exist
    Path directory = Paths.get(qrCodeDirectory);
    if (!Files.exists(directory)) {
      Files.createDirectories(directory);
    }

    // Generate unique filename
    String filename = UUID.randomUUID() + ".png";
    Path filePath = directory.resolve(filename);

    // QR code content (asset tag + URL)
    String qrContent = String.format("ASSET:%s|ID:%d", asset.getAssetTag(), asset.getId());

    // Generate QR code
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      Map<EncodeHintType, Object> hintMap = new HashMap<>();
      hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      hintMap.put(EncodeHintType.MARGIN, 1);

      BitMatrix bitMatrix =
          qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
      MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

      log.info("Generated QR code for asset {}: {}", asset.getAssetTag(), filePath);
      return filePath.toString();
    } catch (Exception e) {
      log.error("Failed to generate QR code for asset: {}", asset.getAssetTag(), e);
      throw new IOException("Failed to generate QR code", e);
    }
  }

  public byte[] getQrCodeAsBytes(String qrCodeUrl) throws IOException {
    Path path = Paths.get(qrCodeUrl);
    return Files.readAllBytes(path);
  }
}
