package com.animalanalyzer.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@Service
@Slf4j
public class ImageService {
    
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_FORMATS = Set.of("image/jpeg", "image/png");
    
    public String processImage(MultipartFile file) throws IOException {
        // Validate image
        validateImage(file);
        
        // Read and process image
        val image = ImageIO.read(file.getInputStream());
        val resized = resizeImage(image, MAX_IMAGE_SIZE);
        
        // Convert to base64
        val baos = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", baos);
        val imageBytes = baos.toByteArray();
        
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!ALLOWED_FORMATS.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid image format. Only JPEG and PNG are allowed.");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image size exceeds 10MB limit");
        }
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int maxSize) {
        val width = originalImage.getWidth();
        val height = originalImage.getHeight();
        
        // Calculate new dimensions maintaining aspect ratio
        val scale = Math.min((double) maxSize / width, (double) maxSize / height);
        
        if (scale >= 1.0) {
            return originalImage; // No need to resize
        }
        
        val newWidth = (int) (width * scale);
        val newHeight = (int) (height * scale);
        
        val resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        val g2d = resizedImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
}