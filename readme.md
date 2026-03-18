# Document Converter API

**Lightning-fast, cloud-native document conversion powered by GraalVM Native Image.**

Transform documents with sub-second startup and 50% less memory footprint. Choose from three specialized engines for your specific needs.

## 🚀 Quick Start with Docker

```bash
# MarkItDown - Office docs to Markdown
 docker pull molandev/markitdown-converter-api:latest

# LibreOffice - Universal format conversion
 docker pull molandev/libreoffice-converter-api:latest

# MinerU - PDF parsing with OCR
 docker pull molandev/mineru-converter-api:latest
```

### Run in Seconds
```bash
docker run -d -p 10996:10996 molandev/markitdown-converter-api:latest
# That's it! Visit http://localhost:10996/help for API docs
```

## 📦 Choose Your Engine

| Engine | Best For | Pull Command |
|--------|----------|--------------|
| **MarkItDown** | DOCX/PPTX/XLSX → Markdown | `docker pull molandev/markitdown-converter-api:latest` |
| **LibreOffice** | 100+ formats (DOC→DOCX, DOCX→PDF, etc.) | `docker pull molandev/libreoffice-converter-api:latest` |
| **MinerU** | PDF → Markdown/JSON (with OCR) | `docker pull molandev/mineru-converter-api:latest` |

### 📋 Detailed Overview

- [MarkItDown Overview](./OVERVIEW-MARKITDOWN.md) - Office documents to Markdown
- [LibreOffice Overview](./OVERVIEW-LIBREOFFICE.md) - Universal format conversion
- [MinerU Overview](./OVERVIEW-MINERU.md) - PDF parsing with OCR

## ✨ Why Choose This?

✅ **Zero JVM Warmup** - GraalVM Native Image means instant peak performance  
✅ **Resource-Efficient** - Runs comfortably on 2C4G instances  
✅ **Production-Ready** - Smart concurrency control & auto-cleanup  
✅ **Cloud-Native** - Docker-first design, Kubernetes-ready  
✅ **Chinese Filename Support** - URL-encoded headers for international compatibility  

## 🔥 Core Features

### Smart Concurrency Control
Semaphore-based rate limiting prevents system overload:
- Configurable max concurrent conversions
- Graceful queuing with timeout
- Automatic "system busy" response when overloaded

### Auto-Cleanup Mechanism
Scheduled task automatically purges temporary files:
- Configurable retention time
- Configurable cleanup interval
- Prevents disk space exhaustion

### Memory Optimized
Stream-based file handling:
- No OOM errors with large files (100MB+)
- Low memory footprint
- Efficient resource utilization

## 📚 API Examples

### MarkItDown (Office → Markdown)
```bash
# Convert Word document
 curl -F "file=@report.docx" http://localhost:10996/convert/markitdown/upload -o report.md

# Keep embedded images
 curl -F "file=@presentation.pptx" -F "keepDataUris=true" http://localhost:10996/convert/markitdown/upload -o output.md
```

### LibreOffice (Universal Conversion)
```bash
# DOCX to PDF
 curl -F "file=@document.docx" -F "format=pdf" http://localhost:10996/convert/libreoffice/upload -o output.pdf

# DOC to DOCX
 curl -F "file=@legacy.doc" -F "format=docx" http://localhost:10996/convert/libreoffice/upload -o output.docx
```

### MinerU (PDF → Markdown/JSON)
```bash
# Get Markdown
 curl -F "file=@document.pdf" http://localhost:10996/convert/mineru/upload -o output.md

# Get all files (ZIP)
 curl -F "file=@document.pdf" "http://localhost:10996/convert/mineru/upload?outputFormat=zip" -o output.zip
```

## ⚙️ Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `CONVERTER_MAX_CONCURRENT` | 10 | Max parallel conversions |
| `CONVERTER_RETENTION_MINUTES` | 10 | Temp file retention (minutes) |
| `CONVERTER_SCHEDULE_MINUTES` | 5 | Cleanup interval (minutes) |
| `CONVERTER_TEMP_DIR` | /app/temp | Temp file directory |
| `SERVER_PORT` | 10996 | Service port |

### Performance Tuning

```bash
# High-concurrency setup
docker run -e CONVERTER_MAX_CONCURRENT=20 \
           -e CONVERTER_ACQUIRE_TIMEOUT=60 \
           molandev/markitdown-converter-api:latest

# Resource-constrained environment
docker run -e CONVERTER_MAX_CONCURRENT=3 \
           -e CONVERTER_RETENTION_MINUTES=5 \
           molandev/markitdown-converter-api:latest

# With persistent temp directory
docker run -v /data/converter-temp:/app/temp \
           -e CONVERTER_RETENTION_MINUTES=30 \
           molandev/markitdown-converter-api:latest
```

## 🎯 Perfect For

- **AI Knowledge Base Pipelines** - Convert documents to LLM-friendly Markdown
- **Document Management Systems** - Batch conversion workflows
- **Content Migration Projects** - Legacy format modernization
- **SaaS Applications** - Multi-tenant document processing
- **Research & Analysis** - Extract structured content from PDFs

## 🏗️ Build from Source

```bash
# MarkItDown
 docker build -f Dockerfile-markitdown -t molandev/markitdown-converter-api:latest .

# LibreOffice
 docker build -f Dockerfile-libreoffice -t molandev/libreoffice-converter-api:latest .

# MinerU
 docker build -f Dockerfile-mineru-pipline -t molandev/mineru-converter-api:latest .
```

## 📖 Documentation

- **API Documentation**: `http://localhost:10996/help` (available after starting the service)
- **中文文档**: [README-zh.md](./README-zh.md)

---

**Built with Spring Boot 3.5.6 + GraalVM 25**

*One service, three engines, infinite possibilities.* ⚡
