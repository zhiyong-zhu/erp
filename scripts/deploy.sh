#!/bin/bash
set -e

# ERP 部署脚本 - 在 ECS 上执行
# 用法: ./deploy.sh <版本标签> 或 ./deploy.sh (最新)

DEPLOY_ROOT="/opt/erp"
BACKEND_DIR="${DEPLOY_ROOT}/backend"
FRONTEND_DIR="/usr/share/erp/web"
SERVICE_NAME="erp-admin"
JAR_FILE="erp-admin-1.0.0-SNAPSHOT.jar"
BACKUP_DIR="${DEPLOY_ROOT}/backups"
LOG_DIR="${DEPLOY_ROOT}/logs"
HEALTH_URL="http://localhost:8080/actuator/health"
MAX_RETRIES=30
RETRY_INTERVAL=2

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >&2
    exit 1
}

# --- 前置检查 ---
check_prerequisites() {
    log "检查部署环境..."

    # 检查必要目录
    for dir in "${BACKEND_DIR}" "${FRONTEND_DIR}" "${BACKUP_DIR}" "${LOG_DIR}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            log "创建目录: $dir"
        fi
    done

    # 检查 systemd 服务
    if ! systemctl list-unit-files | grep -q "${SERVICE_NAME}"; then
        log "安装 systemd 服务..."
        cp "${DEPLOY_ROOT}/scripts/erp-admin.service" /etc/systemd/system/
        systemctl daemon-reload
    fi
}

# --- 备份当前版本 ---
backup_current() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_path="${BACKUP_DIR}/${timestamp}"

    if [ -f "${BACKEND_DIR}/${JAR_FILE}" ]; then
        mkdir -p "$backup_path"
        cp "${BACKEND_DIR}/${JAR_FILE}" "$backup_path/"
        log "备份当前版本到: $backup_path"

        # 保留最近 5 个备份
        ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | tail -n +6 | xargs rm -rf 2>/dev/null
        log "清理旧备份，保留最近 5 个"
    fi
}

# --- 部署前端 ---
deploy_frontend() {
    local dist_dir="${DEPLOY_ROOT}/dist"

    if [ ! -d "$dist_dir" ]; then
        log "警告: 未找到前端产物目录 $dist_dir，跳过前端部署"
        return
    fi

    log "部署前端静态文件..."
    rm -rf "${FRONTEND_DIR:?}"/*
    cp -r "${dist_dir}/"* "${FRONTEND_DIR}/"
    chown -R www-data:www-data "${FRONTEND_DIR}" 2>/dev/null || true
    log "前端部署完成"

    # 重载 Nginx
    if systemctl is-active --quiet nginx; then
        nginx -t && systemctl reload nginx
        log "Nginx 已重载"
    fi
}

# --- 部署后端 ---
deploy_backend() {
    local jar_src="${DEPLOY_ROOT}/erp-admin.jar"

    if [ ! -f "$jar_src" ]; then
        error "未找到后端 JAR 文件: $jar_src"
    fi

    log "部署后端 JAR..."
    cp "$jar_src" "${BACKEND_DIR}/${JAR_FILE}"
    log "后端 JAR 已替换"

    # 重启服务
    log "重启 ${SERVICE_NAME} 服务..."
    systemctl restart "${SERVICE_NAME}"

    # 等待启动
    log "等待后端启动..."
    local retries=0
    while [ $retries -lt $MAX_RETRIES ]; do
        if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
            log "后端启动成功 (耗时 $((retries * RETRY_INTERVAL))s)"
            return 0
        fi
        retries=$((retries + 1))
        sleep $RETRY_INTERVAL
    done

    error "后端启动超时！检查日志: journalctl -u ${SERVICE_NAME} -n 50"
}

# --- 健康检查 ---
health_check() {
    log "执行健康检查..."

    # 后端健康检查
    if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
        log "✅ 后端健康检查通过"
    else
        log "⚠️  后端健康检查失败"
    fi

    # Nginx 健康检查
    if curl -sf -o /dev/null http://localhost/ 2>&1; then
        log "✅ Nginx 健康检查通过"
    else
        log "⚠️  Nginx 健康检查失败"
    fi

    # PostgreSQL 检查
    if systemctl is-active --quiet postgresql; then
        log "✅ PostgreSQL 运行中"
    else
        log "⚠️  PostgreSQL 未运行"
    fi

    # Redis 检查
    if systemctl is-active --quiet redis-server 2>/dev/null || redis-cli ping > /dev/null 2>&1; then
        log "✅ Redis 运行中"
    else
        log "⚠️  Redis 未运行"
    fi
}

# --- 清理日志 ---
cleanup_logs() {
    log "清理 7 天前的日志..."
    find "${LOG_DIR}" -name "*.log.*" -mtime +7 -delete 2>/dev/null || true

    # 清理 systemd journal（保留 7 天）
    journalctl --vacuum-time=7d 2>/dev/null || true
}

# --- 主流程 ---
main() {
    log "=========================================="
    log "ERP 部署开始"
    log "=========================================="

    check_prerequisites
    backup_current
    deploy_frontend
    deploy_backend
    health_check
    cleanup_logs

    log "=========================================="
    log "ERP 部署完成"
    log "=========================================="
}

main "$@"
