package com.molandev.converter.integration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * MarkItDown Converter API 测试工具
 * 用于快速验证本地运行的 markitdown-convert-api Docker 镜像是否可用
 */
public class MarkItDownConverterTest {

    private static final String BASE_URL = "http://localhost:10996";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  MarkItDown Converter API Test");
        System.out.println("========================================\n");

        // TODO: 修改为你要测试的文件路径
        String testFilePath = "D:\\md\\test.docx";

        if (args.length > 0) {
            testFilePath = args[0];
        }

        Path filePath = Paths.get(testFilePath);
        if (!Files.exists(filePath)) {
            System.err.println("❌ 测试文件不存在：" + testFilePath);
            System.err.println("请修改 testFilePath 变量或通过参数传入文件路径");
            System.exit(1);
        }

        System.out.println("✅ 测试文件：" + filePath.toAbsolutePath());
        System.out.println("📡 服务地址：" + BASE_URL);
        System.out.println();

        try {
            // 测试 1: 健康检查
            testHealthCheck();

            // 测试 2: 文件上传转换
            testFileUpload(filePath);

            // 测试 3: URL 方式转换（可选）
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
     * 测试 1: 健康检查
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
     * 测试 2: 文件上传转换
     */
    private static void testFileUpload(Path filePath) throws Exception {
        System.out.println("🔍 Test 2: File Upload Conversion...");

        String urlStr = BASE_URL + "/convert/markitdown/upload?keepDataUris=false";
        String boundary = generateBoundary();

        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        // 写入 multipart 数据
        try (OutputStream os = conn.getOutputStream()) {
            writeMultipartData(os, filePath, boundary);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // 读取响应内容
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // 基于原始文件名生成输出文件名（将原扩展名替换为 .md）
            String originalFilename = filePath.getFileName().toString();
            String outputFilename = originalFilename;
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                outputFilename = originalFilename.substring(0, dotIndex) + ".md";
            } else {
                outputFilename = originalFilename + ".md";
            }
            System.out.println("   Output File: " + outputFilename);

            // 保存转换结果到 D:/md 目录，保持原文件名
            Path outputDir = Paths.get("D:/md");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                System.out.println("   Created output directory: " + outputDir.toAbsolutePath());
            }
            
            Path outputPath = outputDir.resolve(outputFilename);
            Files.write(outputPath, responseBody);
            System.out.println("   ✅ Saved to: " + outputPath.toAbsolutePath());
            System.out.println("   ✅ File conversion successful!");
        } else {
            String errorMessage = readErrorResponse(conn.getErrorStream());
            throw new RuntimeException("File upload conversion failed: HTTP " + responseCode + " - " + errorMessage);
        }
        System.out.println();
    }

    /**
     * 测试 3: URL 方式转换（示例）
     */
    @SuppressWarnings("unused")
    private static void testUrlConversion() throws Exception {
        System.out.println("🔍 Test 3: URL Conversion (Optional)...");

        // TODO: 替换为你的文档 URL
        String fileUrl = "http://192.168.1.100:5143/aaa.docx";

        String urlStr = BASE_URL + "/convert/markitdown/url?fileUrl=" +
                java.net.URLEncoder.encode(fileUrl, "UTF-8") + "&keepDataUris=false";

        HttpURLConnection conn = createConnection(urlStr, "POST");
        int responseCode = conn.getResponseCode();
        System.out.println("   Response Code: " + responseCode);

        if (responseCode == 200) {
            // 读取响应内容
            byte[] responseBody = conn.getInputStream().readAllBytes();
            System.out.println("   Response Size: " + responseBody.length + " bytes");

            // 从 URL 中提取文件名
            String outputFilename = extractFilenameFromUrl(fileUrl);
            System.out.println("   Output File: " + outputFilename);

            // 保存转换结果到 D:/md 目录
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
        conn.setReadTimeout(60000);
        return conn;
    }

    private static String generateBoundary() {
        return "----WebKitFormBoundary" + System.currentTimeMillis();
    }

    private static void writeMultipartData(OutputStream os, Path filePath, String boundary) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

        // 添加文件字段
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filePath.getFileName().toString()).append("\"\r\n");
        writer.append("Content-Type: application/octet-stream\r\n\r\n");
        writer.flush();

        // 写入文件内容
        Files.copy(filePath, os);
        os.flush();

        writer.append("\r\n").flush();
        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.close();
    }

    private static String extractFilename(String contentDisposition) {
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            int startIndex = contentDisposition.indexOf("filename*=");
            if (startIndex != -1) {
                // 处理 UTF-8 编码的文件名
                int equalsIndex = contentDisposition.indexOf("=", startIndex);
                if (equalsIndex != -1) {
                    String encoded = contentDisposition.substring(equalsIndex + 1);
                    try {
                        return java.net.URLDecoder.decode(encoded, "UTF-8");
                    } catch (Exception e) {
                        return encoded;
                    }
                }
            }
            // 普通文件名
            int startIndex2 = contentDisposition.indexOf("filename=");
            return contentDisposition.substring(startIndex2 + 10).replace("\"", "").trim();
        }
        return "converted.md";
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
