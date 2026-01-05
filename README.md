# Smart Light Backend - Java Spring Boot

Backend để quản lý và lưu trữ dữ liệu từ ESP32 Smart Light Control.

## Tính năng
- Kết nối MQTT để nhận dữ liệu từ ESP32
- Lưu lịch sử dòng điện, công suất, trạng thái đèn vào MySQL
- REST API để điều khiển đèn và xem lịch sử
- Thống kê dữ liệu

## Yêu cầu
- Java 17+
- Maven
- MySQL Server
- MQTT Broker (Mosquitto) đang chạy trên `192.168.110.40:1883`

## Cài đặt

### 1. Tạo database MySQL
```sql
CREATE DATABASE smart_light_db;
```

### 2. Cấu hình
Chỉnh sửa `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
mqtt.broker.url=tcp://192.168.110.40:1883
```

### 3. Chạy ứng dụng
```bash
cd smart_light_backend
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

## API Endpoints

### 1. Điều khiển đèn
```bash
POST http://localhost:8080/api/light/control
Content-Type: application/json

{
  "command": "1"  // "1" = bật, "0" = tắt
}
```

### 2. Xem lịch sử
```bash
GET http://localhost:8080/api/light/history
GET http://localhost:8080/api/light/history?start=2025-12-22T00:00:00&end=2025-12-23T00:00:00
```

### 3. Xem dữ liệu mới nhất
```bash
GET http://localhost:8080/api/light/latest
```

### 4. Xem thống kê
```bash
GET http://localhost:8080/api/light/stats
```

Response:
```json
{
  "totalRecords": 150,
  "avgCurrent": 0.728,
  "avgPower": 160.2,
  "onCount": 100,
  "offCount": 50
}
```

## Cấu trúc Database

### Table: light_data
| Column    | Type         | Description           |
|-----------|--------------|-----------------------|
| id        | BIGINT       | Primary key           |
| status    | VARCHAR(10)  | "on" hoặc "off"       |
| current   | DOUBLE       | Dòng điện (A)         |
| power     | DOUBLE       | Công suất (W)         |
| timestamp | DATETIME     | Thời gian ghi nhận    |

## MQTT Topics
- Subscribe: `/light/status`, `/light/current`, `/light/power`
- Publish: `/light/control`

## Test với Postman
1. Bật đèn: `POST /api/light/control` với body `{"command": "1"}`
2. Tắt đèn: `POST /api/light/control` với body `{"command": "0"}`
3. Xem lịch sử: `GET /api/light/history`
4. Xem thống kê: `GET /api/light/stats`
