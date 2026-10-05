# GitIntelligence

GitIntelligence 是一个企业级 AI 知识库应用，保留文档上传、解析、向量化、混合检索、权限控制、聊天和多租户组织标签等完整业务能力。

## 技术栈

- 后端：Java 17、Spring Boot 3.4、Spring Security、JWT、JPA、Redis、Kafka、Elasticsearch、MinIO
- 前端：Vue 3、TypeScript、Vite、Pinia、Naive UI、pnpm
- 本地重排：CPU 部署 `BAAI/bge-reranker-base`，默认 INT8、20 条候选、5 秒超时

## 启动依赖

安装 Java 17、Maven 3.8.6+、Node.js 18.20+、pnpm 8.7+。完整开发环境还需要 MySQL 8、Redis 7、Kafka、Elasticsearch 8.10 和 MinIO。

可使用 Docker Compose 启动基础服务和本地 rerank 服务：

```bash
docker compose -f docs/docker-compose.yaml up -d
```

## 启动后端

```bash
mvn spring-boot:run
```

默认端口为 `8081`。数据库、Redis、AI 服务和 rerank 地址可在 `src/main/resources/application*.yml` 中配置。

## 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

## Rerank 服务

直接在本机运行 sidecar：

```bash
cd rerank-service
python -m venv .venv
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8082
```

首次启动会下载模型。八代 i5 建议保持默认 INT8、批大小 4、最大长度 384；服务不可用或超时时，后端自动返回原混合检索顺序。
