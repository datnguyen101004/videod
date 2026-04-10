# Videod

Dịch vụ backend Spring Boot cho nền tảng chia sẻ video. Hỗ trợ xác thực JWT/OAuth2, tải lên video đơn hoặc đa phần tới DigitalOcean Spaces (tương thích S3), quản lý người dùng, giới hạn tốc độ và giám sát Prometheus/Grafana.

## Tính năng chính

- **Xác thực & Phân quyền:** Đăng ký/đăng nhập bảo mật qua JWT (Stateless) và tích hợp đăng nhập một chạm (SSO) với Google OAuth2.
- **Quản lý Upload Tối ưu:** Áp dụng luồng tải lên trực tiếp qua Presigned URL. Kiến trúc kết hợp Multipart Upload & Presigned URL chuyên biệt cho các tệp dung lượng lớn (>100MB), giúp tối ưu hiệu năng và độ ổn định.
- **Kiểm soát lưu lượng (Rate Limiting):** Bảo vệ hệ thống khỏi lạm dụng cấp độ API (Plan-based) nhờ tích hợp thuật toán Token Bucket thông qua Bucket4j và Redis.
- **Tối ưu hiệu năng:** Tính năng phân trang với Keyset-based và kết hợp với Fluent API cho việc phân trang với tính năng tìm kiếm sử dụng Criteria API.
- **Observability** Thu thập metrics và theo dõi rủi ro hệ thống bằng Spring Actuator, Prometheus, trực quan hóa trên Grafana. Tích hợp sẵn OpenAPI/Swagger 3.0.
- **Kiểm thử mã nguồn:** Đảm bảo tính đúng đắn của service với Mockito cho Unit Test và JUnit (kết hợp H2 / Testcontainers) cho Integration Test.
- **DevOps & Triển khai:** Quản lý thay đổi schema DB với Flyway. Tích hợp CI/CD tự động hoá qua GitHub Actions, nâng cao bảo mật bằng Cloudflare (Reverse Proxy & HTTPS).

## Kiến trúc & Công nghệ

- **Ngôn ngữ & Framework:** Java 21 LTS, Spring Boot 4.0.3 (Web MVC, Security, Data JPA, OAuth2 Client).
- **Database & Caching:** MySQL (Môi trường Producton), Redis (Bộ đệm & Rate limiting phân tán), H2 (Dành cho môi trường Test).
- **Lưu trữ Dữ liệu (Object Storage):** DigitalOcean Spaces (S3-compatible API) cùng AWS V2 SDK (S3 Client, Transfer Manager, Presigner).
- **Security:** Spring Security, JWT (jjwt) xử lý xác thực Stateless, mã hóa mật khẩu với BCrypt, HashiCorp Vault (tùy chọn) lưu trữ secret.
- **Vận hành (DevOps / Monitor):** Docker, Docker Compose, Flyway, Prometheus, Grafana, GitHub Actions.

## Cấu hình môi trường

Tham khảo [src/main/resources/application.yaml](src/main/resources/application.yaml).

| Biến                       | Mô tả                          | Giá trị mẫu                         |
| -------------------------- | ------------------------------ | ----------------------------------- |
| SPRING_DATASOURCE_URL      | JDBC URL MySQL                 | jdbc:mysql://localhost:3306/videod  |
| SPRING_DATASOURCE_USERNAME | User DB                        | root                                |
| SPRING_DATASOURCE_PASSWORD | Password DB                    | root                                |
| SPRING_PROFILES_ACTIVE     | Hồ sơ chạy                     | dev                                 |
| DO_ACCESS_KEY_ID           | Access key DO Spaces           | <your-key>                          |
| DO_SECRET_KEY              | Secret key DO Spaces           | <your-secret>                       |
| GG_CLIENT_ID               | Google OAuth client id         | <google-client-id>                  |
| GG_CLIENT_SECRET           | Google OAuth client secret     | <google-client-secret>              |
| FE_URL                     | URL frontend để redirect OAuth | http://localhost:3001               |
| REDIS_URL                  | Redis URL                      | redis://localhost:6379              |
| JWT_SECRET_KEY             | Khóa bí mật (base64) của JWT   | Y0doaGJX... (chuỗi 64 ký tự base64) |
| ENVIRONMENT                | Tên môi trường                 | development, production             |

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

- Backend: http://localhost:8081 (Được map từ 8080 của container)
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
  - POST /api/v1/video/abort để hủy
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

- Quản lý người dùng:
  - GET /api/v1/user/all (ADMIN): Lấy danh sách toàn bộ người dùng.
  - GET /api/v1/user/myvideo (USER/ADMIN): Lấy danh sách video mà bản thân đã đăng tải.

- Giới hạn tốc độ và quan sát:
  - Bucket4j + Redis áp dụng cho api với 3 plan: FREE, PREMIUM, MAX_PREMIUM.
    - Plan FREE: tối đa 5 requests / 10 giây (hồi 1 token mỗi 10 giây).
    - Plan PREMIUM: tối đa 20 requests / 10 giây (hồi 4 tokens mỗi 10 giây).
    - Plan MAX_PREMIUM: tối đa 1000 requests / 10 giây (hồi 10 tokens mỗi 10 giây).
  - Sức khỏe/metrics: /actuator/health, /actuator/prometheus; quan sát qua Prometheus/Grafana.

## Ghi chú phát triển

- Security: JWT filter trước UsernamePasswordAuthenticationFilter; endpoints public bao gồm swagger, health, metrics, /api/v1/video/all.
- CORS: mở \* cho dev; điều chỉnh trong SecurityFilter nếu cần.
- H2 test profile: [src/main/resources/application-test.yaml](src/main/resources/application-test.yaml) tạo schema in-memory khi dùng lệnh mvn test.
- Rate limit: Bucket4j cấu hình sẵn 100 token/phút; Redis template đã khai báo.

## Kiểm thử nhanh

- Kiểm tra auth: đăng ký, sau đó login để lấy JWT (Authorization: Bearer <token>).
- Kiểm tra upload nhỏ: làm theo 3 bước ở mục API.
- Kiểm tra OAuth: cấu hình GG_CLIENT_ID/SECRET và FE_URL, truy cập /oauth2/authorization/google, quan sát redirect về FE cùng token.
