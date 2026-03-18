package com.molandev.converter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelpController {

    @Value("${converter.tool.type:all}")
    private String toolType;

    @GetMapping(value = {"/", "/help"}, produces = MediaType.TEXT_HTML_VALUE)
    public String help() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html lang='en'>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<link rel='icon' type='image/svg+xml' href='/favicon.svg'>")
            .append("<title>Document Converter API - Help</title>")
            .append("<style>")
            .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', sans-serif; line-height: 1.6; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }")
            .append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }")
            .append("h2 { color: #34495e; margin-top: 30px; border-left: 4px solid #3498db; padding-left: 10px; }")
            .append("h3 { color: #7f8c8d; margin-top: 20px; }")
            .append(".container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }")
            .append(".badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: bold; margin-left: 10px; }")
            .append(".badge-info { background: #3498db; color: white; }")
            .append(".badge-success { background: #27ae60; color: white; }")
            .append(".badge-warning { background: #f39c12; color: white; }")
            .append(".api-card { background: #f8f9fa; border-left: 4px solid #3498db; padding: 20px; margin: 15px 0; border-radius: 4px; }")
            .append(".endpoint { background: #ecf0f1; padding: 15px; margin: 10px 0; border-radius: 4px; font-family: monospace; }")
            .append(".method { display: inline-block; padding: 3px 8px; border-radius: 3px; font-weight: bold; margin-right: 8px; font-size: 0.9em; }")
            .append(".method-post { background: #3498db; color: white; }")
            .append(".method-get { background: #27ae60; color: white; }")
            .append(".path { color: #2c3e50; font-weight: bold; }")
            .append(".param { margin: 8px 0; padding-left: 20px; }")
            .append(".param-name { color: #e74c3c; font-weight: bold; }")
            .append(".code { background: #2c3e50; color: #ecf0f1; padding: 15px; border-radius: 4px; overflow-x: auto; margin: 10px 0; font-family: 'Courier New', monospace; font-size: 0.9em; }")
            .append(".note { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; border-radius: 4px; }")
            .append(".footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; color: #7f8c8d; }")
            .append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }")
            .append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }")
            .append("th { background: #3498db; color: white; font-weight: bold; }")
            .append("tr:hover { background: #f5f5f5; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class='container'>");
        
        // Header
        html.append("<h1>📄 Document Converter Service <span class='badge badge-info'>v1.0.0</span></h1>")
            .append("<p style='font-size: 1.1em; color: #7f8c8d;'>Lightweight & Efficient Document Conversion Service</p>")
            .append("<p><strong>Current Tool Type:</strong> <span class='badge badge-success'>").append(toolType).append("</span></p>");
        
        // Notes
        html.append("<div class='note'>")
            .append("<strong>📌 Important Notes</strong>")
            .append("<ul>")
            .append("<li><strong>File Naming:</strong> Server uses UUID filenames for security and uniqueness</li>")
            .append("<li><strong>Response Files:</strong> Original filename + new extension returned via Content-Disposition header</li>")
            .append("<li><strong>Format Validation:</strong> Uploaded files are validated; unsupported formats return 400 error</li>")
            .append("<li><strong>Error Handling:</strong> Parameter errors return 400, server errors return 500</li>")
            .append("</ul>")
            .append("</div>");
        
        // APIs based on tool type
        if ("all".equals(toolType) || "libreoffice".equals(toolType)) {
            html.append(getLibreOfficeHtml());
        }
        
        if ("all".equals(toolType) || "mineru".equals(toolType)) {
            html.append(getMinerUHtml());
        }
        
        if ("all".equals(toolType) || "markitdown".equals(toolType)) {
            html.append(getMarkItDownHtml());
        }
        
        // Footer
        html.append("<div class='footer'>")
            .append("<p>Document Converter Service © 2026 | Powered by GraalVM + Spring Boot</p>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    private String getLibreOfficeHtml() {
        StringBuilder html = new StringBuilder();
        
        html.append("<h2>🖨️ LibreOffice Document Conversion</h2>")
            .append("<p><strong>Description:</strong> Convert documents using LibreOffice (docx, xlsx, pptx, etc. → pdf, html, etc.)</p>")
            .append("<p><strong>Supported Formats:</strong> <span class='badge badge-warning'>Any LibreOffice-supported format</span></p>")
            .append("<div class='api-card'>")
            .append("<h3>📤 File Upload Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/libreoffice/upload</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>file</td><td>File</td><td>Yes</td><td>Document to convert</td></tr>")
            .append("<tr><td>format</td><td>String</td><td>No</td><td>Target format, default pdf (optional: pdf, html, docx, etc.)</td></tr>")
            .append("</table>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>curl -F \"file=@document.docx\" -F \"format=pdf\" http://localhost:10996/convert/libreoffice/upload -o output.pdf</div>")
            .append("</div>")
            .append("<div class='api-card'>")
            .append("<h3>🌐 URL Download Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/libreoffice/url</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>fileUrl</td><td>String</td><td>Yes</td><td>Remote file URL</td></tr>")
            .append("<tr><td>format</td><td>String</td><td>No</td><td>Target format, default pdf</td></tr>")
            .append("</table>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>curl -X POST \"http://localhost:10996/convert/libreoffice/url?fileUrl=https://example.com/doc.docx&format=pdf\" -o output.pdf</div>")
            .append("</div>");
        
        return html.toString();
    }
    
    private String getMinerUHtml() {
        StringBuilder html = new StringBuilder();
        
        html.append("<h2>📊 MinerU PDF Parser</h2>")
            .append("<p><strong>Description:</strong> Parse PDF documents to Markdown/JSON using MinerU with OCR support</p>")
            .append("<p><strong>Supported Formats:</strong> <span class='badge badge-warning'>PDF only</span></p>")
            .append("<div class='api-card'>")
            .append("<h3>📤 File Upload Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/mineru/upload</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>file</td><td>File</td><td>Yes</td><td>PDF file to convert</td></tr>")
            .append("<tr><td>outputFormat</td><td>String</td><td>No</td><td>Output format: md (default), json, or zip</td></tr>")
            .append("</table>")
            .append("<p><strong>Output Formats:</strong></p>")
            .append("<ul>")
            .append("<li><code>md</code> - Markdown file with extracted content</li>")
            .append("<li><code>json</code> - JSON file with structured data</li>")
            .append("<li><code>zip</code> - All output files packed (md, json, pdf variants, images)</li>")
            .append("</ul>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>")
            .append("# Get Markdown<br>")
            .append("curl -F \"file=@document.pdf\" http://localhost:10996/convert/mineru/upload -o output.md<br><br>")
            .append("# Get JSON<br>")
            .append("curl -F \"file=@document.pdf\" \"http://localhost:10996/convert/mineru/upload?outputFormat=json\" -o output.json<br><br>")
            .append("# Get all files (zip)<br>")
            .append("curl -F \"file=@document.pdf\" \"http://localhost:10996/convert/mineru/upload?outputFormat=zip\" -o output.zip")
            .append("</div>")
            .append("</div>")
            .append("<div class='api-card'>")
            .append("<h3>🌐 URL Download Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/mineru/url</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>fileUrl</td><td>String</td><td>Yes</td><td>Remote PDF URL</td></tr>")
            .append("<tr><td>outputFormat</td><td>String</td><td>No</td><td>Output format: md (default), json, or zip</td></tr>")
            .append("</table>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>curl -X POST \"http://localhost:10996/convert/mineru/url?fileUrl=https://example.com/doc.pdf&outputFormat=zip\" -o output.zip</div>")
            .append("</div>");
        
        return html.toString();
    }
    
    private String getMarkItDownHtml() {
        StringBuilder html = new StringBuilder();
        
        html.append("<h2>📝 MarkItDown Document Conversion</h2>")
            .append("<p><strong>Description:</strong> Convert documents to Markdown format using MarkItDown</p>")
            .append("<p><strong>Supported Formats:</strong> <span class='badge badge-warning'>pdf, docx, pptx, xlsx, xls</span></p>")
            .append("<div class='api-card'>")
            .append("<h3>📤 File Upload Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/markitdown/upload</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>file</td><td>File</td><td>Yes</td><td>Document file (pdf/docx/pptx/xlsx/xls)</td></tr>")
            .append("<tr><td>keepDataUris</td><td>Boolean</td><td>No</td><td>Keep images in document, default false</td></tr>")
            .append("</table>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>")
            .append("# Without images<br>")
            .append("curl -F \"file=@document.docx\" http://localhost:10996/convert/markitdown/upload -o output.md<br><br>")
            .append("# With images<br>")
            .append("curl -F \"file=@document.docx\" -F \"keepDataUris=true\" http://localhost:10996/convert/markitdown/upload -o output.md")
            .append("</div>")
            .append("</div>")
            .append("<div class='api-card'>")
            .append("<h3>🌐 URL Download Conversion</h3>")
            .append("<div class='endpoint'>")
            .append("<span class='method method-post'>POST</span>")
            .append("<span class='path'>/convert/markitdown/url</span>")
            .append("</div>")
            .append("<p><strong>Parameters:</strong></p>")
            .append("<table>")
            .append("<tr><th>Parameter</th><th>Type</th><th>Required</th><th>Description</th></tr>")
            .append("<tr><td>fileUrl</td><td>String</td><td>Yes</td><td>Remote file URL</td></tr>")
            .append("<tr><td>keepDataUris</td><td>Boolean</td><td>No</td><td>Keep images, default false</td></tr>")
            .append("</table>")
            .append("<p><strong>Example:</strong></p>")
            .append("<div class='code'>curl -X POST \"http://localhost:10996/convert/markitdown/url?fileUrl=https://example.com/doc.docx&keepDataUris=true\" -o output.md</div>")
            .append("</div>");
        
        return html.toString();
    }
}
