# ========== H5 前端部署说明 ==========

## 1. 选项一：最简单部署（使用 Java 后端）

最简单的方法是打包后直接运行，这是我们目前的方式。你的 Java 后端已经内置了网页，无需配置。
运行 `java -jar tcm-prescription-service.jar` 即可。

## 2. 选项二：使用 Nginx 部署（推荐用于生产环境）

如果你想使用 Nginx 反向代理（更专业，即使 Java 后端重启也不影响网页访问，还能配合 SSL 证书），请参考以下配置。

### 部署步骤
1. 将 content 目录下的所有文件上传到服务器目录，例如 `/var/www/tcm-h5`
2. 配置 Nginx 如下：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 这里改成你的域名或 IP

    # 前端静态文件
    location / {
        root /var/www/tcm-h5;     # 指向你的 H5 文件目录
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8081;   # 后端 Java 服务地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

这样分离部署的好处是：你可以单独更新前端 HTML 而不重启 Java 应用。
