#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}  学途在线课程平台 - 一键部署脚本${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""

# ==========================================
# 1. 检查环境
# ==========================================
echo -e "${YELLOW}[1/5] 检查环境...${NC}"

if ! command -v java &>/dev/null; then
    echo "安装 JDK 17..."
    if command -v apt &>/dev/null; then
        apt update && apt install -y openjdk-17-jdk
    elif command -v yum &>/dev/null; then
        yum install -y java-17-openjdk
    else
        echo "请手动安装 JDK 17"
        exit 1
    fi
fi
echo "  Java: $(java -version 2>&1 | head -1)"

if ! command -v mvn &>/dev/null; then
    echo "安装 Maven..."
    if command -v apt &>/dev/null; then
        apt install -y maven
    elif command -v yum &>/dev/null; then
        yum install -y maven
    fi
fi
echo "  Maven: $(mvn -version 2>&1 | head -1)"

# Docker
if ! command -v docker &>/dev/null; then
    echo "安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker --now
fi
echo "  Docker: $(docker --version)"

# Git
if ! command -v git &>/dev/null; then
    apt install -y git 2>/dev/null || yum install -y git 2>/dev/null
fi

# ==========================================
# 2. 获取代码
# ==========================================
echo ""
echo -e "${YELLOW}[2/5] 拉取代码...${NC}"
if [ -d "Learning-Platform" ]; then
    cd Learning-Platform
    git pull
else
    git clone https://github.com/l932154045-hue/Learning-Platform.git
    cd Learning-Platform
fi

# ==========================================
# 3. 启动基础设施
# ==========================================
echo ""
echo -e "${YELLOW}[3/5] 启动 MySQL / Redis / RabbitMQ...${NC}"
docker compose down 2>/dev/null
docker compose up -d

echo "  等待服务就绪..."
for i in $(seq 1 30); do
    if docker compose ps | grep -q "healthy"; then
        echo "  基础设施已就绪"
        break
    fi
    sleep 2
done
docker compose ps

# ==========================================
# 4. 编译打包
# ==========================================
echo ""
echo -e "${YELLOW}[4/5] 编译项目（首次需下载依赖，约 3-5 分钟）...${NC}"
mvn clean package -DskipTests -q

# ==========================================
# 5. 启动服务
# ==========================================
echo ""
echo -e "${YELLOW}[5/5] 启动微服务...${NC}"

# 关掉旧进程
pkill -f "target/.*\.jar" 2>/dev/null || true
sleep 2

mkdir -p logs

SERVICES=(
    "gateway-service/jar::8080"
    "user-service/jar::8081"
    "course-service/jar::8082"
    "cart-service/jar::8083"
    "order-service/jar::8084"
    "payment-service/jar::8085"
    "learning-service/jar::8086"
    "admin-service/jar::8087"
)

start_service() {
    local jar_file
    jar_file=$(ls $1/target/$1-*.jar 2>/dev/null | head -1)
    if [ -z "$jar_file" ]; then
        echo "  ✗ $1: JAR not found"
        return
    fi
    nohup java -jar "$jar_file" > "logs/$1.log" 2>&1 &
    echo "  ✓ $1 (PID $!)"
}

for entry in "${SERVICES[@]}"; do
    svc="${entry%%::*}"
    start_service "$svc"
done

# ==========================================
# 验证
# ==========================================
echo ""
echo "  等待 Gateway 启动..."
sleep 15

echo ""
echo -e "${GREEN}====================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}====================================${NC}"
echo ""
echo "  测试网关："
if curl -s http://localhost:8080/api/course/hot > /dev/null 2>&1; then
    echo -e "  ${GREEN}✓ Gateway 正常${NC}"
else
    echo "  ✗ Gateway 未响应，查看日志: tail -f logs/gateway-service.log"
fi

echo ""
echo "  快速验证："
echo "  curl http://localhost:8080/api/course/hot"
echo ""
echo "  注册用户："
echo "  curl -X POST http://localhost:8080/api/user/register -H 'Content-Type: application/json' -d '{\"username\":\"test\",\"password\":\"123456\",\"phone\":\"13800138000\"}'"
echo ""
echo "  查看日志："
echo "  tail -f logs/*.log"
