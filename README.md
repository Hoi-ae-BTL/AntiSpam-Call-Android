# AntiSpam-Call-Android

Dự án phát triển ứng dụng nhận diện và chặn cuộc gọi lừa đảo/làm phiền trên nền tảng Android. Bài tập lớn môn học Phát triển Ứng dụng Di động.

## 🚀 Tính năng chính

- **Nhận diện cuộc gọi:** Tự động tra cứu số điện thoại gọi đến theo thời gian thực.
- **Cảnh báo thông minh (Overlay):** Hiển thị cửa sổ cảnh báo đè lên màn hình gọi điện nếu phát hiện số có dấu hiệu spam.
- **Tự động chặn (Auto-block):** Tự động ngắt kết nối với các số điện thoại nằm trong danh sách đen (Blacklist) hoặc có điểm tín nhiệm cực thấp.
- **Cơ sở dữ liệu cục bộ & Đám mây:** Quản lý quy tắc chặn bằng Room Database và đồng bộ dữ liệu cộng đồng.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ:** Java
- **Minimum SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 34
- **Kiến trúc:** Clean Architecture (Core, Data, UI, Domain)
- **Database:** SQLite (thông qua Room Persistence Library)

## 👥 Nhóm phát triển

1. **Thế (Leader):** Xử lý Core System (CallReceiver, Service) & Quản lý dự án.
2. **Hưng:** Thiết kế Database Schema & Tích hợp Room DB.
3. **Hà:** Thiết kế UI/UX & Main Activity.
4. **Tuấn:** Phát triển module Overlay Alert Window.

## ⚙️ Hướng dẫn cài đặt

1. Clone dự án về máy: `git clone https://github.com/Hoi-ae-BTL/AntiSpam-Call-Android.git`
2. Mở dự án bằng Android Studio (Bản mới nhất).
3. Đợi Gradle Sync thành công.
4. Chạy ứng dụng trên máy ảo hoặc thiết bị thật (Yêu cầu cấp quyền quản lý cuộc gọi ở lần chạy đầu tiên).
