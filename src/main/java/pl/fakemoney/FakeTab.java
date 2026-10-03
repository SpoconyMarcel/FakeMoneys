package pl.fakemoney;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class FakeTab {
    private record Entry(Text label, Identifier tex, String key, int lvl) {}

    public static void render(DrawContext ctx, int sw) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;

        List<Entry> staff = new ArrayList<>(), rest = new ArrayList<>();
        for (FakePlayers.P p : FakePlayers.list) {
            if (p.staff > 0) {
                staff.add(new Entry(Text.empty().append(FakePlayers.staffTag(p)).append(Text.literal("§f" + p.name)),
                        DefaultSkinHelper.getTexture(p.uuid), p.name, p.staff));
            } else {
                rest.add(new Entry(Text.literal("§f" + p.name), DefaultSkinHelper.getTexture(p.uuid), p.name, 0));
            }
        }
        String me = FakeMoneyClient.spoofName != null ? FakeMoneyClient.spoofName
                : (mc.player != null ? mc.player.getName().getString() : "You");
        Identifier meTex = mc.player != null ? mc.player.getSkinTexture()
                : DefaultSkinHelper.getTexture(java.util.UUID.randomUUID());
        if (FakeMoneyClient.selfStaff > 0) {
            staff.add(new Entry(Text.empty().append(FakePlayers.rankText(FakeMoneyClient.selfStaff, 0))
                    .append(Text.literal("§f" + me)), meTex, me, FakeMoneyClient.selfStaff));
        } else {
            rest.add(new Entry(Text.literal("§f" + me), meTex, me, 0));
        }
        staff.sort((a, b) -> a.lvl() != b.lvl() ? Integer.compare(b.lvl(), a.lvl()) : a.key().compareToIgnoreCase(b.key()));
        rest.sort((a, b) -> a.key().compareToIgnoreCase(b.key()));
        List<Entry> es = new ArrayList<>(staff);
        es.addAll(rest);

        int maxLabel = 0;
        for (Entry e : es) maxLabel = Math.max(maxLabel, tr.getWidth(e.label()));
        int entryW = maxLabel + 9 + 9 + 3 + 4 + 15;
        int rows = 20;
        int cols = Math.max(1, Math.min(4, (sw - 20) / entryW));
        int n = Math.min(es.size(), cols * rows);
        cols = (n + rows - 1) / rows;
        int total = cols * entryW;
        int x0 = (sw - total) / 2;
        int y0 = 30;
        int usedRows = Math.min(rows, n);

        String h1 = "§b§lDonutSMP";
        String h2 = "§fOnline: §a" + FakeMoneyClient.fmt(FakeMoneyClient.onlineCount).toUpperCase();
        String foot = "§a$ " + FakeMoneyClient.fmt(FakeMoneyClient.money).toUpperCase()
                + " §7• §f" + FakeMoneyClient.fakePing + " ms";
        int hw = Math.max(Math.max(tr.getWidth(h1), tr.getWidth(h2)) + 12, total);
        int listEnd = y0 + usedRows * 9 + 2;
        ctx.fill((sw - hw) / 2, 3, (sw + hw) / 2, listEnd + 14, 0x80000000);
        ctx.drawTextWithShadow(tr, h1, (sw - tr.getWidth(h1)) / 2, 7, 0xFFFFFF);
        ctx.drawTextWithShadow(tr, h2, (sw - tr.getWidth(h2)) / 2, 18, 0xFFFFFF);
        ctx.drawTextWithShadow(tr, foot, (sw - tr.getWidth(foot)) / 2, listEnd + 3, 0xFFFFFF);

        RenderSystem.enableBlend();
        for (int i = 0; i < n; i++) {
            int col = i / rows, row = i % rows;
            int x = x0 + col * entryW + 1;
            int y = y0 + row * 9;
            Entry e = es.get(i);
            ctx.fill(x - 1, y, x + entryW - 2, y + 8, 0x20FFFFFF);
            ctx.drawTexture(e.tex(), x, y, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
            ctx.drawTexture(e.tex(), x, y, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
            // różowa ikonka (donut)
            ctx.fill(x + 10, y + 1, x + 17, y + 7, 0xFFFF69B4);
            ctx.fill(x + 12, y + 3, x + 15, y + 5, 0xFF3A1A2A);
            ctx.drawTextWithShadow(tr, e.label(), x + 20, y, 0xFFFFFF);
            int bx = x + entryW - 18;
            for (int b = 0; b < 5; b++) {
                int bh = 1 + b * 2;
                ctx.fill(bx + b * 3, y + 8 - bh, bx + b * 3 + 2, y + 8, 0xFF00D000);
            }
        }
    }
}
