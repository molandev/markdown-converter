# Document Converter Service - Overview

## 📦 Source Code

- **GitHub**: https://github.com/molandev/markdown-converter
- **Gitee**: https://gitee.com/molandev/markdown-converter

## ⚠️ Important Notice

> **This image does NOT support `.doc` (legacy Word) format.**
> 
> For `.doc` files, convert to `.docx` first using our LibreOffice converter:
> 
> ```bash
> docker pull molandev/libreoffice-converter-api:latest
> ```
> 
> Then convert: `.doc` → `.docx` → Markdown

> **PDF conversion has limitations.**
> 
> - Only supports **non-scanned** PDFs (no OCR capability)
> - Quality is **significantly lower** than MinerU for complex PDFs
> 
> For scanned PDFs or better quality, use our MinerU converter:
> 
> ```bash
> docker pull molandev/mineru-converter-api:latest
> ```

## 🚀 What Is It?

A **lightning-fast**, **cloud-native** document conversion service powered by GraalVM Native Image and Python's MarkItDown. Built for high-performance scenarios with minimal resource consumption, it transforms Office documents into clean Markdown format effortlessly.

## ✨ Core Features

### 🔥 **Blazing Fast Performance**
- **GraalVM Native Image**: Sub-second startup, 50% less memory footprint
- **MarkItDown Powered**: Intelligent extraction from Office documents
- **Stream Processing**: Handles large files (100MB+) without OOM errors

### 🛡️ **Production-Ready Reliability**
- **Smart Concurrency Control**: Semaphore-based rate limiting prevents system overload
- **Auto-Cleanup Mechanism**: Scheduled task automatically purges temporary files
- **Graceful Degradation**: Queuing mechanism with configurable timeouts

### 🎯 **Developer-Friendly API**
- **Dual Input Modes**: Upload files directly or provide URLs
- **RESTful Design**: Clean endpoints with proper HTTP status codes
- **Chinese Filename Support**: URL-encoded headers for international compatibility

## 📦 Supported Formats

| Input Format | Output | Best For |
|--------------|--------|----------|
| PDF | Markdown | Non-scanned documents only |
| DOCX | Markdown | Word documents (2007+) |
| PPTX | Markdown | PowerPoint presentations |
| XLSX/XLS | Markdown | Excel spreadsheets |

> ⚠️ **Note**: 
> - Legacy `.doc` format is NOT supported. Use [LibreOffice converter](#) to convert `.doc` → `.docx` first.
> - For **scanned PDFs** or **better quality**, use [MinerU converter](#) with OCR support.

## 🎮 Quick Start

### Pull from Docker Hub
```bash
docker pull molandev/markitdown-converter-api:latest
```

### Run in Seconds
```bash
docker run -d \
  --name doc-converter \
  -p 10996:10996 \
  -v /data/temp:/app/temp \
  -e CONVERTER_MAX_CONCURRENT=10 \
  -e CONVERTER_RETENTION_MINUTES=10 \
  molandev/markitdown-converter-api:latest
```

### 📚 API Documentation
**Access the complete API guide at:** `http://localhost:10996/help`

### Convert Your First Document
```bash
# Upload a Word document
curl -F "file=@report.docx" \
     http://localhost:10996/convert/markitdown/upload \
     -o report.md

# Or provide a URL
curl -X POST "http://localhost:10996/convert/markitdown/url?fileUrl=https://example.com/doc.pdf" \
     -o output.md

# Keep embedded images
curl -F "file=@document.pptx" \
     -F "keepDataUris=true" \
     http://localhost:10996/convert/markitdown/upload \
     -o output.md
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
           molandev/markitdown-converter-api:latest
```

### Minimal Resource Environment
```bash
docker run -e CONVERTER_MAX_CONCURRENT=3 \
           -e CONVERTER_RETENTION_MINUTES=5 \
           molandev/markitdown-converter-api:latest
```

## 💡 Why Choose This?

✅ **Zero JVM Warmup** - GraalVM Native means instant peak performance  
✅ **Resource-Efficient** - Runs comfortably on 2C4G instances  
✅ **Production-Ready** - Smart concurrency control & auto-cleanup  
✅ **Cloud-Native** - Docker-first design, Kubernetes-ready  
✅ **MarkItDown Powered** - Best-in-class Office document extraction  

## 🎯 Perfect For

- **AI Knowledge Base Pipelines**: Convert Office docs to LLM-friendly Markdown
- **Document Management Systems**: Batch conversion workflows
- **Content Migration Projects**: Legacy format modernization
- **SaaS Applications**: Multi-tenant document processing

---

**Built with Spring Boot 3.5.6 + GraalVM 25 + Python MarkItDown**

*One engine, one purpose: Perfect Markdown from Office documents.* ⚡
