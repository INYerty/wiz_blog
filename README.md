WizV个人Blog系统

## Docker

构建镜像：

```
docker build -t wiz_blog .
```

运行容器（默认端口 8080）：

```
docker run --rm -p 8080:8080 --name wiz_blog wiz_blog
```

如需连接外部 MySQL/Redis，可通过环境变量覆盖 Spring 配置（例如 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_HOST` 等）。

## GitHub Actions（推送到 Docker Hub）

工作流位于 `.github/workflows/docker-image.yml`，会在 `master` 分支 push 或手动触发时构建并推送镜像。

需要在仓库 `Settings > Secrets and variables > Actions` 中配置：

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

镜像名默认为 `${DOCKERHUB_USERNAME}/wiz_blog`，如需修改可调整工作流中的 `IMAGE_NAME`。
