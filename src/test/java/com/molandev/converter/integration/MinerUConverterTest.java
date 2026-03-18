package com.molandev.converter.integration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MinerU Converter API Test
 * Used to verify PDF to Markdown conversion via MinerU
 */
public class MinerUConverterTest {

    private static final String BASE_URL = "http://localhost:10996";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  MinerU PDF to Markdown Test");
        System.out.println("========================================\n");

        // TODO: Change to your test PDF file path
        String testFilePath = "D:\\md\\test.pdf";

        if (args.length > 0) {
            testFilePath = args[0];
        }

        Path filePath = Paths.get(testFilePath);
        if (!Files.exists(filePath)) {
            System.err.println("❌ Test file not found: " + testFilePath);
            System.err.println("Please modify testFilePath variable or pass file path as argument");
            System.exit(1);
        }

        // Validate file extension
        String filename = filePath.getFileName().toString().toLowerCase();
        if (!filename.endsWith(".pdf")) {
            System.err.println("❌ MinerU only supports PDF files");
            System.err.println("Provided file: " + filePath.getFileName());
            System.exit(1);
        }

        System.out.println("✅ Test file: " + filePath.toAbsolutePath());
        System.out.println("📡 Service URL: " + BASE_URL);
        System.out.println();

        try {
            // Test 1: Health Check
            testHealthCheck();

            // Test 2: File Upload Conversion (PDF -> Markdown)
//            testFileUpload(filePath);

            // Test 3: URL Conversion (Optional)
             testUrlConversion();

            System.out.println("\n========================================");
            System.out.println("  ✅ All Tests Passed!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("  ❌ Test Failed!");
            System.err.println("========================================");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Test 1: Health Check
     */
    private static void testHealthCheck() throws Exception {
        System.out.println("🔍 Test 1: Health Check...");

        String urlStr = BASE_URL + "/health";
        HttpURLConnection conn = createConnection(urlStr, "GET");
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            System.out.println("   ✅ Service is running (HTTP " + responseCode + ")");
        } else {
            throw new RuntimeException("Health check failed: HTTP " + responseCode);
        }
        System.out.println();
    }

    /**
     * Test 2: File Upload Conversion (PDF -> Markdown)
     */
    private static void testFileUpload(Path filePath) throws Exception {
        System.out.println("🔍 Test 2: PDF to Markdown Conversion...");

        // MinerU API endpoint with outputFormat=md (default)
        String urlStr = BASE_URL + "/convert/mineru/upload?outputFormat=md";
        String boundary = generateBoundary();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(300000); // MinerU may take longer for complex PDFs

        // Write multipart data
        try (OutputStream os = conn.getOutputStream()) {
            writeMultipartData(os, filePath, boundary);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // Read response content
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // Generate output filename (replace extension with .md)
            String originalFilename = filePath.getFileName().toString();
            String outputFilename = originalFilename;
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                outputFilename = originalFilename.substring(0, dotIndex) + ".md";
            } else {
                outputFilename = originalFilename + ".md";
            }
            System.out.println("   Output File: " + outputFilename);

            // Save converted file to D:/md directory
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ PDF to Markdown conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("PDF conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    /**
     * Test 3: URL Conversion (Example)
     */
    @SuppressWarnings("unused")
    private static void testUrlConversion() throws Exception {
        System.out.println("🔍 Test 3: URL Conversion (Optional)...");

        // TODO: Replace with your PDF URL
        String fileUrl = "http://192.168.1.100:5143/testscan.pdf";

        String urlStr = BASE_URL + "/convert/mineru/url?fileUrl=" +
                java.net.URLEncoder.encode(fileUrl, "UTF-8") + "&outputFormat=zip";

        HttpURLConnection conn = createConnection(urlStr, "POST");
        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // Read response content
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // Extract filename from URL
            String outputFilename = extractFilenameFromUrl(fileUrl).replace(".md",".zip");
            System.out.println("   Output File: " + outputFilename);

            // Save converted file to D:/md directory
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ URL conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("URL conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    // ==================== Helper Methods ====================

    private static HttpURLConnection createConnection(String urlStr, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(300000); // MinerU needs longer timeout
        return conn;
    }

    private static String generateBoundary() {
        return "----WebKitFormBoundary" + System.currentTimeMillis();
    }

    private static void writeMultipartData(OutputStream os, Path filePath, String boundary) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

        // Add file field
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filePath.getFileName().toString()).append("\"\r\n");
        writer.append("Content-Type: application/pdf\r\n\r\n");
        writer.flush();

        // Write file content
        Files.copy(filePath, os);
        os.flush();

        writer.append("\r\n").flush();
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.close();
    }

    private static String readErrorResponse(InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return "No error message";
        }
        return new String(errorStream.readAllBytes(), "UTF-8");
    }

    /**
     * Extract filename from URL and convert extension to .md
     */
    private static String extractFilenameFromUrl(String fileUrl) {
        // Extract filename from URL path
        String path = fileUrl;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            path = path.substring(lastSlash + 1);
        }
        // Remove query parameters
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        // Replace extension with .md
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            path = path.substring(0, dotIndex) + ".md";
        } else {
            path = path + ".md";
        }
        return path.isEmpty() ? "converted.md" : path;
    }
}
