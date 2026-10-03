package pl.fakemoney;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Pula fejkowych graczy: ci sami płacą, piszą i są na tabie. */
public class FakePlayers {
    public static class P {
        public String name;
        public UUID uuid;
        public int tier;   // 0 brak, 1 Donut+, 2 Donut++, 3 Donut+++
        public int staff;  // 0 brak, 1 Admin, 2 Sr. Admin, 3 OWNER
    }

    public static final List<P> list = new ArrayList<>();
    static { regen(); }

    public static void regen() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        list.clear();
        Set<String> used = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            P p = new P();
            do { p.name = FakeMoneyClient.randomNick(); } while (!used.add(p.name.toLowerCase()));
            p.uuid = UUID.randomUUID();
            int x = r.nextInt(100);
            p.tier = x < 12 ? 3 : x < 30 ? 2 : x < 55 ? 1 : 0;
            p.staff = i == 0 ? 3 : i < 3 ? 2 : i < 6 ? 1 : 0;
            list.add(p);
        }
        list.sort((a, b) -> Integer.compare(b.staff, a.staff));
    }

    public static P find(String name) {
        for (P p : list) if (p.name.equalsIgnoreCase(name)) return p;
        P p = new P();
        p.name = name;
        p.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        return p;
    }

    public static P random() { return list.get(ThreadLocalRandom.current().nextInt(list.size())); }

    /** OWNER z niebieskim gradientem */
    public static MutableText ownerText() {
        String s = "OWNER";
        MutableText t = Text.empty();
        for (int i = 0; i < s.length(); i++) {
            float f = i / (s.length() - 1f);
            int r = (int) (0x1F + (0x4F - 0x1F) * f);
            int g = (int) (0x4B + (0xE3 - 0x4B) * f);
            int b = 0xFF;
            t.append(Text.literal(String.valueOf(s.charAt(i)))
                    .setStyle(Style.EMPTY.withBold(true).withColor(TextColor.fromRgb((r << 16) | (g << 8) | b))));
        }
        return t;
    }

    public static MutableText staffText(int staff) {
        switch (staff) {
            case 3: return ownerText();
            case 2: return Text.literal("Sr. Admin").formatted(Formatting.RED, Formatting.BOLD);
            case 1: return Text.literal("Admin").formatted(Formatting.RED);
            default: return Text.empty();
        }
    }

    public static MutableText tierText(int tier) {
        if (tier <= 0) return Text.empty();
        return Text.literal("Donut").formatted(Formatting.WHITE)
                .append(Text.literal("+".repeat(tier)).formatted(Formatting.AQUA));
    }

    /** ranga + spacja; staff ma pierwszeństwo przed Donut+ */
    public static MutableText rankText(int staff, int tier) {
        MutableText t = staff > 0 ? staffText(staff) : tierText(tier);
        if (staff > 0 || tier > 0) t.append(Text.literal(" "));
        return t;
    }

    public static MutableText chatTag(P p) { return rankText(p.staff, p.tier); }
    public static MutableText staffTag(P p) { return p.staff > 0 ? rankText(p.staff, 0) : Text.empty(); }
    public static MutableText selfRank() { return rankText(FakeMoneyClient.selfStaff, FakeMoneyClient.selfTier); }
}
