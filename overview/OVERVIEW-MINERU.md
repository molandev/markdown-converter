# MinerU PDF Converter Service - Overview

## 📦 Source Code

- **GitHub**: https://github.com/molandev/markdown-converter
- **Gitee**: https://gitee.com/molandev/markdown-converter

## ⚠️ Important Notice

> **This image runs in CPU-only mode with pipeline backend.**
> 
> For GPU acceleration, please use the [official MinerU image](https://github.com/opendatalab/MinerU).
> 
> - **CPU Mode**: Suitable for moderate workloads, no GPU required
> - **Pipeline Backend**: Optimized for general document processing
> - **GPU Mode**: Visit official repository for high-performance GPU support

> **This image only supports PDF files.**
> 
> - Does NOT support `.docx` or `.doc` Word documents
> - Does NOT support `.pptx`, `.xlsx`, or other Office formats
> 
> For other document types, use our other converters:
> 
> | Document Type | Recommended Image |
> |---------------|-------------------|
> | DOCX, PPTX, XLSX → Markdown | `molandev/markitdown-converter-api:latest` |
> | DOC → DOCX/PDF | `molandev/libreoffice-converter-api:latest` |

## 🚀 What Is It?

A **high-precision** PDF parsing service powered by MinerU with advanced OCR capabilities. Built on GraalVM Native Image for cloud-native deployment, it transforms complex PDF documents into structured Markdown or JSON format with exceptional accuracy.

## ✨ Core Features

### 🔥 **Superior PDF Parsing**
- **OCR Engine**: Extract text from scanned documents and images
- **Layout Preservation**: Maintains document structure and formatting
- **Table Detection**: Intelligent table extraction with proper formatting
- **Image Extraction**: Preserves images with organized output

### 🛡️ **Production-Ready Reliability**
- **Smart Concurrency Control**: Semaphore-based rate limiting prevents system overload
- **Auto-Cleanup Mechanism**: Scheduled task automatically purges temporary files
- **Graceful Degradation**: Queuing mechanism with configurable timeouts

### 🎯 **Flexible Output Formats**
- **Markdown**: Clean, LLM-friendly text output
- **JSON**: Structured data with full metadata
- **ZIP Archive**: All output files in one package (md, json, pdf variants, images)

## 📦 Output Files (ZIP Format)

| File | Description |
|------|-------------|
| `result.md` | Extracted Markdown content |
| `result_content_list.json` | Content structure list |
| `result_middle.json` | Intermediate processing data |
| `result_model.json` | Model output data |
| `result_layout.pdf` | Layout analysis PDF |
| `result_origin.pdf` | Original PDF copy |
| `result_span.pdf` | Span annotation PDF |
| `images/` | Extracted images folder |

## 🎮 Quick Start

### Pull from Docker Hub
```bash
docker pull molandev/mineru-converter-api:latest
```

### Run in Seconds
```bash
docker run -d \
  --name mineru-converter \
  -p 10996:10996 \
  -v /data/temp:/app/temp \
  -e CONVERTER_MAX_CONCURRENT=5 \
  -e CONVERTER_RETENTION_MINUTES=30 \
  molandev/mineru-converter-api:latest
```

### 📚 API Documentation
**Access the complete API guide at:** `http://localhost:10996/help`

### Convert Your First PDF
```bash
# Get Markdown (default)
curl -F "file=@document.pdf" \
     http://localhost:10996/convert/mineru/upload \
     -o output.md

# Get JSON
curl -F "file=@document.pdf" \
     "http://localhost:10996/convert/mineru/upload?outputFormat=json" \
     -o output.json

# Get all files (ZIP)
curl -F "file=@document.pdf" \
     "http://localhost:10996/convert/mineru/upload?outputFormat=zip" \
     -o output.zip

# Or provide a URL
curl -X POST "http://localhost:10996/convert/mineru/url?fileUrl=https://example.com/doc.pdf&outputFormat=zip" \
     -o output.zip
```

## ⚙️ Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `CONVERTER_MAX_CONCURRENT` | 5 | Max parallel conversions (MinerU is resource-intensive) |
| `CONVERTER_RETENTION_MINUTES` | 30 | Temp file retention |
| `CONVERTER_SCHEDULE_MINUTES` | 5 | Cleanup interval |
| `SERVER_PORT` | 10996 | Service port |

## 🔧 Advanced Usage

### Resource-Constrained Environment
```bash
docker run -e CONVERTER_MAX_CONCURRENT=2 \
           -e CONVERTER_RETENTION_MINUTES=60 \
           molandev/mineru-converter-api:latest
```

### High-Performance Setup
```bash
docker run -e CONVERTER_MAX_CONCURRENT=10 \
           --memory=8g \
           --cpus=4 \
           molandev/mineru-converter-api:latest
```

## 💡 Why Choose This?

✅ **OCR-Powered** - Extract text from scanned PDFs with high accuracy  
✅ **Multiple Output Formats** - Markdown, JSON, or complete ZIP archive  
✅ **Resource-Efficient** - GraalVM Native Image for minimal footprint  
✅ **Production-Ready** - Smart concurrency control & auto-cleanup  
✅ **Cloud-Native** - Docker-first design, Kubernetes-ready  

## 🎯 Perfect For

- **AI Knowledge Base Pipelines**: Convert PDFs to LLM-friendly Markdown
- **Document Digitization**: OCR for scanned documents and archives
- **Data Extraction**: Structured JSON output for downstream processing
- **Research & Analysis**: Extract tables, figures, and structured content

## 📊 API Reference

### Upload Endpoint
```
POST /convert/mineru/upload
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | PDF file to convert |
| outputFormat | String | No | Output format: `md` (default), `json`, `zip` |

### URL Endpoint
```
POST /convert/mineru/url
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| fileUrl | String | Yes | Remote PDF URL |
| outputFormat | String | No | Output format: `md` (default), `json`, `zip` |

---

**Built with Spring Boot 3.5.6 + GraalVM 25 + MinerU**

*Precision PDF parsing with OCR intelligence.* 📊
