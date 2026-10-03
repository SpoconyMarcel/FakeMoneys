package pl.fakemoney;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class FakeMoneyScreen extends Screen {
    private final int page;
    private TextFieldWidget moneyF, userF, teamF, minF, maxF, payF, chatF, spoofF, onlineF, killF;

    public FakeMoneyScreen(int page) { super(Text.literal("FakeMoney")); this.page = page; }

    private static long parseMoney(String s, long def) {
        try {
            s = s.trim().toLowerCase().replace(" ", "").replace(",", ".");
            double mul = 1;
            if (s.endsWith("k")) { mul = 1e3; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("m")) { mul = 1e6; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("b")) { mul = 1e9; s = s.substring(0, s.length() - 1); }
            return Math.round(Double.parseDouble(s) * mul);
        } catch (Exception e) { return def; }
    }

    private static int parseTicks(String s, int def) {
        try { return Math.max(1, (int) Math.round(Double.parseDouble(s.trim().replace(",", ".")) * 20)); }
        catch (Exception e) { return def; }
    }

    private TextFieldWidget field(int x, int y, String text) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, 200, 18, Text.empty());
        f.setMaxLength(40);
        f.setText(text);
        addDrawableChild(f);
        return f;
    }

    private Text on(String name, boolean v) { return Text.literal(name + ": " + (v ? "§aWŁ" : "§cWYŁ")); }

    private ButtonWidget btn(String label, int x, int y, ButtonWidget.PressAction a) {
        return addDrawableChild(ButtonWidget.builder(Text.literal(label), a).dimensions(x, y, 200, 20).build());
    }

    @Override
    protected void init() {
        if (page == 1) initPage1(); else if (page == 2) initPage2(); else if (page == 3) initPage3(); else initPage4();
    }

    private void initPage1() {
        int L = width / 2 - 205, R = width / 2 + 5;
        moneyF = field(L, 30, FakeMoneyClient.fmt(FakeMoneyClient.money));
        userF = field(L, 66, FakeMoneyClient.user);
        teamF = field(L, 102, FakeMoneyClient.team);
        minF = field(R, 30, FakeMoneyClient.fmt(FakeMoneyClient.minAmt));
        maxF = field(R, 66, FakeMoneyClient.fmt(FakeMoneyClient.maxAmt));
        payF = field(R, 102, String.valueOf(FakeMoneyClient.payTicks / 20.0));
        chatF = field(R, 138, String.valueOf(FakeMoneyClient.chatTicks / 20.0));

        btn("Zastosuj", L, 138, b -> apply());

        btn(on("Scoreboard", FakeMoneyClient.showBoard).getString(), L, 172, b -> {
            FakeMoneyClient.showBoard = !FakeMoneyClient.showBoard;
            b.setMessage(on("Scoreboard", FakeMoneyClient.showBoard));
        });
        btn(on("Spam płatności", FakeMoneyClient.autoPay).getString(), R, 172, b -> {
            apply();
            FakeMoneyClient.autoPay = !FakeMoneyClient.autoPay;
            b.setMessage(on("Spam płatności", FakeMoneyClient.autoPay));
        });
        btn(on("Spam czatu", FakeMoneyClient.autoChat).getString(), L, 196, b -> {
            apply();
            FakeMoneyClient.autoChat = !FakeMoneyClient.autoChat;
            b.setMessage(on("Spam czatu", FakeMoneyClient.autoChat));
        });
        btn("Wyślij 1 płatność", R, 196, b -> {
            apply();
            FakeMoneyClient.pay(client, FakePlayers.random(), FakeMoneyClient.randomAmount());
            moneyF.setText(FakeMoneyClient.fmt(FakeMoneyClient.money));
        });
        btn("Strona 2 →", width / 2 - 100, 222, b -> { apply(); client.setScreen(new FakeMoneyScreen(2)); });
    }

    private void initPage2() {
        int C = width / 2 - 100;
        spoofF = field(C, 30, FakeMoneyClient.spoofName == null ? "" : FakeMoneyClient.spoofName);
        btn("Zmień na ten nick i skin", C, 54, b -> {
            String n = spoofF.getText().trim();
            if (!n.isEmpty()) FakeMoneyClient.spoof(n);
        });
        btn("Wróć do swojego nicku i skina", C, 78, b -> { FakeMoneyClient.resetSpoof(); spoofF.setText(""); });

        onlineF = field(C, 118, FakeMoneyClient.fmt(FakeMoneyClient.onlineCount));
        btn(on("Fake TAB", FakeMoneyClient.fakeTab).getString(), C, 146, b -> {
            FakeMoneyClient.fakeTab = !FakeMoneyClient.fakeTab;
            b.setMessage(on("Fake TAB", FakeMoneyClient.fakeTab));
        });
        btn("Wylosuj nowych fake graczy", C, 170, b -> FakePlayers.regen());
        btn("← Strona 1", C, 196, b -> { apply(); client.setScreen(new FakeMoneyScreen(1)); });
        btn("Strona 3 →", C, 220, b -> { apply(); client.setScreen(new FakeMoneyScreen(3)); });
    }

    private static final String[] TIERS = {"brak", "Donut+", "Donut++", "Donut+++"};
    private static final String[] STAFF = {"brak", "Admin", "Sr. Admin", "OWNER"};

    private void initPage3() {
        int C = width / 2 - 100;
        btn("Ranga Donut: " + TIERS[FakeMoneyClient.selfTier], C, 40, b -> {
            FakeMoneyClient.selfTier = (FakeMoneyClient.selfTier + 1) % 4;
            b.setMessage(Text.literal("Ranga Donut: " + TIERS[FakeMoneyClient.selfTier]));
        });
        btn("Ranga staff: " + STAFF[FakeMoneyClient.selfStaff], C, 70, b -> {
            FakeMoneyClient.selfStaff = (FakeMoneyClient.selfStaff + 1) % 4;
            b.setMessage(Text.literal("Ranga staff: " + STAFF[FakeMoneyClient.selfStaff]));
        });
        btn("Język wiadomości: " + (FakeMoneyClient.polish ? "Polski" : "English"), C, 100, b -> {
            FakeMoneyClient.polish = !FakeMoneyClient.polish;
            b.setMessage(Text.literal("Język wiadomości: " + (FakeMoneyClient.polish ? "Polski" : "English")));
        });
        btn("← Strona 2", C, 130, b -> client.setScreen(new FakeMoneyScreen(2)));
        btn("Strona 4 →", C, 154, b -> client.setScreen(new FakeMoneyScreen(4)));
    }

    private void initPage4() {
        int C = width / 2 - 100;
        killF = field(C, 30, String.valueOf(FakeMoneyClient.killTicks / 20.0));
        btn(on("Kill feed", FakeMoneyClient.killFeed).getString(), C, 54, b -> {
            apply();
            FakeMoneyClient.killFeed = !FakeMoneyClient.killFeed;
            b.setMessage(on("Kill feed", FakeMoneyClient.killFeed));
        });
        btn(on("Losowe odstępy", FakeMoneyClient.jitter).getString(), C, 78, b -> {
            FakeMoneyClient.jitter = !FakeMoneyClient.jitter;
            b.setMessage(on("Losowe odstępy", FakeMoneyClient.jitter));
        });
        btn(on("Dźwięk przy płatności", FakeMoneyClient.sound).getString(), C, 102, b -> {
            FakeMoneyClient.sound = !FakeMoneyClient.sound;
            b.setMessage(on("Dźwięk przy płatności", FakeMoneyClient.sound));
        });
        btn(on("Action bar przy płatności", FakeMoneyClient.actionBar).getString(), C, 126, b -> {
            FakeMoneyClient.actionBar = !FakeMoneyClient.actionBar;
            b.setMessage(on("Action bar przy płatności", FakeMoneyClient.actionBar));
        });
        btn(on("Fake /pay (blokuje prawdziwe)", FakeMoneyClient.interceptPay).getString(), C, 150, b -> {
            FakeMoneyClient.interceptPay = !FakeMoneyClient.interceptPay;
            b.setMessage(on("Fake /pay (blokuje prawdziwe)", FakeMoneyClient.interceptPay));
        });
        btn(on("Rozszerzony scoreboard", FakeMoneyClient.extraBoard).getString(), C, 174, b -> {
            FakeMoneyClient.extraBoard = !FakeMoneyClient.extraBoard;
            b.setMessage(on("Rozszerzony scoreboard", FakeMoneyClient.extraBoard));
        });
        btn("← Strona 3", C, 204, b -> { apply(); client.setScreen(new FakeMoneyScreen(3)); });
    }

    private void apply() {
        if (page == 1) {
            FakeMoneyClient.money = parseMoney(moneyF.getText(), FakeMoneyClient.money);
            FakeMoneyClient.user = userF.getText().isBlank() ? "Steve" : userF.getText();
            FakeMoneyClient.team = teamF.getText();
            FakeMoneyClient.minAmt = parseMoney(minF.getText(), 50_000);
            FakeMoneyClient.maxAmt = parseMoney(maxF.getText(), 2_000_000_000L);
            FakeMoneyClient.payTicks = parseTicks(payF.getText(), 20);
            FakeMoneyClient.chatTicks = parseTicks(chatF.getText(), 20);
        } else if (page == 4) {
            if (killF != null) FakeMoneyClient.killTicks = parseTicks(killF.getText(), 60);
        } else if (onlineF != null) {
            FakeMoneyClient.onlineCount = parseMoney(onlineF.getText(), FakeMoneyClient.onlineCount);
        }
    }

    @Override
    public void removed() { apply(); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx);
        int g = 0xAAAAAA;
        ctx.drawCenteredTextWithShadow(textRenderer, "FakeMoney (strona " + page + "/4)", width / 2, 6, 0xFFFFFF);
        if (page == 1) {
            int L = width / 2 - 205, R = width / 2 + 5;
            ctx.drawTextWithShadow(textRenderer, "Money (np. 100k, 5m, 2b)", L, 20, g);
            ctx.drawTextWithShadow(textRenderer, "User", L, 56, g);
            ctx.drawTextWithShadow(textRenderer, "Team", L, 92, g);
            ctx.drawTextWithShadow(textRenderer, "Min płatność", R, 20, g);
            ctx.drawTextWithShadow(textRenderer, "Max płatność", R, 56, g);
            ctx.drawTextWithShadow(textRenderer, "Płatność co ile sekund", R, 92, g);
            ctx.drawTextWithShadow(textRenderer, "Wiadomość czatu co ile sekund", R, 128, g);
        } else if (page == 3) {
            ctx.drawCenteredTextWithShadow(textRenderer, "Klikaj, żeby zmieniać rangę. Staff ma pierwszeństwo.", width / 2, 24, g);
            ctx.drawCenteredTextWithShadow(textRenderer, "Widać ją na scoreboardzie, tabie (tylko staff) i nad głową.", width / 2, 182, g);
        } else if (page == 4) {
            int C = width / 2 - 100;
            ctx.drawTextWithShadow(textRenderer, "Kill feed co ile sekund", C, 20, g);
        } else {
            int C = width / 2 - 100;
            ctx.drawTextWithShadow(textRenderer, "Nick, w którego chcesz się zamienić", C, 20, g);
            ctx.drawTextWithShadow(textRenderer, "Online w tabie (np. 34.6k)", C, 108, g);
        }
        super.render(ctx, mx, my, delta);
    }
}
