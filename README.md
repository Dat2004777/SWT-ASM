# Staff Management System (Hệ thống Quản lý Nhân viên)

Dự án này là một ứng dụng web quản lý nhân viên được xây dựng trên nền tảng Java Servlet & JSP (Jakarta EE 9+), sử dụng Maven làm công cụ build và Microsoft SQL Server làm cơ sở dữ liệu.

---

## Công nghệ Sử dụng (Technology Stack)

- **Ngôn ngữ**: Java 17 (hoặc phiên bản cao hơn)
- **Công nghệ Web**: Jakarta Servlet API (v5.0/6.0), JSP, JSTL (sử dụng namespace `jakarta.*`)
- **Quản lý Thư viện**: Apache Maven
- **Cơ sở dữ liệu**: Microsoft SQL Server
- **Bảo mật**: BCrypt (`jbcrypt`) để mã hóa mật khẩu
- **Kiểm thử (Testing)**:
  - **Unit Test**: JUnit 5 (Jupiter), Mockito (Mocking database layer)
  - **E2E/UI Test**: Selenium WebDriver, WebDriverManager (Kiểm thử giao diện tự động trên Chrome)
- **Kiểm soát chất lượng mã nguồn**:
  - **Checkstyle**: Google checks
  - **JaCoCo**: Đo lường độ bao phủ mã nguồn (Code Coverage) khi chạy test

---

## Yêu cầu Hệ thống (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

1. **Java JDK 17** trở lên.
2. **Apache Maven 3.6** trở lên.
3. **Microsoft SQL Server** (đã bật cổng `1433` và cho phép kết nối bằng tài khoản SQL Server Authentication).
4. **Apache Tomcat 10.x** (**BẮT BUỘC**: Phải dùng Tomcat 10, không dùng Tomcat 9 hoặc cũ hơn vì dự án sử dụng Jakarta EE với namespace `jakarta.*`).
5. **Google Chrome** (để chạy Selenium UI Test).

---

## Cấu hình Cơ sở dữ liệu (Database Setup)

Ứng dụng được thiết kế tự động khởi tạo cơ sở dữ liệu (Self-initializing) thông qua class listener [DataInitializer.java](file:///d:/Development/FPTU/SU26/SWT302/GroupASM/SWT%20ASM/StaffManagement/src/main/java/fu/swt301/sms/config/DataInitializer.java). Bạn chỉ cần chuẩn bị cơ sở dữ liệu trống theo các bước sau:

1. Mở **SQL Server Management Studio (SSMS)** hoặc công cụ quản lý SQL khác.
2. Tạo một database trống tên là `TestDB`:
   ```sql
   CREATE DATABASE TestDB;
   ```
3. Cấu hình kết nối cơ sở dữ liệu được đặt tại file [DBUtils.java](file:///d:/Development/FPTU/SU26/SWT302/GroupASM/SWT%20ASM/StaffManagement/src/main/java/fu/swt301/sms/utils/DBUtils.java). Mặc định cấu hình như sau:
   - **Driver**: `com.microsoft.sqlserver.jdbc.SQLServerDriver`
   - **URL**: `jdbc:sqlserver://localhost:1433;databaseName=TestDB`
   - **Username**: `sa`
   - **Password**: `sa`

   _(Nếu tài khoản `sa` của bạn có mật khẩu khác, hãy chỉnh sửa trực tiếp trong file `DBUtils.java` trước khi chạy ứng dụng)._

> [!NOTE]
> Khi ứng dụng khởi chạy lần đầu, `DataInitializer` sẽ tự động:
>
> - Tạo các bảng cần thiết (`Role`, `Staff`).
> - Seed dữ liệu mặc định gồm các vai trò (`Admin`, `Staff`), 1 tài khoản quản trị và 1000 tài khoản nhân viên thử nghiệm.

---

## Hướng dẫn Build dự án

Mở Command Prompt/Terminal tại thư mục gốc của dự án và chạy lệnh sau để build ra file WAR:

```bash
# Build dự án (chạy cả bộ test)
mvn clean package

# Build dự án nhanh (bỏ qua chạy test để tiết kiệm thời gian)
mvn clean package -DskipTests
```

Sau khi chạy xong, file `StaffManagement.war` sẽ được tạo ra tại thư mục `target/`.

---

## Hướng dẫn Chạy ứng dụng (Run/Deploy)

### Cách 1: Chạy trực tiếp từ Apache NetBeans (Khuyến nghị)

1. Mở NetBeans và Import dự án (Open Project -> Chọn thư mục `StaffManagement`).
2. Đảm bảo bạn đã cấu hình Server Tomcat 10 trong NetBeans (**Tools** -> **Servers** -> Thêm Tomcat 10.x).
3. Click chuột phải vào dự án -> **Run**. NetBeans sẽ tự động build, deploy lên Tomcat và mở trình duyệt tại địa chỉ `http://localhost:8080/StaffManagement`.

### Cách 2: Deploy thủ công lên Tomcat 10.x độc lập

1. Sao chép file `StaffManagement.war` trong thư mục `target/` vào thư mục `webapps/` của Apache Tomcat 10.
2. Khởi động Tomcat bằng cách chạy file `bin/startup.bat` (Windows) hoặc `bin/startup.sh` (Linux/macOS).
3. Truy cập vào ứng dụng tại: `http://localhost:8080/StaffManagement`.

### Cách 3: Chạy bằng IntelliJ IDEA Ultimate

1. Mở dự án trong IntelliJ dưới dạng dự án Maven.
2. Tạo cấu hình chạy mới (**Run/Debug Configurations**) -> Chọn **Tomcat Server** -> **Local**.
3. Chọn Application Server trỏ tới thư mục Tomcat 10 của bạn.
4. Ở tab **Deployment**, click nút `+` -> **Artifact...** -> Chọn `StaffManagement:war` or `StaffManagement:war exploded`.
5. Đặt **Application context** là `/StaffManagement`.
6. Nhấn **Run** để khởi chạy ứng dụng.

---

## Hướng dẫn Chạy Test

Dự án bao gồm nhiều cấp độ kiểm thử được cấu hình chạy thông qua `maven-surefire-plugin`. Do một số kiểm thử yêu cầu môi trường thực tế (SQL Server/Tomcat), bạn nên chọn chế độ chạy phù hợp:

### 1. Chạy toàn bộ Test

_Yêu cầu: SQL Server đang chạy và ứng dụng đã được deploy lên Tomcat tại `http://localhost:8080/StaffManagement`._

```bash
mvn test
```

### 2. Chạy riêng Unit Test (Khuyến nghị khi code độc lập)

_Các kiểm thử này sử dụng Mockito để giả lập tầng DAO, hoàn toàn độc lập và không cần cài đặt SQL Server hay chạy Web Server._

```bash
mvn test -Dtest=*ServiceTest,*ServletTest,*ValidatorTest,*FilterTest,*UtilsTest
```

### 3. Chạy Integration/DAO Test (Kiểm thử tầng tương tác Database)

_Yêu cầu: SQL Server đang chạy ở cổng 1433 với tài khoản cấu hình trong `DBUtils`._

```bash
mvn test -Dtest=StaffDAOTest
```

### 4. Chạy System/UI Test (Kiểm thử giao diện tự động với Selenium)

_Yêu cầu: Ứng dụng đã chạy thành công trên Tomcat tại địa chỉ `http://localhost:8080/StaffManagement` và máy có sẵn Google Chrome._

```bash
mvn test -Dtest=AuthenticationSystemTest
```

### Xem báo cáo độ bao phủ kiểm thử (JaCoCo)

Sau khi chạy các câu lệnh test, bạn có thể kiểm tra báo cáo độ bao phủ mã nguồn (Code Coverage Report) dạng HTML bằng cách mở file sau trên trình duyệt:
`target/site/jacoco/index.html`

---

## Tài khoản Đăng nhập Mặc định

Hệ thống tự động tạo các tài khoản thử nghiệm sau để bạn đăng nhập:

| Chức vụ / Vai trò         | Email đăng nhập                                | Mật khẩu mặc định | Quyền hạn                                  |
| :------------------------ | :--------------------------------------------- | :---------------- | :----------------------------------------- |
| **Quản trị viên (Admin)** | `admin@example.com`                            | `admin123`        | Toàn quyền (Thêm, Sửa, Xóa, Xem danh sách) |
| **Nhân viên (Staff)**     | `user1@example.com` đến `user1000@example.com` | `user123`         | Chỉ xem danh sách nhân viên (Read-only)    |
