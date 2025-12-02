package com.personalfin.server.receipt.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class ReceiptScannerService {

    private final Tesseract tesseract;

    public ReceiptScannerService() {
        this.tesseract = new Tesseract();
        // Set Tesseract data path - adjust based on your installation
        // For production, this should be configurable
        try {
            tesseract.setDatapath(System.getProperty("user.dir") + "/tessdata");
        } catch (Exception e) {
            // Fallback to default or system path
        }
        tesseract.setLanguage("eng");
    }

    public String extractText(byte[] imageBytes) throws TesseractException, IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        return tesseract.doOCR(image);
    }

    public String extractTextFromBase64(String base64Image) throws TesseractException, IOException {
        // Remove data URL prefix if present
        String base64Data = base64Image;
        if (base64Image.contains(",")) {
            base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        return extractText(imageBytes);
    }
}










