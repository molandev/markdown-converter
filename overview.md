# 文档转换服务

## 📦 源码地址

- **GitHub**: https://github.com/molandev/markdown-converter
- **Gitee**: https://gitee.com/molandev/markdown-converter

## 💡 创作初衷

在构建 Java AI 应用时，我们面临一个关键挑战：**如何高效地将各种文档格式转换为结构化的 Markdown 文本**。这是 RAG 系统、智能问答、知识图谱构建等场景的基础能力。

### 🎯 应用场景

- **RAG 知识库构建**：从企业文档中提取内容，保留结构
- **智能问答系统**：解析用户上传的文档，基于内容回答问题
- **文档分析摘要**：自动生成摘要，提取关键信息
- **知识图谱构建**：从文档中提取实体和关系

### 🔧 技术选型

我们精选了三款业界顶尖的 Python 文档处理工具：

| 工具 | 适用场景 | 核心优势 |
|------|----------|----------|
| **MarkItDown** | DOCX、PPTX、XLSX → Markdown | 微软官方出品，结构保留完美 |
| **LibreOffice** | 旧版 .doc、格式互转 | 企业级能力，支持 100+ 格式 |
| **MinerU** | 复杂 PDF 解析 | OCR 加持，表格识别，学术论文利器 |

### 🌟 解决方案

我们构建了一个 **轻量、高效、生产就绪** 的文档转换微服务：

```
✅ 三种顶尖 Python 转换工具
✅ 封装为 Java REST API
✅ GraalVM 原生镜像（启动 0.04s，内存 20MB）
✅ 内置并发控制、自动清理、错误隔离
✅ Docker 一键部署，开箱即用
```

---

## ✨ 核心特性

### 🚀 极速启动

GraalVM Native Image 编译，启动时间从秒级降至毫秒级：

```bash
# 传统 JVM
Started in 3.245 seconds

# GraalVM Native Image  
Started in 0.042 seconds（快 80 倍）
```

### 💾 超低内存

原生镜像内存占用仅为传统 JVM 的 1/10：

| 模式 | 内存占用 | 镜像大小 |
|------|---------|---------|
| **传统 JVM** | ~200MB | ~350MB |
| **GraalVM Native** | ~20MB | ~80MB |

### 🔄 三种转换引擎

#### 1. MarkItDown - 微软官方方案

**适用场景**：
- ✅ Word 文档（.docx, .pptx, .xlsx）转 Markdown
- ✅ PDF 转 Markdown（非扫描件）
- ✅ 保留章节结构和图片

**快速开始**：
```bash
docker pull molandev/markitdown-converter-api:latest

docker run -d -p 10996:10996 --name markitdown-converter \
  molandev/markitdown-converter-api:latest
```

**镜像大小**：~80MB

#### 2. LibreOffice - 格式转换专家

**适用场景**：
- ✅ 旧版 Word 文档（.doc）转换
- ✅ Office 文档转 PDF/HTML/DOCX
- ✅ 企业级文档处理

**快速开始**：
```bash
docker pull molandev/libreoffice-converter-api:latest

docker run -d -p 10996:10996 --name libreoffice-converter \
  molandev/libreoffice-converter-api:latest
```

**镜像大小**：~500MB

#### 3. MinerU - PDF 解析专家

**适用场景**：
- ✅ 复杂 PDF 解析（多栏、表格、图像）
- ✅ 学术论文、研究报告
- ✅ OCR 文字识别

**快速开始**：
```bash
docker pull molandev/mineru-converter-api:latest

docker run -d -p 10996:10996 --name mineru-converter \
  molandev/mineru-converter-api:latest
```

**镜像大小**：~2GB

---

## 📖 API 文档

启动服务后，访问 `http://localhost:10996/help` 查看完整 API 文档。

### MarkItDown API

**文件上传转换**：
```bash
# 基础转换
curl -F "file=@document.docx" \
  http://localhost:10996/convert/markitdown/upload \
  -o output.md

# 保留图片
curl -F "file=@document.docx" -F "keepDataUris=true" \
  http://localhost:10996/convert/markitdown/upload \
  -o output.md
```

**URL 下载转换**：
```bash
curl -X POST "http://localhost:10996/convert/markitdown/url?fileUrl=https://example.com/doc.docx" \
  -o output.md
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文档文件（pdf/docx/pptx/xlsx/xls） |
| keepDataUris | Boolean | 否 | 是否保留图片，默认 false |

### LibreOffice API

**文件上传转换**：
```bash
# 转 PDF（默认）
curl -F "file=@document.doc" \
  http://localhost:10996/convert/libreoffice/upload \
  -o output.pdf

# 转 DOCX
curl -F "file=@document.doc" -F "format=docx" \
  http://localhost:10996/convert/libreoffice/upload \
  -o output.docx
```

**URL 下载转换**：
```bash
curl -X POST "http://localhost:10996/convert/libreoffice/url?fileUrl=https://example.com/doc.doc&format=pdf" \
  -o output.pdf
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文档文件 |
| format | String | 否 | 目标格式，默认 pdf（支持 pdf、docx、html 等） |

### MinerU API

**文件上传转换**：
```bash
# 输出 Markdown（默认）
curl -F "file=@document.pdf" \
  http://localhost:10996/convert/mineru/upload \
  -o output.md

# 输出 JSON
curl -F "file=@document.pdf" "http://localhost:10996/convert/mineru/upload?outputFormat=json" \
  -o output.json

# 输出全部文件（ZIP 包含 md、json、pdf、图片）
curl -F "file=@document.pdf" "http://localhost:10996/convert/mineru/upload?outputFormat=zip" \
  -o output.zip
```

**URL 下载转换**：
```bash
curl -X POST "http://localhost:10996/convert/mineru/url?fileUrl=https://example.com/doc.pdf&outputFormat=zip" \
  -o output.zip
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | PDF 文件 |
| outputFormat | String | 否 | 输出格式：md（默认）、json、zip |

---

## ⚙️ 配置说明

| 环境变量 | 说明 | 默认值 |
|----------|------|--------|
| `SERVER_PORT` | 服务端口 | 10996 |
| `CONVERTER_MAX_CONCURRENT` | 最大并发数 | 10 |
| `CONVERTER_ACQUIRE_TIMEOUT` | 获取并发许可超时（秒） | 30 |
| `CONVERTER_RETENTION_MINUTES` | 临时文件保留时间（分钟） | 10 |
| `CONVERTER_SCHEDULE_MINUTES` | 清理任务间隔（分钟） | 5 |
| `CONVERTER_TEMP_DIR` | 临时文件目录 | /app/temp |

**自定义配置示例**：
```bash
docker run -d -p 10996:10996 \
  -e CONVERTER_MAX_CONCURRENT=20 \
  -e CONVERTER_RETENTION_MINUTES=5 \
  -v /data/temp:/app/temp \
  molandev/markitdown-converter-api:latest
```

---

## 🐳 镜像对比

| 镜像 | 大小 | 推荐场景 |
|------|------|----------|
| **markitdown-converter-api** | ~80MB | DOCX/PPTX/XLSX 转 Markdown |
| **libreoffice-converter-api** | ~500MB | 旧版 .doc 转换、格式互转 |
| **mineru-converter-api** | ~2GB | 复杂 PDF 解析、OCR |

---

## 🔧 Java 集成示例

```java
// Spring Boot RestTemplate
MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
body.add("file", new FileSystemResource(file));

ResponseEntity<byte[]> response = restTemplate.postForEntity(
    "http://localhost:10996/convert/markitdown/upload",
    new HttpEntity<>(body),
    byte[].class
);

String markdown = new String(response.getBody(), StandardCharsets.UTF_8);
```

---

## 💪 核心优势

✅ **极速启动**：0.04 秒启动，比传统 JVM 快 80 倍  
✅ **超低内存**：仅需 20MB，是传统 JVM 的 1/10  
✅ **镜像轻量**：最小 80MB，减少 77% 体积  
✅ **生产就绪**：内置并发控制、文件管理、错误隔离  
✅ **完美转换**：保留章节结构、表格、图片  

## 🎯 适用场景

- RAG 知识库构建
- 智能问答系统
- 文档分析与摘要
- 知识图谱构建

## 🚀 一行命令启动

```bash
docker run -d -p 10996:10996 molandev/markitdown-converter-api:latest
```

---

<div align="center">
  <p>🎉 <b>轻量 · 高效 · 易用</b></p>
  <p><b>Document Converter</b> - 让文档转换更简单</p>
  <p>📦 <a href="https://hub.docker.com/r/molandev">Docker Hub</a></p>
</div>
