#!/bin/bash
set -e

# ERP ECS 一键初始化脚本（Ubuntu 22.04/24.04）
# 用法：bash setup-ecs.sh

log() { echo "[$(date '+%H:%M:%S')] $1"; }
err() { echo "[$(date '+%H:%M:%S')] ERROR: $1" >&2; exit 1; }

# ===== 配置（请按需修改）=====
DB_NAME="erp_db"
DB_USER="erp_user"
DB_PASSWORD="erp_password_2026"          # ← 改成你的数据库密码
REDIS_PASSWORD=""                         # ← 如需 Redis 密码填这里
JWT_SECRET="erp-jwt-secret-change-me-$(openssl rand -hex 16)"  # 自动生成
RUSTFS_ROOT_USER="admin"
RUSTFS_ROOT_PASSWORD="admin123456"        # ← 改成你的 RustFS 密码
RUSTFS_BUCKET="erp-prod"
RUSTFS_DATA_DIR="/data/rustfs0"
RUSTFS_LOG_DIR="/var/logs/rustfs"
DEPLOY_ROOT="/opt/erp"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ===== 1. 系统更新 + 基础软件 =====
log "1/8 系统更新 + 安装基础软件..."
apt update -y
apt upgrade -y
apt install -y curl wget unzip git ufw

# ===== 2. 安装 JDK 17 =====
log "2/8 安装 JDK 17..."
if ! command -v java &> /dev/null; then
    apt install -y openjdk-17-jdk-headless
fi
log "Java 版本: $(java -version 2>&1 | head -1)"

# ===== 3. 安装 PostgreSQL 15 =====
log "3/8 安装 PostgreSQL..."
if ! command -v psql &> /dev/null; then
    # 优先用系统自带源安装，避免官方源对新发行版（如 Debian Trixie）的兼容问题
    apt update -y
    apt install -y postgresql
fi
systemctl enable --now postgresql

# 创建数据库和用户
log "创建数据库 $DB_NAME 和用户 $DB_USER..."
su - postgres -c "psql -tc \"SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'\" | grep -q 1 || psql -c \"CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';\""
su - postgres -c "psql -tc \"SELECT 1 FROM pg_database WHERE datname='$DB_NAME'\" | grep -q 1 || createdb -O $DB_USER $DB_NAME"
su - postgres -c "psql -c \"ALTER USER $DB_USER WITH PASSWORD '$DB_PASSWORD';\"" 2>/dev/null || true
log "PostgreSQL 就绪"

# ===== 4. 安装 Redis =====
log "4/8 安装 Redis..."
if ! command -v redis-cli &> /dev/null; then
    apt install -y redis-server
fi
if [ -n "$REDIS_PASSWORD" ]; then
    sed -i "s/^# requirepass .*/requirepass $REDIS_PASSWORD/" /etc/redis/redis.conf
fi
systemctl enable --now redis-server
log "Redis 就绪"

# ===== 5. 安装 RustFS（对象存储，官方脚本安装）=====
log "5/8 安装 RustFS..."
if ! command -v rustfs &> /dev/null; then
    curl -O https://rustfs.com/install_rustfs.sh
    bash install_rustfs.sh
    rm -f install_rustfs.sh
fi

mkdir -p "$RUSTFS_DATA_DIR" "$RUSTFS_LOG_DIR"
chmod -R 750 "$RUSTFS_DATA_DIR" "$RUSTFS_LOG_DIR"
cat > /etc/default/rustfs << EOF
RUSTFS_ACCESS_KEY=$RUSTFS_ROOT_USER
RUSTFS_SECRET_KEY=$RUSTFS_ROOT_PASSWORD
RUSTFS_VOLUMES="$RUSTFS_DATA_DIR"
RUSTFS_ADDRESS=":9000"
RUSTFS_CONSOLE_ENABLE=true
RUST_LOG=error
RUSTFS_OBS_LOG_DIRECTORY="$RUSTFS_LOG_DIR"
EOF
systemctl enable --now rustfs 2>/dev/null || true
systemctl restart rustfs 2>/dev/null || true
sleep 2
log "RustFS 就绪（API :9000，控制台 :9001）"

# ===== 6. 安装 Nginx =====
log "6/8 安装 Nginx..."
if ! command -v nginx &> /dev/null; then
    apt install -y nginx
fi
systemctl enable --now nginx
log "Nginx 就绪"

# ===== 7. 创建部署目录结构 =====
log "7/8 创建部署目录..."
mkdir -p "$DEPLOY_ROOT"/{backend,logs,backups,scripts,dist}
mkdir -p /usr/share/erp/web

# ===== 8. 写入配置文件 =====
log "8/8 写入配置文件..."

# 环境变量
cat > "$DEPLOY_ROOT/scripts/env.production" << EOF
# 数据库
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/$DB_NAME
SPRING_DATASOURCE_USERNAME=$DB_USER
SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT
ERP_SECURITY_JWT_SECRET=$JWT_SECRET
ERP_SECURITY_ACCESS_TOKEN_EXPIRE_SECONDS=7200
ERP_SECURITY_REFRESH_TOKEN_EXPIRE_SECONDS=604800

# 对象存储
ERP_STORAGE_ENDPOINT=http://localhost:9000
ERP_STORAGE_ACCESS_KEY=$RUSTFS_ROOT_USER
ERP_STORAGE_SECRET_KEY=$RUSTFS_ROOT_PASSWORD
ERP_STORAGE_BUCKET=$RUSTFS_BUCKET

# Flowable（禁用，节省资源）
FLOWABLE_ASYNC_EXECUTOR_ACTIVATE=false

# JVM
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
EOF

# systemd 服务
if [ -f "$SCRIPT_DIR/erp-admin.service" ]; then
    cp "$SCRIPT_DIR/erp-admin.service" /etc/systemd/system/erp-admin.service
else
    err "未找到 $SCRIPT_DIR/erp-admin.service"
fi
systemctl daemon-reload
systemctl enable erp-admin

# Nginx 配置
if [ -f "$SCRIPT_DIR/nginx.conf" ]; then
    cp "$SCRIPT_DIR/nginx.conf" /etc/nginx/sites-available/erp
else
    err "未找到 $SCRIPT_DIR/nginx.conf"
fi

ln -sf /etc/nginx/sites-available/erp /etc/nginx/sites-enabled/erp
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

# 创建 RustFS bucket（若 rustfs 提供 S3 兼容 API 且凭据已生效）
log "创建对象存储 bucket: $RUSTFS_BUCKET"
if command -v aws &> /dev/null; then
    AWS_ACCESS_KEY_ID="$RUSTFS_ROOT_USER" AWS_SECRET_ACCESS_KEY="$RUSTFS_ROOT_PASSWORD" \
        aws --endpoint-url http://localhost:9000 s3 mb "s3://$RUSTFS_BUCKET" 2>/dev/null || true
else
    apt install -y awscli
    AWS_ACCESS_KEY_ID="$RUSTFS_ROOT_USER" AWS_SECRET_ACCESS_KEY="$RUSTFS_ROOT_PASSWORD" \
        aws --endpoint-url http://localhost:9000 s3 mb "s3://$RUSTFS_BUCKET" 2>/dev/null || true
fi

# ===== 防火墙 =====
log "配置防火墙..."
ufw allow 22/tcp 2>/dev/null || true
ufw allow 80/tcp 2>/dev/null || true
ufw --force enable 2>/dev/null || true

# ===== 完成 =====
echo ""
echo "============================================"
echo "✅ ECS 初始化完成！"
echo "============================================"
echo ""
echo "已安装服务："
echo "  PostgreSQL  : localhost:5432 (数据库: $DB_NAME, 用户: $DB_USER)"
echo "  Redis       : localhost:6379"
echo "  RustFS      : localhost:9000 (控制台 :9001)"
echo "  Nginx       : :80"
echo ""
echo "密码信息（请妥善保存）："
echo "  数据库密码   : $DB_PASSWORD"
echo "  RustFS 用户  : $RUSTFS_ROOT_USER"
echo "  RustFS 密码  : $RUSTFS_ROOT_PASSWORD"
echo "  JWT Secret   : $JWT_SECRET"
echo ""
echo "环境配置文件：$DEPLOY_ROOT/scripts/env.production"
echo ""
echo "下一步：push 代码到 main，GitHub Actions 会自动部署"
echo "============================================"
