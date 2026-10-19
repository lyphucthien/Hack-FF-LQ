# LQ Assistant (Android)

Ứng dụng companion độc lập cho Liên Quân Mobile, Free Fire và Free Fire MAX. Dự án dùng Java + Android SDK, có thể mở và sửa bằng VS Code. Workflow GitHub Actions build APK trên máy chủ, không cần cài Android Studio trên máy của bạn.

## Có gì trong bản 1.0

- Nhận diện package Liên Quân (`com.garena.game.kgvn`), Free Fire (`com.dts.freefireth`) và Free Fire MAX (`com.dts.freefiremax`) qua Android Usage Access.
- Khi nhận diện một game, gửi thông báo; chạm vào thông báo sẽ mở trang hướng dẫn tương ứng.
- Có trang hướng dẫn chiến thuật riêng cho Liên Quân và Free Fire.
- Cho phép mở game đã cài từ màn hình chính.
- Có nút bật/tắt giám sát; không chạy giám sát trước khi người dùng bật.

Đây là companion app. Ứng dụng **không** đọc/ghi bộ nhớ game, không tự ngắm, không dò đối thủ bị ẩn, không mở khóa skin, không chèn mã vào game và không tự vẽ bảng đè lên trận đấu.

## Build APK mà không cài Android Studio

Cần tài khoản GitHub để dùng GitHub Actions.

1. Giải nén ZIP này.
2. Tạo repository mới trên GitHub, ví dụ `LQ-Assistant-Android`.
3. Đưa toàn bộ nội dung bên trong thư mục `LQ-Assistant-Android` lên repository, giữ nguyên cấu trúc thư mục `.github/workflows/build-apk.yml`, `app/`, `build.gradle`, `settings.gradle`.
4. Vào tab **Actions**, chọn workflow **Build Android APK**, rồi nhấn **Run workflow**. Lần push đầu lên nhánh `main` cũng tự kích hoạt build.
5. Khi workflow có dấu tích xanh, mở lần chạy đó, tìm **Artifacts**, tải `LQ-Assistant-APK.zip` về rồi giải nén. APK nằm trong đó với tên `app-debug.apk`.

## Cài và dùng trên Android

1. Cài `app-debug.apk` trên điện thoại Android của bạn.
2. Mở LQ Assistant, bấm **Cấp quyền truy cập sử dụng**.
3. Trong màn hình Android, tìm **LQ Assistant** và bật quyền cho phép xem thông tin sử dụng ứng dụng; quay lại app.
4. Nếu Android hỏi quyền thông báo, chọn cho phép.
5. Bấm **Bắt đầu nhận diện game**, rồi mở Liên Quân hoặc Free Fire.
6. Khi app nhận ra game, Android sẽ hiển thị thông báo. Chạm vào đó để mở trang hướng dẫn riêng.
7. Để dừng, mở LQ Assistant và bấm **Dừng nhận diện**.

## Lưu ý tương thích

- Yêu cầu Android 8.0 trở lên (API 26+).
- Tên menu quyền Usage Access có thể khác tùy hãng điện thoại.
- Một số ROM có thể hạn chế dịch vụ nền; nếu nhận diện bị ngừng, bỏ tối ưu pin cho LQ Assistant trong phần cài đặt của Android.
- Thông báo game cần quyền thông báo trên Android 13 trở lên.
- Bản build debug phù hợp để thử nghiệm; phát hành công khai nên tạo bản release có chữ ký riêng và bổ sung chính sách quyền riêng tư giải thích rõ quyền Usage Access.
