# LibreOffice Converter Service - Overview

## 🚀 What Is It?

A **powerful**, **cloud-native** document format conversion service powered by GraalVM Native Image and LibreOffice. Built for versatile document transformation with minimal resource consumption, it converts between 100+ document formats effortlessly.

## ✨ Core Features

### 🔥 **Versatile Format Conversion**
- **LibreOffice Engine**: Industry-standard document conversion
- **100+ Formats**: DOC, DOCX, PDF, XLS, XLSX, PPT, PPTX, HTML, and more
- **Stream Processing**: Handles large files without OOM errors

### 🛡️ **Production-Ready Reliability**
- **Smart Concurrency Control**: Semaphore-based rate limiting prevents system overload
- **Auto-Cleanup Mechanism**: Scheduled task automatically purges temporary files
- **Graceful Degradation**: Queuing mechanism with configurable timeouts

### 🎯 **Developer-Friendly API**
- **Dual Input Modes**: Upload files directly or provide URLs
- **RESTful Design**: Clean endpoints with proper HTTP status codes
- **Chinese Filename Support**: URL-encoded headers for international compatibility

## 📦 Supported Conversions

| From | To | Best For |
|------|----|---------|
| DOC | DOCX, PDF | Legacy Word document modernization |
| DOCX | PDF, HTML | Document sharing & publishing |
| XLS | XLSX, PDF | Excel spreadsheet conversion |
| XLSX | PDF, CSV | Report generation |
| PPT | PPTX, PDF | PowerPoint presentation conversion |
| PPTX | PDF | Presentation sharing |
| Any LibreOffice Format | PDF | Universal document output |

## 🎮 Quick Start

### Pull from Docker Hub
```bash
docker pull molandev/libreoffice-converter-api:latest
```

### Run in Seconds
```bash
docker run -d \
  --name libreoffice-converter \
  -p 10996:10996 \
  -v /data/temp:/app/temp \
  -e CONVERTER_MAX_CONCURRENT=10 \
  -e CONVERTER_RETENTION_MINUTES=10 \
  molandev/libreoffice-converter-api:latest
```

### 📚 API Documentation
**Access the complete API guide at:** `http://localhost:10996/help`

### Convert Your First Document
```bash
# Convert DOC to DOCX
curl -F "file=@document.doc" \
     -F "format=docx" \
     http://localhost:10996/convert/libreoffice/upload \
     -o output.docx

# Convert DOCX to PDF
curl -F "file=@document.docx" \
     -F "format=pdf" \
     http://localhost:10996/convert/libreoffice/upload \
     -o output.pdf

# Convert via URL
curl -X POST "http://localhost:10996/convert/libreoffice/url?fileUrl=https://example.com/doc.docx&format=pdf" \
     -o output.pdf

# Convert Excel to PDF
curl -F "file=@spreadsheet.xlsx" \
     -F "format=pdf" \
     http://localhost:10996/convert/libreoffice/upload \
     -o output.pdf
```

## ⚙️ Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `CONVERTER_MAX_CONCURRENT` | 10 | Max parallel conversions |
| `CONVERTER_RETENTION_MINUTES` | 10 | Temp file retention |
| `CONVERTER_SCHEDULE_MINUTES` | 5 | Cleanup interval |
| `SERVER_PORT` | 10996 | Service port |

## 🔧 Advanced Usage

### High-Concurrency Setup
```bash
docker run -e CONVERTER_MAX_CONCURRENT=20 \
           -e CONVERTER_ACQUIRE_TIMEOUT=60 \
           molandev/libreoffice-converter-api:latest
```

### Minimal Resource Environment
```bash
docker run -e CONVERTER_MAX_CONCURRENT=3 \
           -e CONVERTER_RETENTION_MINUTES=5 \
           molandev/libreoffice-converter-api:latest
```

## 💡 Why Choose This?

✅ **Zero JVM Warmup** - GraalVM Native means instant peak performance  
✅ **100+ Format Support** - LibreOffice handles virtually any document format  
✅ **Production-Ready** - Smart concurrency control & auto-cleanup  
✅ **Cloud-Native** - Docker-first design, Kubernetes-ready  
✅ **Battle-Tested Engine** - LibreOffice is the industry standard for document conversion  

## 🎯 Perfect For

- **Legacy Document Modernization**: Convert old DOC files to modern DOCX format
- **Document Publishing**: Transform any format to PDF for universal sharing
- **Report Generation**: Convert Excel spreadsheets to PDF reports
- **Content Migration Projects**: Batch convert documents between formats
- **SaaS Applications**: Multi-tenant document processing

## 📋 Common Use Cases

### DOC to DOCX (Legacy Modernization)
```bash
curl -F "file=@legacy.doc" -F "format=docx" \
     http://localhost:10996/convert/libreoffice/upload -o modern.docx
```

### DOCX to PDF (Document Sharing)
```bash
curl -F "file=@document.docx" -F "format=pdf" \
     http://localhost:10996/convert/libreoffice/upload -o document.pdf
```

### XLSX to PDF (Report Generation)
```bash
curl -F "file=@report.xlsx" -F "format=pdf" \
     http://localhost:10996/convert/libreoffice/upload -o report.pdf
```

### PPTX to PDF (Presentation Sharing)
```bash
curl -F "file=@slides.pptx" -F "format=pdf" \
     http://localhost:10996/convert/libreoffice/upload -o slides.pdf
```

---

**Built with Spring Boot 3.5.6 + GraalVM 25 + LibreOffice**

*One engine, endless possibilities: Universal document format conversion.* ⚡
