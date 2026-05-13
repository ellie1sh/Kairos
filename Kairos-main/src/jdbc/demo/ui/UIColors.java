package jdbc.demo.ui;

import java.awt.*;

public final class UIColors {

    // ── Brand palette ──────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(37,  99,  235);  // blue-600
    public static final Color PRIMARY_DARK   = new Color(29,  78,  216);  // blue-700
    public static final Color PRIMARY_LIGHT  = new Color(219, 234, 254);  // blue-100
    public static final Color SUCCESS        = new Color(22,  163,  74);  // green-600
    public static final Color SUCCESS_LIGHT  = new Color(220, 252, 231);  // green-100
    public static final Color WARNING        = new Color(217, 119,   6);  // amber-600
    public static final Color WARNING_LIGHT  = new Color(254, 243, 199);  // amber-100
    public static final Color DANGER         = new Color(220,  38,  38);  // red-600
    public static final Color DANGER_LIGHT   = new Color(254, 226, 226);  // red-100
    public static final Color ORANGE         = new Color(234,  88,  12);  // orange-600
    public static final Color ORANGE_LIGHT   = new Color(255, 237, 213);  // orange-100
    public static final Color PURPLE         = new Color(126,  34, 206);  // purple-700
    public static final Color PURPLE_LIGHT   = new Color(243, 232, 255);  // purple-100
    public static final Color CYAN           = new Color(  8, 145, 178);  // cyan-600
    public static final Color CYAN_LIGHT     = new Color(207, 250, 254);  // cyan-100

    // ── Sidebar ────────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG     = new Color(15,  23,  42);   // slate-900
    public static final Color SIDEBAR_HOVER  = new Color(30,  41,  59);   // slate-800
    public static final Color SIDEBAR_TEXT   = new Color(148, 163, 184);  // slate-400

    // ── Surface / Layout ───────────────────────────────────────────────────
    public static final Color BG             = new Color(248, 250, 252);  // slate-50
    public static final Color CARD_BG        = Color.WHITE;
    public static final Color BORDER         = new Color(226, 232, 240);  // slate-200
    public static final Color HEADER_BG      = Color.WHITE;

    // ── Text ───────────────────────────────────────────────────────────────
    public static final Color TEXT           = new Color( 15,  23,  42);  // slate-900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);  // slate-500
    public static final Color TEXT_MUTED     = new Color(148, 163, 184);  // slate-400

    // ── Table ──────────────────────────────────────────────────────────────
    public static final Color TABLE_HEADER   = new Color(248, 250, 252);
    public static final Color TABLE_ALT_ROW  = new Color(250, 251, 253);
    public static final Color TABLE_SELECT   = new Color(219, 234, 254);  // blue-100

    private UIColors() {}

    // ── Badge helpers ──────────────────────────────────────────────────────
    public static Color badgeBg(String s) {
        if (s == null) return BORDER;
        return switch (s.toLowerCase()) {
            case "available", "approved", "working", "returned" -> SUCCESS_LIGHT;
            case "borrowed", "pending"                          -> PRIMARY_LIGHT;
            case "unavailable", "rejected", "damaged"           -> DANGER_LIGHT;
            case "under maintenance", "returned with damage"    -> WARNING_LIGHT;
            case "equipment"                                    -> new Color(238, 242, 255);
            case "peripheral"                                   -> new Color(245, 243, 255);
            case "accessory"                                    -> new Color(240, 249, 255);
            case "tool"                                         -> new Color(240, 253, 250);
            case "admin"                                        -> PURPLE_LIGHT;
            case "custodian"                                    -> PRIMARY_LIGHT;
            case "professor"                                    -> CYAN_LIGHT;
            default                                             -> new Color(241, 245, 249);
        };
    }

    public static Color badgeFg(String s) {
        if (s == null) return TEXT_SECONDARY;
        return switch (s.toLowerCase()) {
            case "available", "approved", "working", "returned" -> SUCCESS;
            case "borrowed", "pending"                          -> PRIMARY;
            case "unavailable", "rejected", "damaged"           -> DANGER;
            case "under maintenance", "returned with damage"    -> WARNING;
            case "equipment"                                    -> new Color(79,  70, 229);
            case "peripheral"                                   -> new Color(109, 40, 217);
            case "accessory"                                    -> new Color(2,  132, 199);
            case "tool"                                         -> new Color(13, 148, 136);
            case "admin"                                        -> PURPLE;
            case "custodian"                                    -> PRIMARY;
            case "professor"                                    -> CYAN;
            default                                             -> TEXT_SECONDARY;
        };
    }

    public static Color roleColor(String type) {
        if (type == null) return TEXT_SECONDARY;
        return switch (type) {
            case "Admin"     -> PURPLE;
            case "Custodian" -> PRIMARY;
            case "Professor" -> CYAN;
            default          -> SUCCESS;
        };
    }
}
