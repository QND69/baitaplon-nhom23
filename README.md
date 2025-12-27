Farm Simulation Game (Mô Phỏng Nông Trại)

Chào mừng đến với game mô phỏng nông trại được viết bằng ngôn ngữ Java. Trò chơi cho phép bạn trải nghiệm cuộc sống của một người nông dân: trồng trọt, chăn nuôi, buôn bán và hoàn thành các nhiệm vụ thú vị.

📋 Mục lục

Yêu cầu hệ thống

Cách chạy game

Các phím điều khiển

Hướng dẫn chơi game

Tính năng nổi bật

💻 Yêu cầu hệ thống

Java Development Kit (JDK): Phiên bản 8 trở lên (Khuyến nghị JDK 17 hoặc 21).

Hệ điều hành: Windows, MacOS, hoặc Linux.

IDE (Khuyến nghị): IntelliJ IDEA, Eclipse, hoặc NetBeans để dễ dàng biên dịch và chạy.

🚀 Cách chạy game

Cách 1: Chạy bằng IDE (IntelliJ IDEA, Eclipse) - Khuyên dùng

Mở IDE của bạn.

Chọn File > Open (hoặc Import Project) và trỏ đến thư mục chứa source code (thư mục cha của farmSimulation).

Tìm file farmSimulation/Main.java.

Chuột phải vào file Main.java và chọn Run 'Main'.

Cách 2: Chạy bằng dòng lệnh (Command Line/Terminal)

Mở Terminal hoặc Command Prompt.

Di chuyển đến thư mục chứa mã nguồn (thư mục chứa folder farmSimulation).

Biên dịch mã nguồn:

javac -d bin farmSimulation/Main.java


(Lưu ý: Nếu lệnh trên báo lỗi thiếu file, bạn có thể cần biên dịch toàn bộ thư mục).

Chạy game:

java -cp bin farmSimulation.Main


🎮 Các phím điều khiển

Di chuyển & Nhân vật

W : Di chuyển lên trên.

S: Di chuyển xuống dưới.

A: Di chuyển sang trái.

D: Di chuyển sang phải.

Tương tác & Công cụ

Chuột Trái: * Tương tác với ô đất (Cuốc, Gieo hạt, Tưới nước).

Tương tác với NPC (Cửa hàng, Nhiệm vụ).

Chọn vật phẩm trong Túi đồ.

Phím số (1-9): Chọn công cụ/vật phẩm nhanh trên thanh Hotbar.

E: Mở/Đóng túi đồ (Inventory) hoặc Tương tác (tùy ngữ cảnh).

ESC: Mở Menu Cài đặt (Settings) / Tạm dừng game.

🌾 Hướng dẫn chơi game

1. Khởi tạo nhân vật

Khi bắt đầu game, bạn sẽ vào màn hình Character Creation.

Nhập tên nhân vật của bạn.

Chọn giao diện nhân vật (Sprite) mà bạn thích.

Nhấn Start Game để bắt đầu.

2. Trồng trọt (Cơ bản)

Để kiếm tiền, bạn cần trồng nông sản:

Làm đất: Chọn cái Cuốc (Hoe) trên thanh công cụ và click vào ô đất cỏ để xới đất.

Gieo hạt: Mua hạt giống từ Cửa hàng (Shop), chọn hạt giống trên tay và click vào ô đất đã xới.

Tưới nước: Chọn Bình tưới (Watering Can) và click vào ô đất đã gieo hạt. Đất sẽ sẫm màu lại.

Lưu ý: Cây cần được tưới nước mỗi ngày để lớn lên.

Thu hoạch: Khi cây trưởng thành (hình dạng thay đổi), click chuột để thu hoạch. Nông sản sẽ vào túi đồ của bạn.

3. Cửa hàng (Shop)

Tìm NPC bán hàng hoặc khu vực Shop trên bản đồ.

Click vào Shop để mở giao diện mua bán.

Mua: Chọn hạt giống, công cụ hoặc vật phẩm nâng cấp.

Bán: Chọn nông sản bạn đã thu hoạch để bán lấy Vàng (Gold).

4. Chăn nuôi

Bạn có thể mua gia súc/gia cầm (Gà, Bò, v.v.) từ cửa hàng chăn nuôi.

Đảm bảo cho chúng ăn hàng ngày.

Thu thập sản phẩm (Trứng, Sữa) sau một khoảng thời gian nhất định.

5. Hệ thống Thời gian & Thời tiết

Thời gian: Game có chu kỳ Sáng/Tối. Hãy chú ý thời gian để làm việc hiệu quả.

Ngủ: Khi trời tối hoặc thanh năng lượng (Stamina) cạn kiệt, hãy về nhà và đi ngủ để hồi phục và chuyển sang ngày hôm sau.

Thời tiết: Trời có thể Nắng hoặc Mưa. Nếu trời mưa, bạn không cần tưới cây (tiết kiệm sức lực).

6. Nhiệm vụ (Quests)

Kiểm tra Bảng Nhiệm vụ (Quest Board) để nhận các thử thách.

Hoàn thành nhiệm vụ (ví dụ: Thu hoạch 10 củ cà rốt) để nhận phần thưởng lớn.

✨ Tính năng nổi bật

Hệ thống Inventory & Hotbar: Quản lý vật phẩm mượt mà, kéo thả tiện lợi.

Lưu game (Save/Load): Game tự động lưu hoặc cho phép lưu thủ công để bạn không mất tiến trình.

Hiệu ứng hình ảnh: Hiệu ứng thời tiết (Mưa, Nắng), hiệu ứng khi cuốc đất hay tưới nước.

Âm thanh: Nhạc nền thư giãn và âm thanh tương tác sống động.

Chúc bạn có những giờ phút thư giãn vui vẻ cùng Nông trại của mình!