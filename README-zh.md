# Document Converter API

**基于 GraalVM Native Image 的闪电级文档转换服务。**

亚秒级启动，内存占用降低 50%。三种专业引擎，满足您的各种文档转换需求。

## 🚀 一键拉取，即刻使用

```bash
# MarkItDown - Office 文档转 Markdown
 docker pull molandev/markitdown-converter-api:latest

# LibreOffice - 通用格式转换
 docker pull molandev/libreoffice-converter-api:latest

# MinerU - PDF 智能解析（支持 OCR）
 docker pull molandev/mineru-converter-api:latest
```

### 秒级启动
```bash
docker run -d -p 10996:10996 molandev/markitdown-converter-api:latest
# 就这么简单！访问 http://localhost:10996/help 查看完整 API 文档
```

## 📦 选择您的引擎

| 引擎 | 适用场景 | 拉取命令 |
|------|----------|----------|
| **MarkItDown** | DOCX/PPTX/XLSX → Markdown | `docker pull molandev/markitdown-converter-api:latest` |
| **LibreOffice** | 100+ 格式互转（DOC→DOCX、DOCX→PDF 等） | `docker pull molandev/libreoffice-converter-api:latest` |
| **MinerU** | PDF → Markdown/JSON（支持 OCR） | `docker pull molandev/mineru-converter-api:latest` |

### 📋 详细说明

- [MarkItDown 说明](./OVERVIEW-MARKITDOWN.md) - Office 文档转 Markdown
- [LibreOffice 说明](./OVERVIEW-LIBREOFFICE.md) - 通用格式转换
- [MinerU 说明](./OVERVIEW-MINERU.md) - PDF 智能解析

## ✨ 为什么选择我们？

✅ **零 JVM 预热** - GraalVM Native Image 带来即时峰值性能  
✅ **极致省资源** - 2C4G 配置即可流畅运行  
✅ **生产级就绪** - 智能并发控制 + 自动清理机制  
✅ **云原生设计** - Docker 优先，Kubernetes 友好  
✅ **中文文件名支持** - URL 编码头，国际化无忧  

## 🔥 核心特性

### 智能并发控制
基于信号量的限流机制，防止系统过载：
- 可配置最大并发数
- 优雅排队等待
- 超载时自动返回"系统繁忙"

### 自动清理机制
定时任务自动清理临时文件：
- 可配置保留时间
- 可配置清理间隔
- 防止磁盘空间耗尽

### 内存优化
基于流的文件处理：
- 大文件（100MB+）也不会 OOM
- 低内存占用
- 高效资源利用

## 📚 API 示例

### MarkItDown（Office → Markdown）
```bash
# 转换 Word 文档
 curl -F "file=@report.docx" http://localhost:10996/convert/markitdown/upload -o report.md

# 保留嵌入图片
 curl -F "file=@presentation.pptx" -F "keepDataUris=true" http://localhost:10996/convert/markitdown/upload -o output.md
```

### LibreOffice（通用转换）
```bash
# DOCX 转 PDF
 curl -F "file=@document.docx" -F "format=pdf" http://localhost:10996/convert/libreoffice/upload -o output.pdf

# DOC 转 DOCX
 curl -F "file=@legacy.doc" -F "format=docx" http://localhost:10996/convert/libreoffice/upload -o output.docx
```

### MinerU（PDF → Markdown/JSON）
```bash
# 获取 Markdown
 curl -F "file=@document.pdf" http://localhost:10996/convert/mineru/upload -o output.md

# 获取所有文件（ZIP）
 curl -F "file=@document.pdf" "http://localhost:10996/convert/mineru/upload?outputFormat=zip" -o output.zip
```

## ⚙️ 配置参数

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `CONVERTER_MAX_CONCURRENT` | 10 | 最大并发转换数 |
| `CONVERTER_RETENTION_MINUTES` | 10 | 临时文件保留时间（分钟） |
| `CONVERTER_SCHEDULE_MINUTES` | 5 | 清理任务间隔（分钟） |
| `CONVERTER_TEMP_DIR` | /app/temp | 临时文件目录 |
| `SERVER_PORT` | 10996 | 服务端口 |

### 性能调优

```bash
# 高并发配置
docker run -e CONVERTER_MAX_CONCURRENT=20 \
           -e CONVERTER_ACQUIRE_TIMEOUT=60 \
           molandev/markitdown-converter-api:latest

# 资源受限环境
docker run -e CONVERTER_MAX_CONCURRENT=3 \
           -e CONVERTER_RETENTION_MINUTES=5 \
           molandev/markitdown-converter-api:latest

# 持久化临时目录
docker run -v /data/converter-temp:/app/temp \
           -e CONVERTER_RETENTION_MINUTES=30 \
           molandev/markitdown-converter-api:latest
```

## 🎯 适用场景

- **AI 知识库流水线** - 将文档转换为 LLM 友好的 Markdown
- **文档管理系统** - 批量转换工作流
- **内容迁移项目** - 传统格式现代化
- **SaaS 应用** - 多租户文档处理
- **科研分析** - 从 PDF 提取结构化内容

## 🏗️ 从源码构建

```bash
# MarkItDown
 docker build -f Dockerfile-markitdown -t molandev/markitdown-converter-api:latest .

# LibreOffice
 docker build -f Dockerfile-libreoffice -t molandev/libreoffice-converter-api:latest .

# MinerU
 docker build -f Dockerfile-mineru-pipline -t molandev/mineru-converter-api:latest .
```

## 📖 文档

- **API 文档**：`http://localhost:10996/help`（启动服务后访问）
- **English Docs**: [README.md](./README.md)

---

**基于 Spring Boot 3.5.6 + GraalVM 25 构建**

*一个服务，三种引擎，无限可能。* ⚡
