package com.example.ui;

import com.example.Activity;
import com.example.Event;
import com.example.FixedActivity;
import com.example.Task;

public class ActivityIconUtil {
    public static String getIcon(Activity activity) {
        if (activity instanceof Event) return "\uD83D\uDCC5"; // 📅
        if (activity instanceof Task) return "\u2705"; // ✅
        if (activity instanceof FixedActivity) {
            String name = activity.getName().toLowerCase();
            if (name.contains("work")) return "\uD83D\uDCBC"; // 💼
            if (name.contains("sleep")) return "\uD83C\uDF19"; // 🌙
            return "\u2699"; // ⚙️
        }
        return "";
    }
}
