WizV个人Blog系统

## Docker

拉取镜像：

```
docker pull ghcr.io/inyerty/wiz_blog:latest
```
改名(如果名称不一致):
```
docker tag ghcr.io/inyerty/wiz_blog:latest wiz_blog:latest
```

运行容器（默认端口 8080）：

```
docker run --rm -p 8080:8080 --name wiz_blog -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/wiz_blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" -e SPRING_DATASOURCE_USERNAME="YOUR_MYSQL_USERNAME" -e SPRING_DATASOURCE_PASSWORD="YOUR_MYSQL_PASSWORD" -e SPRING_DATA_REDIS_HOST="host.docker.internal" -e SPRING_REDIS_HOST="host.docker.internal" wiz_blog
```

如需连接外部 MySQL/Redis，可通过环境变量覆盖 Spring 配置（例如 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_HOST` 等）。

