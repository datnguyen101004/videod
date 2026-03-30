# MovieD (videod)

Dịch vụ backend Spring Boot cho nền tảng chia sẻ video. Hỗ trợ xác thực JWT/OAuth2, tải lên video đơn hoặc đa phần tới DigitalOcean Spaces (tương thích S3), quản lý người dùng, giới hạn tốc độ và giám sát Prometheus/Grafana.

## Tính năng chính

- Đăng ký/đăng nhập bằng JWT, hỗ trợ đăng nhập Google OAuth2 và redirect về FE cùng access token.
- Tải lên video kích thước nhỏ qua presigned URL và xác minh lưu DB; tải lên multipart cho file lớn.
- Danh sách, tải xuống, xóa video; xác minh upload và dọn dẹp multipart (abort).
- Giới hạn tốc độ bằng Bucket4j + Redis, cấu hình CORS mở cho SPA.
- Giám sát qua Spring Actuator, Prometheus và dashboard Grafana; tài liệu OpenAPI/Swagger sẵn có.

## Kiến trúc & công nghệ

- Java 21, Spring Boot 4.0.3 (Web MVC, Security, Data JPA, OAuth2 Client, Actuator).
- Lưu trữ MySQL; Redis cho rate limit; H2 cho môi trường test.
- Lưu trữ đối tượng: DigitalOcean Spaces (S3 client, transfer manager, presigner).
- JWT (jjwt) cho xác thực stateless; BCrypt để mã hóa mật khẩu.
- Prometheus/Grafana cho metrics; Vault (tùy chọn) để lấy cấu hình bí mật.

## Cấu hình môi trường

Tham khảo [src/main/resources/application.yaml](src/main/resources/application.yaml).

| Biến                       | Mô tả                          | Giá trị mẫu                        |
| -------------------------- | ------------------------------ | ---------------------------------- |
| SPRING_DATASOURCE_URL      | JDBC URL MySQL                 | jdbc:mysql://localhost:3306/videod |
| SPRING_DATASOURCE_USERNAME | User DB                        | root                               |
| SPRING_DATASOURCE_PASSWORD | Password DB                    | root                               |
| SPRING_PROFILES_ACTIVE     | Hồ sơ chạy                     | dev                                |
| DO_ACCESS_KEY_ID           | Access key DO Spaces           | <your-key>                         |
| DO_SECRET_KEY              | Secret key DO Spaces           | <your-secret>                      |
| GG_CLIENT_ID               | Google OAuth client id         | <google-client-id>                 |
| GG_CLIENT_SECRET           | Google OAuth client secret     | <google-client-secret>             |
| FE_URL                     | URL frontend để redirect OAuth | http://localhost:3001              |
| SPRING_DATA_REDIS_HOST     | Redis host                     | localhost                          |
| SPRING_DATA_REDIS_PORT     | Redis port                     | 6379                               |

## Chạy cục bộ (Maven)

1. Cài Java 21, Maven, MySQL, Redis.
2. Cập nhật biến môi trường ở trên (hoặc cấu hình trong application.yaml) và tạo DB `videod`.
3. Chạy:

```
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. Swagger UI: http://localhost:8080/swagger-ui/index.html
5. Metrics: http://localhost:8080/actuator/prometheus

## Chạy bằng Docker Compose

Yêu cầu Docker & Docker Compose. Biến môi trường cần: DO_ACCESS_KEY_ID, DO_SECRET_KEY, GG_CLIENT_ID, GG_CLIENT_SECRET, FE_URL.

```
docker compose up -d
```

- Backend: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3030 (login mặc định admin/admin)

## API chính

- Auth: POST /auth/login, POST /auth/register. OAuth2: /oauth2/authorization/google (redirect về FE với token).
- Video nhỏ (<100MB):
  - POST /api/v1/video/upload/small → presigned URL
  - PUT file tới URL trả về
  - POST /api/v1/video/verify lưu metadata
- Video lớn (multipart):
  - POST /api/v1/video/upload/multipart/initiate → uploadId, key
  - Lặp: POST /api/v1/video/upload/multipart/part-url → presigned part URL, PUT từng part
  - POST /api/v1/video/upload/multipart/complete để ghép part
  - POST /api/v1/video/abort để hủy (nếu cần)
- Khác: GET /api/v1/video/all, POST /api/v1/video/download, DELETE /api/v1/video/delete?videoId=ID.

## Workflow

- Đăng ký/đăng nhập JWT:
    - POST /auth/register để tạo tài khoản.
    - POST /auth/login lấy access token; đính kèm Authorization: Bearer <token> cho các API bảo vệ.
    - Token được JwtFilter xác thực, context chứa username cho service.

- Đăng nhập Google OAuth2:
  - Truy cập /oauth2/authorization/google.
  - Sau khi Google cấp quyền, backend tạo user (nếu chưa có), sinh access + refresh token.
  - Backend set cookie refresh_token, redirect về FE_URL/login/success?token=<accessToken>.

- Upload video nhỏ (<100MB):
  - POST /api/v1/video/upload/small với metadata → nhận presigned URL và key.
  - PUT file trực tiếp lên URL đó.
  - POST /api/v1/video/verify với key + metadata để lưu DB.

- Upload video lớn (multipart ≥100MB):
  - POST /api/v1/video/upload/multipart/initiate → nhận uploadId, key.
  - Lặp: POST /api/v1/video/upload/multipart/part-url lấy URL cho từng part, PUT dữ liệu part.
  - POST /api/v1/video/upload/multipart/complete kèm danh sách partNumber + eTag để ghép.
  - Nếu lỗi, POST /api/v1/video/abort với uploadId, key để dọn dẹp.

- Tải xuống/xóa video:
    - Download: POST /api/v1/video/download với key → trả link tải.
    - Xóa: DELETE /api/v1/video/delete?videoId=ID (yêu cầu role USER/ADMIN).

- Giới hạn tốc độ và quan sát:
    - Bucket4j + Redis áp dụng mặc định 100 yêu cầu/phút/bucket.
    - Sức khỏe/metrics: /actuator/health, /actuator/prometheus; quan sát qua Prometheus/Grafana.

## Ghi chú phát triển

- Security: JWT filter trước UsernamePasswordAuthenticationFilter; endpoints public bao gồm swagger, health, metrics, /api/v1/video/all.
- CORS: mở \* cho dev; điều chỉnh trong SecurityFilter nếu cần.
- H2 test profile: [src/main/resources/application-test.yaml](src/main/resources/application-test.yaml) tạo schema in-memory khi `mvn test`.
- Rate limit: Bucket4j cấu hình sẵn 100 token/phút; Redis template đã khai báo.

## Kiểm thử nhanh

- Kiểm tra auth: đăng ký, sau đó login để lấy JWT (Authorization: Bearer <token>).
- Kiểm tra upload nhỏ: làm theo 3 bước ở mục API.
- Kiểm tra OAuth: cấu hình GG_CLIENT_ID/SECRET và FE_URL, truy cập /oauth2/authorization/google, quan sát redirect về FE cùng token.