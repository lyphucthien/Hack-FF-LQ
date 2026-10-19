package com.lpt.lqassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_POST_NOTIFICATIONS = 301;
    private static final int REQ_PACKAGE_ACCESS_HELP = 302;
    private String page = "home";
    private String selectedGame = "";
    private TextView statusView;
    private LinearLayout content;
    private boolean startAfterPermission = false;

    private final int bg = Color.rgb(16, 11, 28);
    private final int card = Color.rgb(28, 19, 39);
    private final int purple = Color.rgb(115, 43, 190);
    private final int white = Color.rgb(248, 245, 255);
    private final int muted = Color.rgb(198, 187, 211);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedGame = getIntent().getStringExtra("selectedGame");
        if (selectedGame == null) {selectedGame = "";}
        if ("lq".equals(selectedGame)) {page = "lq";}
        else if ("ff".equals(selectedGame)) {page = "ff";}

        render();
        requestPermissionsOnFirstLaunch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        selectedGame = intent.getStringExtra("selectedGame");
        if (selectedGame == null) selectedGame = "";
        if ("lq".equals(selectedGame)) page = "lq";
        else if ("ff".equals(selectedGame)) page = "ff";
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();

        if (startAfterPermission && hasUsageAccess()) {
            startAfterPermission = false;

            if (hasNotificationPermission()) {
                startMonitorService();
            } else {
                Toast.makeText(this,"Chưa có quyền thông báo.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(18), dp(18), dp(18), dp(24));
        scroll.addView(outer, new ScrollView.LayoutParams(-1, -2));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(20), dp(18), dp(20));
        header.setBackground(roundDrawable(new int[]{Color.rgb(49, 16, 91), Color.rgb(114, 27, 101)}, dp(18)));
        TextView title = label("✦ LQ Assistant", 25, white, true);
        header.addView(title);
        TextView subtitle = label("GAME COMPANION  •  BẢN ANDROID", 12, muted, true);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.topMargin = dp(7);
        header.addView(subtitle, subParams);
        outer.addView(header);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, dp(12), 0, dp(12));
        nav.addView(navButton("Trang chính", "home"), weightParams());
        nav.addView(navButton("Liên Quân", "lq"), weightParams());
        nav.addView(navButton("Free Fire", "ff"), weightParams());
        outer.addView(nav);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        outer.addView(content, new LinearLayout.LayoutParams(-1, -2));

        if ("lq".equals(page)) renderLq();
        else if ("ff".equals(page)) renderFf();
        else renderHome();

        TextView footer = label("Ứng dụng độc lập • Không sửa đổi dữ liệu game", 11, muted, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(18);
        outer.addView(footer, fp);
        setContentView(scroll);
        updateStatus();
    }

    private void renderHome() {
        addSectionTitle("Nhận diện ứng dụng đang mở");
        addBody("Khi mày bật giám sát rồi mở Liên Quân hoặc Free Fire, ứng dụng sẽ gửi thông báo nhận diện. Chạm thông báo để mở đúng trang hướng dẫn.");

        statusView = label("Đang kiểm tra quyền…", 13, Color.rgb(217, 165, 255), true);
        statusView.setPadding(dp(12), dp(12), dp(12), dp(12));
        statusView.setBackground(roundDrawable(new int[]{card, card}, dp(10)));
        content.addView(statusView, marginParams(0, 0, 0, 10));

        addButton("① Cấp quyền truy cập sử dụng", v -> openUsageSettings());
        addButton("② Bắt đầu nhận diện game", v -> requestStartMonitor());
        addButton("Dừng nhận diện", v -> stopMonitorService());

        addSectionTitle("Mở game đã cài");
        addButton("Mở Liên Quân Mobile", v -> launchGame("com.garena.game.kgvn"));
        addButton("Mở Free Fire", v -> launchGame("com.dts.freefireth"));
        addButton("Mở Free Fire MAX", v -> launchGame("com.dts.freefiremax"));

        addSectionTitle("Cách nhận diện hoạt động");
        addBody("Android yêu cầu mày tự cấp quyền Usage Access trong Cài đặt. Giám sát chỉ chạy sau khi mày chủ động bật và có thể dừng bất cứ lúc nào. Bản này dùng thông báo, không phủ bảng lên trên trận đấu.");
    }

    private void renderLq() {
        addSectionTitle("Liên Quân Mobile");
        addBody("Trang hướng dẫn nhanh dành cho Liên Quân. Các mục bên dưới là ghi chú chiến thuật, không đọc dữ liệu trận đấu.");
        addInfoCard("🗺 Bản đồ", "Quan sát minimap thường xuyên, ping khi đối thủ mất dạng và tránh đi lẻ khi không có tầm nhìn.");
        addInfoCard("⚔️ Đi đường", "Dọn lính trước khi đảo đường; ưu tiên phối hợp cùng đồng đội thay vì giao tranh khi thiếu người.");
        addInfoCard("🛡 Trang bị", "Điều chỉnh trang bị theo đội hình đối thủ và tình huống trận đấu; không nhất thiết giữ nguyên một bộ đồ mọi trận.");
        addInfoCard("🎯 Mục tiêu", "Cân nhắc Rồng, Caesar và trụ khi vừa thắng giao tranh; kiểm tra vị trí đồng đội trước khi bắt mục tiêu.");
        addButton("Quay về trang chính", v -> { page = "home"; render(); });
    }

    private void renderFf() {
        addSectionTitle("Free Fire");
        addBody("Trang hướng dẫn nhanh dành cho Free Fire / Free Fire MAX.");
        addInfoCard("🎯 Độ nhạy", "Điều chỉnh độ nhạy từ từ trong phần cài đặt chính thức và thử ở khu huấn luyện trước khi vào trận.");
        addInfoCard("🧭 Tập ngắm", "Luyện kéo tâm vào mục tiêu trong khu huấn luyện; ưu tiên độ ổn định thay vì thay đổi nhiều thông số cùng lúc.");
        addInfoCard("👂 Âm thanh", "Dùng âm thanh trong game để định hướng bước chân và tiếng súng; để ý vị trí vật che chắn.");
        addInfoCard("🎒 Sinh tồn", "Chọn vị trí đáp phù hợp, thu thập vật phẩm cần thiết và di chuyển theo vùng an toàn.");
        addButton("Quay về trang chính", v -> { page = "home"; render(); });
    }

    private void addSectionTitle(String text) {
        TextView t = label(text, 18, white, true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(10);
        p.bottomMargin = dp(8);
        content.addView(t, p);
    }

    private void addBody(String text) {
        TextView t = label(text, 14, muted, false);
        t.setLineSpacing(dp(3), 1.0f);
        content.addView(t, marginParams(0, 0, 0, 10));
    }

    private void addInfoCard(String title, String text) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(roundDrawable(new int[]{card, card}, dp(12)));
        TextView heading = label(title, 15, white, true);
        box.addView(heading);
        TextView desc = label(text, 13, muted, false);
        desc.setLineSpacing(dp(2), 1.0f);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(-1, -2);
        dp2.topMargin = dp(5);
        box.addView(desc, dp2);
        content.addView(box, marginParams(0, 0, 0, 9));
    }

    private void addButton(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(white);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(roundDrawable(new int[]{purple, Color.rgb(88, 32, 143)}, dp(11)));
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setOnClickListener(listener);
        content.addView(b, marginParams(0, 0, 0, 9));
    }

    private Button navButton(String text, String destination) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTextColor(white);
        b.setPadding(dp(2), dp(4), dp(2), dp(4));
        b.setBackground(roundDrawable(
                new int[]{page.equals(destination) ? purple : card, page.equals(destination) ? purple : card}, dp(10)));
        b.setOnClickListener(v -> { page = destination; render(); });
        return b;
    }

    private void updateStatus() {
        if (statusView == null) return;
        boolean usage = hasUsageAccess();
        boolean monitoring = getSharedPreferences("state", MODE_PRIVATE).getBoolean("monitoring", false);
        String text = "Usage Access: " + (usage ? "ĐÃ CẤP" : "CHƯA CẤP") +
                "\nGiám sát: " + (monitoring ? "ĐANG BẬT" : "ĐANG TẮT") +
                "\nThông báo: " + (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ? "SẴN SÀNG" : "CHƯA CẤP");
        statusView.setText(text);
    }

    private void requestStartMonitor() {
        if (!hasNotificationPermission()) {
            startAfterPermission = true;

            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_POST_NOTIFICATIONS);

            return;
        }

        if (!hasUsageAccess()) {
            startAfterPermission = true;
            openUsageSettings();
            return;
        }

        startAfterPermission = false;
        startMonitorService();
    }

    private void requestPermissionsOnFirstLaunch() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("state", MODE_PRIVATE);

        if (prefs.getBoolean("first_permission_flow_done", false)) {
            return;
        }

        prefs.edit()
                .putBoolean("first_permission_flow_done", true)
                .apply();

        requestStartMonitor();
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < 33
                || checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;
    }

    private void startMonitorService() {
        Intent service = new Intent(this, GameMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(service);
        else startService(service);
        getSharedPreferences("state", MODE_PRIVATE).edit().putBoolean("monitoring", true).apply();
        Toast.makeText(this, "Đã bật nhận diện. Có thể mở game.", Toast.LENGTH_LONG).show();
        if ("home".equals(page)) render(); else updateStatus();
    }

    private void stopMonitorService() {
        stopService(new Intent(this, GameMonitorService.class));
        getSharedPreferences("state", MODE_PRIVATE).edit().putBoolean("monitoring", false).apply();
        Toast.makeText(this, "Đã dừng nhận diện.", Toast.LENGTH_SHORT).show();
        render();
    }

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void openUsageSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được cài đặt Usage Access trên thiết bị này.", Toast.LENGTH_LONG).show();
        }
    }

    private void launchGame(String packageName) {
        PackageManager pm = getPackageManager();
        Intent launch = pm.getLaunchIntentForPackage(packageName);
        if (launch == null) {
            Toast.makeText(this, "Chưa tìm thấy game này trên máy.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được game.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) 
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQ_POST_NOTIFICATIONS) {
            return;
        }

        boolean granted = hasNotificationPermission();

        if (!granted) {
            startAfterPermission = false;

            Toast.makeText(this,"Hãy cấp quyền thông báo để dùng tính năng nhận diện game.",Toast.LENGTH_LONG).show();

            updateStatus();
            return;
        }

        if (!hasUsageAccess()) {
            startAfterPermission = true;
            openUsageSettings();
        } else {
            startAfterPermission = false;
            startMonitorService();
        }

        updateStatus();
    }

    private TextView label(String text, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(44), 1f);
        p.setMargins(dp(2), 0, dp(2), 0);
        return p;
    }

    private GradientDrawable roundDrawable(int[] colors, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        d.setCornerRadius(radius);
        return d;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
