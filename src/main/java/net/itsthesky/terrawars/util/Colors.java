package net.itsthesky.terrawars.util;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility class about colors.
 */
public class Colors {

    // Private constructor to prevent instantiation
    private Colors() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Shade Indexes
    public static final int SHADE_50 = 0;
    public static final int SHADE_100 = 1;
    public static final int SHADE_200 = 2;
    public static final int SHADE_300 = 3;
    public static final int SHADE_400 = 4;
    public static final int SHADE_500 = 5;
    public static final int SHADE_600 = 6;
    public static final int SHADE_700 = 7;
    public static final int SHADE_800 = 8;
    public static final int SHADE_900 = 9;
    public static final int SHADE_950 = 10;

    // Slate
    public static final @NotNull TextColor SLATE_50 = Objects.requireNonNull(TextColor.fromHexString("#f8fafc"));
    public static final @NotNull TextColor SLATE_100 = Objects.requireNonNull(TextColor.fromHexString("#f1f5f9"));
    public static final @NotNull TextColor SLATE_200 = Objects.requireNonNull(TextColor.fromHexString("#e2e8f0"));
    public static final @NotNull TextColor SLATE_300 = Objects.requireNonNull(TextColor.fromHexString("#cbd5e1"));
    public static final @NotNull TextColor SLATE_400 = Objects.requireNonNull(TextColor.fromHexString("#94a3b8"));
    public static final @NotNull TextColor SLATE_500 = Objects.requireNonNull(TextColor.fromHexString("#64748b"));
    public static final @NotNull TextColor SLATE_600 = Objects.requireNonNull(TextColor.fromHexString("#475569"));
    public static final @NotNull TextColor SLATE_700 = Objects.requireNonNull(TextColor.fromHexString("#334155"));
    public static final @NotNull TextColor SLATE_800 = Objects.requireNonNull(TextColor.fromHexString("#1e293b"));
    public static final @NotNull TextColor SLATE_900 = Objects.requireNonNull(TextColor.fromHexString("#0f172a"));
    public static final @NotNull TextColor SLATE_950 = Objects.requireNonNull(TextColor.fromHexString("#020617"));

    // Gray
    public static final @NotNull TextColor GRAY_50 = Objects.requireNonNull(TextColor.fromHexString("#f9fafb"));
    public static final @NotNull TextColor GRAY_100 = Objects.requireNonNull(TextColor.fromHexString("#f3f4f6"));
    public static final @NotNull TextColor GRAY_200 = Objects.requireNonNull(TextColor.fromHexString("#e5e7eb"));
    public static final @NotNull TextColor GRAY_300 = Objects.requireNonNull(TextColor.fromHexString("#d1d5db"));
    public static final @NotNull TextColor GRAY_400 = Objects.requireNonNull(TextColor.fromHexString("#9ca3af"));
    public static final @NotNull TextColor GRAY_500 = Objects.requireNonNull(TextColor.fromHexString("#6b7280"));
    public static final @NotNull TextColor GRAY_600 = Objects.requireNonNull(TextColor.fromHexString("#4b5563"));
    public static final @NotNull TextColor GRAY_700 = Objects.requireNonNull(TextColor.fromHexString("#374151"));
    public static final @NotNull TextColor GRAY_800 = Objects.requireNonNull(TextColor.fromHexString("#1f2937"));
    public static final @NotNull TextColor GRAY_900 = Objects.requireNonNull(TextColor.fromHexString("#111827"));
    public static final @NotNull TextColor GRAY_950 = Objects.requireNonNull(TextColor.fromHexString("#030712"));

    // Zinc
    public static final @NotNull TextColor ZINC_50 = Objects.requireNonNull(TextColor.fromHexString("#fafafa"));
    public static final @NotNull TextColor ZINC_100 = Objects.requireNonNull(TextColor.fromHexString("#f4f4f5"));
    public static final @NotNull TextColor ZINC_200 = Objects.requireNonNull(TextColor.fromHexString("#e4e4e7"));
    public static final @NotNull TextColor ZINC_300 = Objects.requireNonNull(TextColor.fromHexString("#d4d4d8"));
    public static final @NotNull TextColor ZINC_400 = Objects.requireNonNull(TextColor.fromHexString("#a1a1aa"));
    public static final @NotNull TextColor ZINC_500 = Objects.requireNonNull(TextColor.fromHexString("#71717a"));
    public static final @NotNull TextColor ZINC_600 = Objects.requireNonNull(TextColor.fromHexString("#52525b"));
    public static final @NotNull TextColor ZINC_700 = Objects.requireNonNull(TextColor.fromHexString("#3f3f46"));
    public static final @NotNull TextColor ZINC_800 = Objects.requireNonNull(TextColor.fromHexString("#27272a"));
    public static final @NotNull TextColor ZINC_900 = Objects.requireNonNull(TextColor.fromHexString("#18181b"));
    public static final @NotNull TextColor ZINC_950 = Objects.requireNonNull(TextColor.fromHexString("#09090b"));

    // Neutral
    public static final @NotNull TextColor NEUTRAL_50 = Objects.requireNonNull(TextColor.fromHexString("#fafafa"));
    public static final @NotNull TextColor NEUTRAL_100 = Objects.requireNonNull(TextColor.fromHexString("#f5f5f5"));
    public static final @NotNull TextColor NEUTRAL_200 = Objects.requireNonNull(TextColor.fromHexString("#e5e5e5"));
    public static final @NotNull TextColor NEUTRAL_300 = Objects.requireNonNull(TextColor.fromHexString("#d4d4d4"));
    public static final @NotNull TextColor NEUTRAL_400 = Objects.requireNonNull(TextColor.fromHexString("#a3a3a3"));
    public static final @NotNull TextColor NEUTRAL_500 = Objects.requireNonNull(TextColor.fromHexString("#737373"));
    public static final @NotNull TextColor NEUTRAL_600 = Objects.requireNonNull(TextColor.fromHexString("#525252"));
    public static final @NotNull TextColor NEUTRAL_700 = Objects.requireNonNull(TextColor.fromHexString("#404040"));
    public static final @NotNull TextColor NEUTRAL_800 = Objects.requireNonNull(TextColor.fromHexString("#262626"));
    public static final @NotNull TextColor NEUTRAL_900 = Objects.requireNonNull(TextColor.fromHexString("#171717"));
    public static final @NotNull TextColor NEUTRAL_950 = Objects.requireNonNull(TextColor.fromHexString("#0a0a0a"));

    // Stone
    public static final @NotNull TextColor STONE_50 = Objects.requireNonNull(TextColor.fromHexString("#fafaf9"));
    public static final @NotNull TextColor STONE_100 = Objects.requireNonNull(TextColor.fromHexString("#f5f5f4"));
    public static final @NotNull TextColor STONE_200 = Objects.requireNonNull(TextColor.fromHexString("#e7e5e4"));
    public static final @NotNull TextColor STONE_300 = Objects.requireNonNull(TextColor.fromHexString("#d6d3d1"));
    public static final @NotNull TextColor STONE_400 = Objects.requireNonNull(TextColor.fromHexString("#a8a29e"));
    public static final @NotNull TextColor STONE_500 = Objects.requireNonNull(TextColor.fromHexString("#78716c"));
    public static final @NotNull TextColor STONE_600 = Objects.requireNonNull(TextColor.fromHexString("#57534e"));
    public static final @NotNull TextColor STONE_700 = Objects.requireNonNull(TextColor.fromHexString("#44403c"));
    public static final @NotNull TextColor STONE_800 = Objects.requireNonNull(TextColor.fromHexString("#292524"));
    public static final @NotNull TextColor STONE_900 = Objects.requireNonNull(TextColor.fromHexString("#1c1917"));
    public static final @NotNull TextColor STONE_950 = Objects.requireNonNull(TextColor.fromHexString("#0c0a09"));

    // Red
    public static final @NotNull TextColor RED_50 = Objects.requireNonNull(TextColor.fromHexString("#fef2f2"));
    public static final @NotNull TextColor RED_100 = Objects.requireNonNull(TextColor.fromHexString("#fee2e2"));
    public static final @NotNull TextColor RED_200 = Objects.requireNonNull(TextColor.fromHexString("#fecaca"));
    public static final @NotNull TextColor RED_300 = Objects.requireNonNull(TextColor.fromHexString("#fca5a5"));
    public static final @NotNull TextColor RED_400 = Objects.requireNonNull(TextColor.fromHexString("#f87171"));
    public static final @NotNull TextColor RED_500 = Objects.requireNonNull(TextColor.fromHexString("#ef4444"));
    public static final @NotNull TextColor RED_600 = Objects.requireNonNull(TextColor.fromHexString("#dc2626"));
    public static final @NotNull TextColor RED_700 = Objects.requireNonNull(TextColor.fromHexString("#b91c1c"));
    public static final @NotNull TextColor RED_800 = Objects.requireNonNull(TextColor.fromHexString("#991b1b"));
    public static final @NotNull TextColor RED_900 = Objects.requireNonNull(TextColor.fromHexString("#7f1d1d"));
    public static final @NotNull TextColor RED_950 = Objects.requireNonNull(TextColor.fromHexString("#450a0a"));

    // Orange
    public static final @NotNull TextColor ORANGE_50 = Objects.requireNonNull(TextColor.fromHexString("#fff7ed"));
    public static final @NotNull TextColor ORANGE_100 = Objects.requireNonNull(TextColor.fromHexString("#ffedd5"));
    public static final @NotNull TextColor ORANGE_200 = Objects.requireNonNull(TextColor.fromHexString("#fed7aa"));
    public static final @NotNull TextColor ORANGE_300 = Objects.requireNonNull(TextColor.fromHexString("#fdba74"));
    public static final @NotNull TextColor ORANGE_400 = Objects.requireNonNull(TextColor.fromHexString("#fb923c"));
    public static final @NotNull TextColor ORANGE_500 = Objects.requireNonNull(TextColor.fromHexString("#f97316"));
    public static final @NotNull TextColor ORANGE_600 = Objects.requireNonNull(TextColor.fromHexString("#ea580c"));
    public static final @NotNull TextColor ORANGE_700 = Objects.requireNonNull(TextColor.fromHexString("#c2410c"));
    public static final @NotNull TextColor ORANGE_800 = Objects.requireNonNull(TextColor.fromHexString("#9a3412"));
    public static final @NotNull TextColor ORANGE_900 = Objects.requireNonNull(TextColor.fromHexString("#7c2d12"));
    public static final @NotNull TextColor ORANGE_950 = Objects.requireNonNull(TextColor.fromHexString("#431407"));

    // Amber
    public static final @NotNull TextColor AMBER_50 = Objects.requireNonNull(TextColor.fromHexString("#fffbeb"));
    public static final @NotNull TextColor AMBER_100 = Objects.requireNonNull(TextColor.fromHexString("#fef3c7"));
    public static final @NotNull TextColor AMBER_200 = Objects.requireNonNull(TextColor.fromHexString("#fde68a"));
    public static final @NotNull TextColor AMBER_300 = Objects.requireNonNull(TextColor.fromHexString("#fcd34d"));
    public static final @NotNull TextColor AMBER_400 = Objects.requireNonNull(TextColor.fromHexString("#fbbf24"));
    public static final @NotNull TextColor AMBER_500 = Objects.requireNonNull(TextColor.fromHexString("#f59e0b"));
    public static final @NotNull TextColor AMBER_600 = Objects.requireNonNull(TextColor.fromHexString("#d97706"));
    public static final @NotNull TextColor AMBER_700 = Objects.requireNonNull(TextColor.fromHexString("#b45309"));
    public static final @NotNull TextColor AMBER_800 = Objects.requireNonNull(TextColor.fromHexString("#92400e"));
    public static final @NotNull TextColor AMBER_900 = Objects.requireNonNull(TextColor.fromHexString("#78350f"));
    public static final @NotNull TextColor AMBER_950 = Objects.requireNonNull(TextColor.fromHexString("#451a03"));

    // Yellow
    public static final @NotNull TextColor YELLOW_50 = Objects.requireNonNull(TextColor.fromHexString("#fefce8"));
    public static final @NotNull TextColor YELLOW_100 = Objects.requireNonNull(TextColor.fromHexString("#fef9c3"));
    public static final @NotNull TextColor YELLOW_200 = Objects.requireNonNull(TextColor.fromHexString("#fef08a"));
    public static final @NotNull TextColor YELLOW_300 = Objects.requireNonNull(TextColor.fromHexString("#fde047"));
    public static final @NotNull TextColor YELLOW_400 = Objects.requireNonNull(TextColor.fromHexString("#facc15"));
    public static final @NotNull TextColor YELLOW_500 = Objects.requireNonNull(TextColor.fromHexString("#eab308"));
    public static final @NotNull TextColor YELLOW_600 = Objects.requireNonNull(TextColor.fromHexString("#ca8a04"));
    public static final @NotNull TextColor YELLOW_700 = Objects.requireNonNull(TextColor.fromHexString("#a16207"));
    public static final @NotNull TextColor YELLOW_800 = Objects.requireNonNull(TextColor.fromHexString("#854d0e"));
    public static final @NotNull TextColor YELLOW_900 = Objects.requireNonNull(TextColor.fromHexString("#713f12"));
    public static final @NotNull TextColor YELLOW_950 = Objects.requireNonNull(TextColor.fromHexString("#422006"));

    // Lime
    public static final @NotNull TextColor LIME_50 = Objects.requireNonNull(TextColor.fromHexString("#f7fee7"));
    public static final @NotNull TextColor LIME_100 = Objects.requireNonNull(TextColor.fromHexString("#ecfccb"));
    public static final @NotNull TextColor LIME_200 = Objects.requireNonNull(TextColor.fromHexString("#d9f99d"));
    public static final @NotNull TextColor LIME_300 = Objects.requireNonNull(TextColor.fromHexString("#bef264"));
    public static final @NotNull TextColor LIME_400 = Objects.requireNonNull(TextColor.fromHexString("#a3e635"));
    public static final @NotNull TextColor LIME_500 = Objects.requireNonNull(TextColor.fromHexString("#84cc16"));
    public static final @NotNull TextColor LIME_600 = Objects.requireNonNull(TextColor.fromHexString("#65a30d"));
    public static final @NotNull TextColor LIME_700 = Objects.requireNonNull(TextColor.fromHexString("#4d7c0f"));
    public static final @NotNull TextColor LIME_800 = Objects.requireNonNull(TextColor.fromHexString("#3f6212"));
    public static final @NotNull TextColor LIME_900 = Objects.requireNonNull(TextColor.fromHexString("#365314"));
    public static final @NotNull TextColor LIME_950 = Objects.requireNonNull(TextColor.fromHexString("#1a2e05"));

    // Green
    public static final @NotNull TextColor GREEN_50 = Objects.requireNonNull(TextColor.fromHexString("#f0fdf4"));
    public static final @NotNull TextColor GREEN_100 = Objects.requireNonNull(TextColor.fromHexString("#dcfce7"));
    public static final @NotNull TextColor GREEN_200 = Objects.requireNonNull(TextColor.fromHexString("#bbf7d0"));
    public static final @NotNull TextColor GREEN_300 = Objects.requireNonNull(TextColor.fromHexString("#86efac"));
    public static final @NotNull TextColor GREEN_400 = Objects.requireNonNull(TextColor.fromHexString("#4ade80"));
    public static final @NotNull TextColor GREEN_500 = Objects.requireNonNull(TextColor.fromHexString("#22c55e"));
    public static final @NotNull TextColor GREEN_600 = Objects.requireNonNull(TextColor.fromHexString("#16a34a"));
    public static final @NotNull TextColor GREEN_700 = Objects.requireNonNull(TextColor.fromHexString("#15803d"));
    public static final @NotNull TextColor GREEN_800 = Objects.requireNonNull(TextColor.fromHexString("#166534"));
    public static final @NotNull TextColor GREEN_900 = Objects.requireNonNull(TextColor.fromHexString("#14532d"));
    public static final @NotNull TextColor GREEN_950 = Objects.requireNonNull(TextColor.fromHexString("#052e16"));

    // Emerald
    public static final @NotNull TextColor EMERALD_50 = Objects.requireNonNull(TextColor.fromHexString("#ecfdf5"));
    public static final @NotNull TextColor EMERALD_100 = Objects.requireNonNull(TextColor.fromHexString("#d1fae5"));
    public static final @NotNull TextColor EMERALD_200 = Objects.requireNonNull(TextColor.fromHexString("#a7f3d0"));
    public static final @NotNull TextColor EMERALD_300 = Objects.requireNonNull(TextColor.fromHexString("#6ee7b7"));
    public static final @NotNull TextColor EMERALD_400 = Objects.requireNonNull(TextColor.fromHexString("#34d399"));
    public static final @NotNull TextColor EMERALD_500 = Objects.requireNonNull(TextColor.fromHexString("#10b981"));
    public static final @NotNull TextColor EMERALD_600 = Objects.requireNonNull(TextColor.fromHexString("#059669"));
    public static final @NotNull TextColor EMERALD_700 = Objects.requireNonNull(TextColor.fromHexString("#047857"));
    public static final @NotNull TextColor EMERALD_800 = Objects.requireNonNull(TextColor.fromHexString("#065f46"));
    public static final @NotNull TextColor EMERALD_900 = Objects.requireNonNull(TextColor.fromHexString("#064e3b"));
    public static final @NotNull TextColor EMERALD_950 = Objects.requireNonNull(TextColor.fromHexString("#022c22"));

    // Teal
    public static final @NotNull TextColor TEAL_50 = Objects.requireNonNull(TextColor.fromHexString("#f0fdfa"));
    public static final @NotNull TextColor TEAL_100 = Objects.requireNonNull(TextColor.fromHexString("#ccfbf1"));
    public static final @NotNull TextColor TEAL_200 = Objects.requireNonNull(TextColor.fromHexString("#99f6e4"));
    public static final @NotNull TextColor TEAL_300 = Objects.requireNonNull(TextColor.fromHexString("#5eead4"));
    public static final @NotNull TextColor TEAL_400 = Objects.requireNonNull(TextColor.fromHexString("#2dd4bf"));
    public static final @NotNull TextColor TEAL_500 = Objects.requireNonNull(TextColor.fromHexString("#14b8a6"));
    public static final @NotNull TextColor TEAL_600 = Objects.requireNonNull(TextColor.fromHexString("#0d9488"));
    public static final @NotNull TextColor TEAL_700 = Objects.requireNonNull(TextColor.fromHexString("#0f766e"));
    public static final @NotNull TextColor TEAL_800 = Objects.requireNonNull(TextColor.fromHexString("#115e59"));
    public static final @NotNull TextColor TEAL_900 = Objects.requireNonNull(TextColor.fromHexString("#134e4a"));
    public static final @NotNull TextColor TEAL_950 = Objects.requireNonNull(TextColor.fromHexString("#042f2e"));

    // Cyan
    public static final @NotNull TextColor CYAN_50 = Objects.requireNonNull(TextColor.fromHexString("#ecfeff"));
    public static final @NotNull TextColor CYAN_100 = Objects.requireNonNull(TextColor.fromHexString("#cffafe"));
    public static final @NotNull TextColor CYAN_200 = Objects.requireNonNull(TextColor.fromHexString("#a5f3fc"));
    public static final @NotNull TextColor CYAN_300 = Objects.requireNonNull(TextColor.fromHexString("#67e8f9"));
    public static final @NotNull TextColor CYAN_400 = Objects.requireNonNull(TextColor.fromHexString("#22d3ee"));
    public static final @NotNull TextColor CYAN_500 = Objects.requireNonNull(TextColor.fromHexString("#06b6d4"));
    public static final @NotNull TextColor CYAN_600 = Objects.requireNonNull(TextColor.fromHexString("#0891b2"));
    public static final @NotNull TextColor CYAN_700 = Objects.requireNonNull(TextColor.fromHexString("#0e7490"));
    public static final @NotNull TextColor CYAN_800 = Objects.requireNonNull(TextColor.fromHexString("#155e75"));
    public static final @NotNull TextColor CYAN_900 = Objects.requireNonNull(TextColor.fromHexString("#164e63"));
    public static final @NotNull TextColor CYAN_950 = Objects.requireNonNull(TextColor.fromHexString("#083344"));

    // Sky
    public static final @NotNull TextColor SKY_50 = Objects.requireNonNull(TextColor.fromHexString("#f0f9ff"));
    public static final @NotNull TextColor SKY_100 = Objects.requireNonNull(TextColor.fromHexString("#e0f2fe"));
    public static final @NotNull TextColor SKY_200 = Objects.requireNonNull(TextColor.fromHexString("#bae6fd"));
    public static final @NotNull TextColor SKY_300 = Objects.requireNonNull(TextColor.fromHexString("#7dd3fc"));
    public static final @NotNull TextColor SKY_400 = Objects.requireNonNull(TextColor.fromHexString("#38bdf8"));
    public static final @NotNull TextColor SKY_500 = Objects.requireNonNull(TextColor.fromHexString("#0ea5e9"));
    public static final @NotNull TextColor SKY_600 = Objects.requireNonNull(TextColor.fromHexString("#0284c7"));
    public static final @NotNull TextColor SKY_700 = Objects.requireNonNull(TextColor.fromHexString("#0369a1"));
    public static final @NotNull TextColor SKY_800 = Objects.requireNonNull(TextColor.fromHexString("#075985"));
    public static final @NotNull TextColor SKY_900 = Objects.requireNonNull(TextColor.fromHexString("#0c4a6e"));
    public static final @NotNull TextColor SKY_950 = Objects.requireNonNull(TextColor.fromHexString("#082f49"));

    // Blue
    public static final @NotNull TextColor BLUE_50 = Objects.requireNonNull(TextColor.fromHexString("#eff6ff"));
    public static final @NotNull TextColor BLUE_100 = Objects.requireNonNull(TextColor.fromHexString("#dbeafe"));
    public static final @NotNull TextColor BLUE_200 = Objects.requireNonNull(TextColor.fromHexString("#bfdbfe"));
    public static final @NotNull TextColor BLUE_300 = Objects.requireNonNull(TextColor.fromHexString("#93c5fd"));
    public static final @NotNull TextColor BLUE_400 = Objects.requireNonNull(TextColor.fromHexString("#60a5fa"));
    public static final @NotNull TextColor BLUE_500 = Objects.requireNonNull(TextColor.fromHexString("#3b82f6"));
    public static final @NotNull TextColor BLUE_600 = Objects.requireNonNull(TextColor.fromHexString("#2563eb"));
    public static final @NotNull TextColor BLUE_700 = Objects.requireNonNull(TextColor.fromHexString("#1d4ed8"));
    public static final @NotNull TextColor BLUE_800 = Objects.requireNonNull(TextColor.fromHexString("#1e40af"));
    public static final @NotNull TextColor BLUE_900 = Objects.requireNonNull(TextColor.fromHexString("#1e3a8a"));
    public static final @NotNull TextColor BLUE_950 = Objects.requireNonNull(TextColor.fromHexString("#172554"));

    // Indigo
    public static final @NotNull TextColor INDIGO_50 = Objects.requireNonNull(TextColor.fromHexString("#eef2ff"));
    public static final @NotNull TextColor INDIGO_100 = Objects.requireNonNull(TextColor.fromHexString("#e0e7ff"));
    public static final @NotNull TextColor INDIGO_200 = Objects.requireNonNull(TextColor.fromHexString("#c7d2fe"));
    public static final @NotNull TextColor INDIGO_300 = Objects.requireNonNull(TextColor.fromHexString("#a5b4fc"));
    public static final @NotNull TextColor INDIGO_400 = Objects.requireNonNull(TextColor.fromHexString("#818cf8"));
    public static final @NotNull TextColor INDIGO_500 = Objects.requireNonNull(TextColor.fromHexString("#6366f1"));
    public static final @NotNull TextColor INDIGO_600 = Objects.requireNonNull(TextColor.fromHexString("#4f46e5"));
    public static final @NotNull TextColor INDIGO_700 = Objects.requireNonNull(TextColor.fromHexString("#4338ca"));
    public static final @NotNull TextColor INDIGO_800 = Objects.requireNonNull(TextColor.fromHexString("#3730a3"));
    public static final @NotNull TextColor INDIGO_900 = Objects.requireNonNull(TextColor.fromHexString("#312e81"));
    public static final @NotNull TextColor INDIGO_950 = Objects.requireNonNull(TextColor.fromHexString("#1e1b4b"));

    // Violet
    public static final @NotNull TextColor VIOLET_50 = Objects.requireNonNull(TextColor.fromHexString("#f5f3ff"));
    public static final @NotNull TextColor VIOLET_100 = Objects.requireNonNull(TextColor.fromHexString("#ede9fe"));
    public static final @NotNull TextColor VIOLET_200 = Objects.requireNonNull(TextColor.fromHexString("#ddd6fe"));
    public static final @NotNull TextColor VIOLET_300 = Objects.requireNonNull(TextColor.fromHexString("#c4b5fd"));
    public static final @NotNull TextColor VIOLET_400 = Objects.requireNonNull(TextColor.fromHexString("#a78bfa"));
    public static final @NotNull TextColor VIOLET_500 = Objects.requireNonNull(TextColor.fromHexString("#8b5cf6"));
    public static final @NotNull TextColor VIOLET_600 = Objects.requireNonNull(TextColor.fromHexString("#7c3aed"));
    public static final @NotNull TextColor VIOLET_700 = Objects.requireNonNull(TextColor.fromHexString("#6d28d9"));
    public static final @NotNull TextColor VIOLET_800 = Objects.requireNonNull(TextColor.fromHexString("#5b21b6"));
    public static final @NotNull TextColor VIOLET_900 = Objects.requireNonNull(TextColor.fromHexString("#4c1d95"));
    public static final @NotNull TextColor VIOLET_950 = Objects.requireNonNull(TextColor.fromHexString("#2e1065"));

    // Purple
    public static final @NotNull TextColor PURPLE_50 = Objects.requireNonNull(TextColor.fromHexString("#faf5ff"));
    public static final @NotNull TextColor PURPLE_100 = Objects.requireNonNull(TextColor.fromHexString("#f3e8ff"));
    public static final @NotNull TextColor PURPLE_200 = Objects.requireNonNull(TextColor.fromHexString("#e9d5ff"));
    public static final @NotNull TextColor PURPLE_300 = Objects.requireNonNull(TextColor.fromHexString("#d8b4fe"));
    public static final @NotNull TextColor PURPLE_400 = Objects.requireNonNull(TextColor.fromHexString("#c084fc"));
    public static final @NotNull TextColor PURPLE_500 = Objects.requireNonNull(TextColor.fromHexString("#a855f7"));
    public static final @NotNull TextColor PURPLE_600 = Objects.requireNonNull(TextColor.fromHexString("#9333ea"));
    public static final @NotNull TextColor PURPLE_700 = Objects.requireNonNull(TextColor.fromHexString("#7e22ce"));
    public static final @NotNull TextColor PURPLE_800 = Objects.requireNonNull(TextColor.fromHexString("#6b21a8"));
    public static final @NotNull TextColor PURPLE_900 = Objects.requireNonNull(TextColor.fromHexString("#581c87"));
    public static final @NotNull TextColor PURPLE_950 = Objects.requireNonNull(TextColor.fromHexString("#3b0764"));

    // Fuchsia
    public static final @NotNull TextColor FUCHSIA_50 = Objects.requireNonNull(TextColor.fromHexString("#fdf4ff"));
    public static final @NotNull TextColor FUCHSIA_100 = Objects.requireNonNull(TextColor.fromHexString("#fae8ff"));
    public static final @NotNull TextColor FUCHSIA_200 = Objects.requireNonNull(TextColor.fromHexString("#f5d0fe"));
    public static final @NotNull TextColor FUCHSIA_300 = Objects.requireNonNull(TextColor.fromHexString("#f0abfc"));
    public static final @NotNull TextColor FUCHSIA_400 = Objects.requireNonNull(TextColor.fromHexString("#e879f9"));
    public static final @NotNull TextColor FUCHSIA_500 = Objects.requireNonNull(TextColor.fromHexString("#d946ef"));
    public static final @NotNull TextColor FUCHSIA_600 = Objects.requireNonNull(TextColor.fromHexString("#c026d3"));
    public static final @NotNull TextColor FUCHSIA_700 = Objects.requireNonNull(TextColor.fromHexString("#a21caf"));
    public static final @NotNull TextColor FUCHSIA_800 = Objects.requireNonNull(TextColor.fromHexString("#86198f"));
    public static final @NotNull TextColor FUCHSIA_900 = Objects.requireNonNull(TextColor.fromHexString("#701a75"));
    public static final @NotNull TextColor FUCHSIA_950 = Objects.requireNonNull(TextColor.fromHexString("#4a044e"));

    // Pink
    public static final @NotNull TextColor PINK_50 = Objects.requireNonNull(TextColor.fromHexString("#fdf2f8"));
    public static final @NotNull TextColor PINK_100 = Objects.requireNonNull(TextColor.fromHexString("#fce7f3"));
    public static final @NotNull TextColor PINK_200 = Objects.requireNonNull(TextColor.fromHexString("#fbcfe8"));
    public static final @NotNull TextColor PINK_300 = Objects.requireNonNull(TextColor.fromHexString("#f9a8d4"));
    public static final @NotNull TextColor PINK_400 = Objects.requireNonNull(TextColor.fromHexString("#f472b6"));
    public static final @NotNull TextColor PINK_500 = Objects.requireNonNull(TextColor.fromHexString("#ec4899"));
    public static final @NotNull TextColor PINK_600 = Objects.requireNonNull(TextColor.fromHexString("#db2777"));
    public static final @NotNull TextColor PINK_700 = Objects.requireNonNull(TextColor.fromHexString("#be185d"));
    public static final @NotNull TextColor PINK_800 = Objects.requireNonNull(TextColor.fromHexString("#9d174d"));
    public static final @NotNull TextColor PINK_900 = Objects.requireNonNull(TextColor.fromHexString("#831843"));
    public static final @NotNull TextColor PINK_950 = Objects.requireNonNull(TextColor.fromHexString("#500724"));

    // Rose
    public static final @NotNull TextColor ROSE_50 = Objects.requireNonNull(TextColor.fromHexString("#fff1f2"));
    public static final @NotNull TextColor ROSE_100 = Objects.requireNonNull(TextColor.fromHexString("#ffe4e6"));
    public static final @NotNull TextColor ROSE_200 = Objects.requireNonNull(TextColor.fromHexString("#fecdd3"));
    public static final @NotNull TextColor ROSE_300 = Objects.requireNonNull(TextColor.fromHexString("#fda4af"));
    public static final @NotNull TextColor ROSE_400 = Objects.requireNonNull(TextColor.fromHexString("#fb7185"));
    public static final @NotNull TextColor ROSE_500 = Objects.requireNonNull(TextColor.fromHexString("#f43f5e"));
    public static final @NotNull TextColor ROSE_600 = Objects.requireNonNull(TextColor.fromHexString("#e11d48"));
    public static final @NotNull TextColor ROSE_700 = Objects.requireNonNull(TextColor.fromHexString("#be123c"));
    public static final @NotNull TextColor ROSE_800 = Objects.requireNonNull(TextColor.fromHexString("#9f1239"));
    public static final @NotNull TextColor ROSE_900 = Objects.requireNonNull(TextColor.fromHexString("#881337"));
    public static final @NotNull TextColor ROSE_950 = Objects.requireNonNull(TextColor.fromHexString("#4c0519"));

    // Color Lists - Immutable lists of all shades for each color
    public static final List<TextColor> SLATE = List.of(SLATE_50, SLATE_100, SLATE_200, SLATE_300, SLATE_400, SLATE_500, SLATE_600, SLATE_700, SLATE_800, SLATE_900, SLATE_950);

    public static final List<TextColor> GRAY = List.of(GRAY_50, GRAY_100, GRAY_200, GRAY_300, GRAY_400, GRAY_500, GRAY_600, GRAY_700, GRAY_800, GRAY_900, GRAY_950);

    public static final List<TextColor> ZINC = List.of(ZINC_50, ZINC_100, ZINC_200, ZINC_300, ZINC_400, ZINC_500, ZINC_600, ZINC_700, ZINC_800, ZINC_900, ZINC_950);

    public static final List<TextColor> NEUTRAL = List.of(NEUTRAL_50, NEUTRAL_100, NEUTRAL_200, NEUTRAL_300, NEUTRAL_400, NEUTRAL_500, NEUTRAL_600, NEUTRAL_700, NEUTRAL_800, NEUTRAL_900, NEUTRAL_950);

    public static final List<TextColor> STONE = List.of(STONE_50, STONE_100, STONE_200, STONE_300, STONE_400, STONE_500, STONE_600, STONE_700, STONE_800, STONE_900, STONE_950);

    public static final List<TextColor> RED = List.of(RED_50, RED_100, RED_200, RED_300, RED_400, RED_500, RED_600, RED_700, RED_800, RED_900, RED_950);

    public static final List<TextColor> ORANGE = List.of(ORANGE_50, ORANGE_100, ORANGE_200, ORANGE_300, ORANGE_400, ORANGE_500, ORANGE_600, ORANGE_700, ORANGE_800, ORANGE_900, ORANGE_950);

    public static final List<TextColor> AMBER = List.of(AMBER_50, AMBER_100, AMBER_200, AMBER_300, AMBER_400, AMBER_500, AMBER_600, AMBER_700, AMBER_800, AMBER_900, AMBER_950);

    public static final List<TextColor> YELLOW = List.of(YELLOW_50, YELLOW_100, YELLOW_200, YELLOW_300, YELLOW_400, YELLOW_500, YELLOW_600, YELLOW_700, YELLOW_800, YELLOW_900, YELLOW_950);

    public static final List<TextColor> LIME = List.of(LIME_50, LIME_100, LIME_200, LIME_300, LIME_400, LIME_500, LIME_600, LIME_700, LIME_800, LIME_900, LIME_950);

    public static final List<TextColor> GREEN = List.of(GREEN_50, GREEN_100, GREEN_200, GREEN_300, GREEN_400, GREEN_500, GREEN_600, GREEN_700, GREEN_800, GREEN_900, GREEN_950);

    public static final List<TextColor> EMERALD = List.of(EMERALD_50, EMERALD_100, EMERALD_200, EMERALD_300, EMERALD_400, EMERALD_500, EMERALD_600, EMERALD_700, EMERALD_800, EMERALD_900, EMERALD_950);

    public static final List<TextColor> TEAL = List.of(TEAL_50, TEAL_100, TEAL_200, TEAL_300, TEAL_400, TEAL_500, TEAL_600, TEAL_700, TEAL_800, TEAL_900, TEAL_950);

    public static final List<TextColor> CYAN = List.of(CYAN_50, CYAN_100, CYAN_200, CYAN_300, CYAN_400, CYAN_500, CYAN_600, CYAN_700, CYAN_800, CYAN_900, CYAN_950);

    public static final List<TextColor> SKY = List.of(SKY_50, SKY_100, SKY_200, SKY_300, SKY_400, SKY_500, SKY_600, SKY_700, SKY_800, SKY_900, SKY_950);

    public static final List<TextColor> BLUE = List.of(BLUE_50, BLUE_100, BLUE_200, BLUE_300, BLUE_400, BLUE_500, BLUE_600, BLUE_700, BLUE_800, BLUE_900, BLUE_950);

    public static final List<TextColor> INDIGO = List.of(INDIGO_50, INDIGO_100, INDIGO_200, INDIGO_300, INDIGO_400, INDIGO_500, INDIGO_600, INDIGO_700, INDIGO_800, INDIGO_900, INDIGO_950);

    public static final List<TextColor> VIOLET = List.of(VIOLET_50, VIOLET_100, VIOLET_200, VIOLET_300, VIOLET_400, VIOLET_500, VIOLET_600, VIOLET_700, VIOLET_800, VIOLET_900, VIOLET_950);

    public static final List<TextColor> PURPLE = List.of(PURPLE_50, PURPLE_100, PURPLE_200, PURPLE_300, PURPLE_400, PURPLE_500, PURPLE_600, PURPLE_700, PURPLE_800, PURPLE_900, PURPLE_950);

    public static final List<TextColor> FUCHSIA = List.of(FUCHSIA_50, FUCHSIA_100, FUCHSIA_200, FUCHSIA_300, FUCHSIA_400, FUCHSIA_500, FUCHSIA_600, FUCHSIA_700, FUCHSIA_800, FUCHSIA_900, FUCHSIA_950);

    public static final List<TextColor> PINK = List.of(PINK_50, PINK_100, PINK_200, PINK_300, PINK_400, PINK_500, PINK_600, PINK_700, PINK_800, PINK_900, PINK_950);

    public static final List<TextColor> ROSE = List.of(ROSE_50, ROSE_100, ROSE_200, ROSE_300, ROSE_400, ROSE_500, ROSE_600, ROSE_700, ROSE_800, ROSE_900, ROSE_950);

    public static List<TextColor> getSchemeByName(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "slate" -> SLATE;
            case "gray" -> GRAY;
            case "zinc" -> ZINC;
            case "neutral" -> NEUTRAL;
            case "stone" -> STONE;
            case "red" -> RED;
            case "orange" -> ORANGE;
            case "amber" -> AMBER;
            case "yellow" -> YELLOW;
            case "lime" -> LIME;
            case "green" -> GREEN;
            case "emerald" -> EMERALD;
            case "teal" -> TEAL;
            case "cyan" -> CYAN;
            case "sky" -> SKY;
            case "blue" -> BLUE;
            case "indigo" -> INDIGO;
            case "violet" -> VIOLET;
            case "purple" -> PURPLE;
            case "fuchsia" -> FUCHSIA;
            case "pink" -> PINK;
            case "rose" -> ROSE;
            default -> throw new IllegalArgumentException("Unknown color name: " + colorName);
        };
    }

    public static List<String> getAvailableSchemesByName() {
        return List.of(
                "slate", "gray", "zinc", "neutral", "stone",
                "red", "orange", "amber", "yellow", "lime",
                "green", "emerald", "teal", "cyan", "sky",
                "blue", "indigo", "violet", "purple", "fuchsia",
                "pink", "rose"
        );
    }
}
