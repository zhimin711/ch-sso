# ch-sso
### 上传配置 
```
scp -r src/main/docker/Dockerfile zhimin@192.168.199.194:/home/zhimin/docker/ch-sso
scp -r build/libs/ch-sso-1.0.0-SNAPSHOT.jar zhimin@192.168.199.194:/home/zhimin/docker/ch-sso
```
### 打包
```
docker build -t ch-sso:v1 /home/zhimin/docker/ch-sso
```

### 启动
```
docker run --name ch-sso \
--net=none \
 -v /home/zhimin/share/logs:/mnt/share/logs  \
 -m 512M --memory-swap -1 \
-d ch-sso:v1 ;
```
```
docker run --name ch-sso \
-p 7000:7000 \
 -v /home/zhimin/share/logs:/mnt/share/logs  \
 -m 512M --memory-swap -1 \
-d ch-sso:v1 ;
```
### 重启 停止 删除
```
docker restart ch-sso;

docker stop ch-sso;
docker rm ch-sso;
docker rmi ch-sso:v1;

```

### 分配网络
```
pipework br0 ch-sso 192.168.1.20/24@192.168.1.1;
```