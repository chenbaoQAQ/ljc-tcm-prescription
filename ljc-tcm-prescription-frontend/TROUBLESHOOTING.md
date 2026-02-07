# 前端无法保存数据 - 诊断指南

## 问题现象
小程序显示：**"网络错误/后端未启动"**

## 排查步骤

### 1. 确认后端已启动 ✅

在浏览器访问：
```
http://localhost:8081/swagger-ui.html
```

如果能打开Swagger页面，说明后端正常运行。

### 2. 确认数据库连接 ✅

后端需要连接数据库 `ljc_tcm_prescription`。

**检查MySQL是否启动：**
```bash
# Mac/Linux
ps aux | grep mysql

# 或者通过Docker
docker ps
```

**确认数据库存在：**
```bash
mysql -u root -p
```
然后执行：
```sql
SHOW DATABASES;
USE ljc_tcm_prescription;
SHOW TABLES;
```

应该看到4张表：
- herbs
- prescriptions
- prescription_items
- medical_records

### 3. 确认局域网IP配置 ⚠️

**关键问题：**前端配置的IP `192.168.1.7` 必须是你**实际电脑的IP**

**如何获取正确的IP：**

Mac/Linux:
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Windows:
```bash
ipconfig
```

找到类似 `192.168.x.x` 的地址（不是127.0.0.1）

**修改前端配置：**

打开 `config/env.js`，把IP改成你电脑的实际IP：
```javascript
module.exports = {
  BASE_URL: 'http://你的实际IP:8081'  // 比如 http://192.168.31.100:8081
};
```

### 4. 测试网络连通性

**在微信开发者工具的控制台（Console）：**

```javascript
wx.request({
  url: 'http://你的实际IP:8081/api/v1/herbs',
  method: 'GET',
  success: (res) => console.log('成功', res),
  fail: (err) => console.error('失败', err)
});
```

如果失败，可能是：
- IP地址错误
- 端口被防火墙阻止
- 后端没有启动

### 5. 确认CORS配置

后端需要允许跨域请求。检查是否有CORS配置：

在后端添加（如果没有的话）：
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*");
    }
}
```

### 6. 检查微信开发者工具设置 ✅

**必须勾选：**
详情 → 本地设置 → **不校验合法域名、web-view（业务域名）、TLS版本以及HTTPS证书**

如果没勾选，http请求会被阻止！

### 7. 查看详细错误信息

在微信开发者工具：
1. 打开"调试器" → "Network"标签
2. 尝试添加药材
3. 查看请求详情：
   - Request URL 是否正确？
   - Status Code 是什么？
   - Response 内容是什么？

## 快速测试方案

### 方案A：直接用localhost（开发者工具在同一台电脑）

修改 `config/env.js`：
```javascript
module.exports = {
  BASE_URL: 'http://localhost:8081'
};
```

重新编译小程序，再试一次。

### 方案B：用真机调试

如果要在真机上测试，必须：
1. 手机和电脑在同一WiFi网络
2. 使用电脑的局域网IP（不能用localhost）
3. 确保电脑防火墙允许8081端口

## 常见错误及解决

### 错误1：net::ERR_CONNECTION_REFUSED
**原因：**后端没启动或IP/端口错误

**解决：**
1. 确认后端在运行
2. 确认IP和端口正确

### 错误2：CORS policy blocked
**原因：**后端没有CORS配置

**解决：**添加CORS配置（见上方第5步）

### 错误3：404 Not Found
**原因：**API路径错误

**解决：**
- 确认后端接口是 `/api/v1/herbs`
- 查看Swagger确认实际路径

### 错误4：500 Internal Server Error
**原因：**后端代码或数据库有问题

**解决：**
- 查看后端控制台日志
- 确认数据库表已创建
- 检查数据库连接配置

## 最简单的验证方法

**1. 在浏览器直接测试API：**

打开浏览器，访问：
```
http://localhost:8081/api/v1/herbs?keyword=&page=1&size=50
```

应该返回：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [],
    ...
  }
}
```

**2. 用Postman测试添加药材：**

```
POST http://localhost:8081/api/v1/herbs
Content-Type: application/json

{
  "nameCn": "测试药材"
}
```

如果成功，说明后端完全正常，问题在前端网络配置。

## 我的诊断建议

根据截图"网络错误/后端未启动"，最可能的原因是：

1. **IP地址配置错误**（90%可能）
   - 解决：改成 `http://localhost:8081`（如果开发者工具和后端在同一台电脑）
   
2. **没勾选"不校验域名"**（5%可能）
   - 解决：勾选该选项并重新编译

3. **后端没启动**（5%可能）
   - 解决：启动后端服务

## 推荐检查顺序

1. ✅ 浏览器访问 `http://localhost:8081/swagger-ui.html`
2. ✅ 修改 `config/env.js` 为 `http://localhost:8081`
3. ✅ 确认勾选"不校验域名"
4. ✅ 重新编译小程序
5. ✅ 再次测试添加药材

如果还不行，在微信开发者工具的 Network 标签查看具体错误，告诉我详细信息！
