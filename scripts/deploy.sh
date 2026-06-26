#!/bin/bash
set -e

# ERP 部署脚本 - 在 ECS 上执行
# 由 GitHub Actions 调用，处理：停服 → 备份 → 部署 → 启动 → 健康检查

DEPLOY_ROOT="/opt/erp"
BACKEND_DIR="${DEPLOY_ROOT}/backend"
FRONTEND_DIR="/usr/share/erp/web"
SERVICE_NAME="erp-admin"
JAR_FILE="erp-admin-1.0.0-SNAPSHOT.jar"
BACKUP_DIR="${DEPLOY_ROOT}/backups"
HEALTH_URL="http://localhost:8080/actuator/health"
MAX_RETRIES=30
RETRY_INTERVAL=2

log() { echo "[$(date '+%H:%M:%S')] $1"; }
err() { echo "[$(date '+%H:%M:%S')] ERROR: $1" >&2; exit 1; }

# --- 1. 停止当前服务 ---
stop_service() {
    log "停止 ${SERVICE_NAME} 服务..."
    systemctl stop "${SERVICE_NAME}" 2>/dev/null || log "服务未运行，跳过停止"
}

# --- 2. 备份当前版本 ---
backup_current() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local backup_path="${BACKUP_DIR}/${timestamp}"

    if [ -f "${BACKEND_DIR}/${JAR_FILE}" ]; then
        mkdir -p "$backup_path"
        cp "${BACKEND_DIR}/${JAR_FILE}" "$backup_path/"
        # 前端也备份
        if [ -d "${FRONTEND_DIR}" ] && [ "$(ls -A ${FRONTEND_DIR} 2>/dev/null)" ]; then
            tar czf "$backup_path/web.tar.gz" -C "${FRONTEND_DIR}" .
        fi
        log "备份当前版本到: $backup_path"
        # 保留最近 5 个备份
        ls -dt "${BACKUP_DIR}"/*/ 2>/dev/null | tail -n +6 | xargs rm -rf 2>/dev/null || true
    fi
}

# --- 3. 部署后端 ---
deploy_backend() {
    local jar_src="${DEPLOY_ROOT}/erp-admin.jar"
    if [ ! -f "$jar_src" ]; then
        log "未找到新 JAR，跳过后端部署"
        return
    fi
    log "部署后端 JAR..."
    mv "$jar_src" "${BACKEND_DIR}/${JAR_FILE}"
    log "后端 JAR 已就位"
}

# --- 4. 部署前端 ---
deploy_frontend() {
    local pkg="${DEPLOY_ROOT}/web-dist.tar.gz"
    if [ ! -f "$pkg" ]; then
        log "未找到前端包，跳过前端部署"
        return
    fi
    log "部署前端（解压 tar.gz）..."
    rm -rf "${FRONTEND_DIR:?}"/*
    tar xzf "$pkg" -C "${FRONTEND_DIR}/"
    rm -f "$pkg"
    chown -R www-data:www-data "${FRONTEND_DIR}" 2>/dev/null || true
    log "前端部署完成"

    # 重载 Nginx
    if systemctl is-active --quiet nginx; then
        nginx -t 2>/dev/null && systemctl reload nginx
        log "Nginx 已重载"
    fi
}

# --- 5. 启动服务 ---
start_service() {
    log "启动 ${SERVICE_NAME} 服务..."
    systemctl start "${SERVICE_NAME}"

    log "等待后端启动..."
    local retries=0
    while [ $retries -lt $MAX_RETRIES ]; do
        if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
            log "后端启动成功（耗时 $((retries * RETRY_INTERVAL))s）"
            return 0
        fi
        retries=$((retries + 1))
        sleep $RETRY_INTERVAL
    done
    err "后端启动超时！查看日志: journalctl -u ${SERVICE_NAME} -n 50"
}

# --- 6. 健康检查 ---
health_check() {
    log "健康检查..."
    curl -sf "$HEALTH_URL" > /dev/null 2>&1 && log "✅ 后端正常" || log "⚠️ 后端异常"
    curl -sf -o /dev/null http://localhost/ 2>&1 && log "✅ Nginx 正常" || log "⚠️ Nginx 异常"
    systemctl is-active --quiet postgresql && log "✅ PostgreSQL 正常" || log "⚠️ PostgreSQL 异常"
    systemctl is-active --quiet redis-server 2>/dev/null && log "✅ Redis 正常" || log "⚠️ Redis 异常"
    systemctl is-active --quiet rustfs 2>/dev/null && log "✅ RustFS 正常" || log "⚠️ RustFS 异常"
}

# --- 7. 清理日志 ---
cleanup_logs() {
    find "${DEPLOY_ROOT}/logs" -name "*.log.*" -mtime +7 -delete 2>/dev/null || true
    journalctl --vacuum-time=7d 2>/dev/null || true
}

# --- 主流程 ---
main() {
    log "=========================================="
    log "ERP 部署开始"
    log "=========================================="
    stop_service
    backup_current
    deploy_backend
    deploy_frontend
    start_service
    health_check
    cleanup_logs
    log "=========================================="
    log "ERP 部署完成"
    log "=========================================="
}

main "$@"
