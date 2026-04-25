# AntiSpam-Call-Android

Ứng dụng Android hỗ trợ nhận diện, cảnh báo và chặn các cuộc gọi lừa đảo hoặc làm phiền.  
Dự án bài tập lớn học phần "Lập trình thiết bị di động".

## Tính Năng Chính

- Đăng nhập bằng số điện thoại và OTP
- Nhận diện cuộc gọi theo thời gian thực
- Hiển thị cảnh báo bằng overlay
- Tự động chặn cuộc gọi spam
- Quản lý danh sách chặn cá nhân
- Tra cứu số điện thoại từ dữ liệu cộng đồng
- Báo cáo số spam và xác thực số an toàn
- Lưu nhật ký cuộc gọi
- Hiển thị thống kê cơ bản
- Đồng bộ dữ liệu nền từ Firebase về local database

## Công Nghệ Sử Dụng

- **Ngôn Ngữ:** Java
- **Min SDK:** API 26
- **Target SDK:** API 34
- **Giao Diện:** XML, Material Components, View Binding
- **Điều Hướng:** Activity, Fragment, BottomNavigationView
- **Cơ Sở Dữ Liệu Cục Bộ:** Room Database
- **Dữ Liệu Trực Tuyến:** Firebase Firestore
- **Xác Thực:** Firebase Authentication
- **Tác Vụ Nền:** WorkManager
- **Lưu Thiết Lập:** SharedPreferences
- **Hiển Thị Danh Sách:** RecyclerView

## Cấu Trúc Dự Án
- ui/      Giao diện và màn hình chức năng
- core/    Xử lý cuộc gọi, nhận diện spam, overlay
- data/    Database, DAO, entity, worker đồng bộ

## Hướng Dẫn Chạy Dự Án
- Clone dự án: git clone https://github.com/Hoi-ae-BTL/AntiSpam-Call-Android.git
- Mở bằng Android Studio
- Chờ Gradle Sync hoàn tất
- Cấu hình Firebase nếu cần (google-services.json)
- Chạy trên máy ảo hoặc thiết bị thật
- Cấp đầy đủ quyền hệ thống khi ứng dụng yêu cầu
## Quyền Cần Cấp
- READ_PHONE_STATE
- READ_CALL_LOG
- ANSWER_PHONE_CALLS
- READ_CONTACTS
- Overlay Permission
- Call Screening Role (nếu thiết bị hỗ trợ)
## Lưu Ý
Xác thực OTP hiện chủ yếu được kiểm thử bằng số điện thoại thử nghiệm và mã OTP thiết lập sẵn trên Firebase.
Một số chức năng liên quan đến cuộc gọi và overlay hoạt động tốt hơn trên thiết bị thật.
Kết quả nhận diện phụ thuộc vào dữ liệu cục bộ, dữ liệu cộng đồng và trạng thái quyền hệ thống.

## Nhóm Phát Triển
- Thế (Leader): Core System, xử lý cuộc gọi, quản lý dự án
- Hưng: Database Schema, Room Database
- Hà: UI/UX, Main Activity, giao diện chính
- Tuấn: Overlay Alert Window
## Mục Đích
Dự án được thực hiện phục vụ mục đích học tập và nghiên cứu trong khuôn khổ môn học.
