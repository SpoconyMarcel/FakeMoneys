package pl.fakemoney;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FakeMoneyClient implements ClientModInitializer {
    public static KeyBinding openKey;

    // ---- ustawienia ----
    public static long money = 100_000;
    public static String server = "Donut SMP";
    public static String user = "Zł0ty";
    public static String team = "Zlotciaki";
    public static boolean showBoard = true;
    public static boolean extraBoard = false;
    public static boolean autoPay = false;
    public static boolean autoChat = false;
    public static boolean killFeed = false;
    public static boolean fakeTab = true;
    public static boolean jitter = true;
    public static boolean sound = false;
    public static boolean actionBar = true;
    public static boolean interceptPay = false;
    public static boolean polish = false;   // język fejkowych wiadomości
    public static long onlineCount = 34_600;
    public static int selfTier = 0, selfStaff = 0;
    public static long minAmt = 50_000, maxAmt = 2_000_000_000L;
    public static int payTicks = 20, chatTicks = 20, killTicks = 60;
    public static int shards = 0, kills = 0, deaths = 0;
    public static int fakePing = 61;

    // ---- przebranie (nick + skin) ----
    public static String spoofName = null;
    public static Identifier spoofSkin = null;
    public static String spoofModel = "default";

    // ---- wewnętrzne ----
    private static int payLeft = 20, chatLeft = 20, killLeft = 60;
    private static int now = 0;
    private static long playTicks = 0;

    private static class Task {
        final int at; final Runnable r;
        Task(int at, Runnable r) { this.at = at; this.r = r; }
    }
    private static final List<Task> tasks = new ArrayList<>();

    public static void later(int ticks, Runnable r) { tasks.add(new Task(now + Math.max(1, ticks), r)); }
    private static int rnd(int lo, int hi) { return ThreadLocalRandom.current().nextInt(lo, hi + 1); }
    private static <T> T pick(T[] a) { return a[ThreadLocalRandom.current().nextInt(a.length)]; }
    private static int delay(int base) {
        if (!jitter) return base;
        return Math.max(2, (int) (base * ThreadLocalRandom.current().nextDouble(0.4, 1.8)));
    }

    private static final String[] WORDS = {"Dark","Pro","Ender","Creeper","Noob","Shadow","Fire","Ice","Wolf","Zombie",
            "Kacper","Maciek","Bartek","Kuba","Szymon","Oskar","Franek","Wojtek","Mati","Dominik",
            "Skull","Ninja","Killer","Mega","Cobra","Tiger","Blaze","Nether","Diamond","Gold",
            "Pixel","Turbo","Sniper","Lord","King","Boss","Rafal","Tomek","Jasiek","Hubert"};
    private static final String[] MSGS_PL = {"siema","ktos chce trade?","lol","gg","xd","daj kase","kto ma elytre","ez",
            "jak sie teleportowac","hahaha","ale lagi","wbijaj na pvp","ktos pomoze?","/pay mi ktos","co jest xd",
            "ile masz kasy?","elo","kto na spawnie","omg","serio?","nie wierze","zrobcie giveaway","wbijam na baze",
            "ktos kupi shulkery?","kto idzie na end?","dostalem 5m","afk farm dziala?","sorry lag","ktos ma wolny team?","niezle"};
    private static final String[] MSGS_EN = {"anyone selling spawners?","how do i get money","lag","who wants to trade",
            "nice base","gg ez","bro what","free money?","where is spawn","need help","tp me pls","lmao","wtf",
            "anyone online?","new here","hi","yo","gg","xd","lol","ez","who has elytra","can someone help me","nice",
            "any teams recruiting?","pay me pls","omg","no way","this server is crazy","any giveaways?"};
    private static final String[] REACT_PL = {"ile dostales?","wow","skad masz tyle kasy","omg","ez money","daj mi tez","xdd",
            "ktos dal ci kase??","damn","ale masz fart","bro","niezle","gg"};
    private static final String[] REACT_EN = {"how much?","wow","where did u get that","omg","ez money","give me some too",
            "xdd","someone paid u??","damn","lucky","gg","bro","nice"};
    private static final String[] THANKS_PL = {"thx","dzieki","ty","thanks!","omg dzieki","dzieki wielkie","tysm"};
    private static final String[] THANKS_EN = {"thx","ty","thanks!","omg thanks","thank you","tysm","appreciate it","thanks bro"};
    private static final String[] ITEMS_PL = {"spawnery","beacony","netherite","elytre","totemy","diamenty","shulkery",
            "klucze","stack gappli","zloto","trident","mace"};
    private static final String[] ITEMS_EN = {"spawners","beacons","netherite","elytra","totems","diamonds","shulkers",
            "keys","stack of gapples","gold","trident","mace"};
    private static final String[] PRICES = {"50k","100k","250k","500k","1m","2m","5m","10m","50m","100m","1b","2b"};

    public static String randomNick() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String a = WORDS[r.nextInt(WORDS.length)], b = WORDS[r.nextInt(WORDS.length)];
        switch (r.nextInt(7)) {
            case 0: return a + b;
            case 1: return a + r.nextInt(1, 100);
            case 2: return "xX" + a + "Xx";
            case 3: return a + "_" + b;
            case 4: return a.toLowerCase() + r.nextInt(100, 10000);
            case 5: return b + a + r.nextInt(1, 10);
            default: return a.toLowerCase() + b.toLowerCase();
        }
    }

    private static String[] react() { return polish ? REACT_PL : REACT_EN; }
    private static String[] thanks() { return polish ? THANKS_PL : THANKS_EN; }

    public static String randomMessage() {
        if (rnd(1, 100) <= 55) return pick(polish ? MSGS_PL : MSGS_EN);
        String item = pick(polish ? ITEMS_PL : ITEMS_EN), price = pick(PRICES);
        if (polish) {
            switch (rnd(0, 4)) {
                case 0: return "sellam " + item + " za " + price;
                case 1: return "kupie " + item + " " + price;
                case 2: return "WTB " + item + " paying " + price;
                case 3: return "WTS " + item + " " + price + " dm me";
                default: return "ktos ma " + item + "?";
            }
        }
        switch (rnd(0, 4)) {
            case 0: return "selling " + item + " for " + price;
            case 1: return "buying " + item + " " + price;
            case 2: return "WTB " + item + " paying " + price;
            case 3: return "WTS " + item + " " + price + " dm me";
            default: return "anyone got " + item + "?";
        }
    }

    public static long randomAmount() {
        long lo = Math.max(1, Math.min(minAmt, maxAmt)), hi = Math.max(minAmt, maxAmt);
        double v = Math.exp(ThreadLocalRandom.current().nextDouble(Math.log(lo), Math.log(hi + 1)));
        return Math.min(hi, Math.max(lo, (long) v));
    }

    public static long parseMoney(String s, long def) {
        try {
            s = s.trim().toLowerCase().replace(" ", "").replace(",", ".");
            double mul = 1;
            if (s.endsWith("k")) { mul = 1e3; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("m")) { mul = 1e6; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("b")) { mul = 1e9; s = s.substring(0, s.length() - 1); }
            return Math.round(Double.parseDouble(s) * mul);
        } catch (Exception e) { return def; }
    }

    public static String fmt(long v) {
        if (v >= 1_000_000_000L) return trim(v / 1e9) + "B";
        if (v >= 1_000_000L) return trim(v / 1e6) + "M";
        if (v >= 1_000L) return trim(v / 1e3) + "k";
        return String.valueOf(v);
    }
    private static String trim(double d) {
        String s = String.format(java.util.Locale.US, "%.1f", d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    private static String playtime() {
        long sec = playTicks / 20;
        return (sec / 3600) + "h " + ((sec % 3600) / 60) + "m";
    }

    // ---- akcje ----
    private static void chat(MinecraftClient c, Text t) { c.inGameHud.getChatHud().addMessage(t); }

    /** ktoś płaci Tobie */
    public static void pay(MinecraftClient c, FakePlayers.P p, long amt) {
        money += amt;
        chat(c, Text.empty().append(FakePlayers.chatTag(p))
                .append(Text.literal("§a" + p.name + (polish ? " §fprzelał Ci §a$" : " §fpaid you §a$") + fmt(amt))));
        if (sound) c.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2F));
        if (actionBar) c.inGameHud.setOverlayMessage(Text.literal("§a+$" + fmt(amt) + (polish ? " §7od §f" : " §7from §f") + p.name), false);
        if (rnd(1, 100) <= 25) later(rnd(15, 70), () -> fakeChat(c, FakePlayers.random(), pick(react())));
    }

    /** Ty płacisz komuś (fejkowe /pay) */
    public static void paySent(MinecraftClient c, String nick, long amt) {
        if (amt <= 0) { chat(c, Text.literal(polish ? "§cNieprawidłowa kwota." : "§cInvalid amount.")); return; }
        if (amt > money) { chat(c, Text.literal(polish ? "§cNie masz wystarczająco pieniędzy." : "§cYou don't have enough money.")); return; }
        money -= amt;
        chat(c, Text.literal((polish ? "§aZapłaciłeś §f" : "§aYou paid §f") + nick + " §a$" + fmt(amt)));
        FakePlayers.P p = FakePlayers.find(nick);
        later(rnd(20, 100), () -> fakeChat(c, p, pick(thanks())));
    }

    public static void fakeChat(MinecraftClient c, FakePlayers.P p, String msg) {
        chat(c, Text.empty().append(FakePlayers.chatTag(p)).append(Text.literal("§7" + p.name + " §8» §f" + msg)));
    }

    public static void killMsg(MinecraftClient c, String victim, String killer) {
        String t;
        int r = rnd(1, 100);
        if (polish) {
            if (r <= 12) t = victim + " spadł z wysokiego miejsca";
            else if (r <= 70) t = victim + " został zabity przez " + killer;
            else if (r <= 90) t = victim + " został zastrzelony przez " + killer;
            else t = victim + " został wysadzony przez " + killer;
        } else {
            if (r <= 12) t = victim + " fell from a high place";
            else if (r <= 55) t = victim + " was slain by " + killer;
            else if (r <= 75) t = victim + " was shot by " + killer;
            else if (r <= 90) t = victim + " was killed by " + killer;
            else t = victim + " was blown up by " + killer;
        }
        chat(c, Text.literal("§f" + t));
    }

    public static void note(String s) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) chat(mc, Text.literal("§8[FakeMoney] §7" + s));
    }

    /** zmienia Twój nick i skin (tylko u Ciebie) na wybrany nick */
    public static void spoof(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GameProfile p = new GameProfile(null, name);
        note("Pobieram skin " + name + "...");
        SkullBlockEntity.loadProperties(p, filled -> mc.execute(() -> {
            try {
                mc.getSkinProvider().loadSkin(filled, (type, id, tex) -> {
                    if (type != MinecraftProfileTexture.Type.SKIN) return;
                    spoofSkin = id;
                    spoofModel = "slim".equals(tex.getMetadata("model")) ? "slim" : "default";
                    spoofName = filled.getName() != null ? filled.getName() : name;
                    note("Jesteś teraz " + spoofName);
                }, false);
            } catch (Exception e) {
                note("Błąd pobierania skina: " + e.getMessage());
            }
        }));
    }

    public static void resetSpoof() { spoofName = null; spoofSkin = null; spoofModel = "default"; }

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakemoney.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.fakemoney"));

        // ---- komendy: /fm ... ----
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("fm")
                        .then(literal("pay")
                                .then(argument("nick", StringArgumentType.word())
                                        .then(argument("kwota", StringArgumentType.word()).executes(ctx -> {
                                            paySent(MinecraftClient.getInstance(),
                                                    StringArgumentType.getString(ctx, "nick"),
                                                    parseMoney(StringArgumentType.getString(ctx, "kwota"), -1));
                                            return 1;
                                        }))))
                        .then(literal("money")
                                .then(argument("kwota", StringArgumentType.word()).executes(ctx -> {
                                    money = parseMoney(StringArgumentType.getString(ctx, "kwota"), money);
                                    note("Money: " + fmt(money));
                                    return 1;
                                })))
                        .then(literal("msg")
                                .then(argument("nick", StringArgumentType.word())
                                        .then(argument("tekst", StringArgumentType.greedyString()).executes(ctx -> {
                                            fakeChat(MinecraftClient.getInstance(),
                                                    FakePlayers.find(StringArgumentType.getString(ctx, "nick")),
                                                    StringArgumentType.getString(ctx, "tekst"));
                                            return 1;
                                        }))))
                        .then(literal("kill")
                                .then(argument("nick", StringArgumentType.word()).executes(ctx -> {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    String me = spoofName != null ? spoofName : mc.player.getName().getString();
                                    killMsg(mc, StringArgumentType.getString(ctx, "nick"), me);
                                    kills++;
                                    return 1;
                                })))
                        .then(literal("lang")
                                .then(literal("pl").executes(ctx -> { polish = true; note("Język: polski"); return 1; }))
                                .then(literal("en").executes(ctx -> { polish = false; note("Language: English"); return 1; })))
                        .then(literal("set")
                                .then(literal("kills").then(argument("n", IntegerArgumentType.integer(0)).executes(ctx -> {
                                    kills = IntegerArgumentType.getInteger(ctx, "n"); return 1; })))
                                .then(literal("deaths").then(argument("n", IntegerArgumentType.integer(0)).executes(ctx -> {
                                    deaths = IntegerArgumentType.getInteger(ctx, "n"); return 1; })))
                                .then(literal("shards").then(argument("n", IntegerArgumentType.integer(0)).executes(ctx -> {
                                    shards = IntegerArgumentType.getInteger(ctx, "n"); return 1; }))))
        ));

        // ---- przechwytywanie prawdziwego /pay (opcjonalne) ----
        ClientSendMessageEvents.ALLOW_COMMAND.register(cmd -> {
            if (!interceptPay) return true;
            String[] a = cmd.trim().split("\\s+");
            if (a.length == 3 && a[0].equalsIgnoreCase("pay")) {
                paySent(MinecraftClient.getInstance(), a[1], parseMoney(a[2], -1));
                return false;
            }
            return true;
        });

        // ---- tick ----
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            while (openKey.wasPressed()) c.setScreen(new FakeMoneyScreen(1));
            if (c.player == null) return;
            now++;
            playTicks++;
            if (now % 100 == 0) fakePing = rnd(42, 78);

            if (!tasks.isEmpty()) {
                List<Task> copy = new ArrayList<>(tasks);
                for (Task t : copy) if (t.at <= now) { tasks.remove(t); t.r.run(); }
            }

            if (autoPay && --payLeft <= 0) {
                payLeft = delay(payTicks);
                pay(c, FakePlayers.random(), randomAmount());
                if (rnd(1, 100) <= 15) {
                    int extra = rnd(1, 3);
                    for (int i = 0; i < extra; i++) later(rnd(3, 20), () -> pay(c, FakePlayers.random(), randomAmount()));
                }
            }
            if (autoChat && --chatLeft <= 0) {
                chatLeft = delay(chatTicks);
                fakeChat(c, FakePlayers.random(), randomMessage());
                if (rnd(1, 100) <= 12) {
                    int extra = rnd(1, 3);
                    for (int i = 0; i < extra; i++)
                        later(rnd(5, 25), () -> fakeChat(c, FakePlayers.random(), randomMessage()));
                }
            }
            if (killFeed && --killLeft <= 0) {
                killLeft = delay(killTicks);
                FakePlayers.P v = FakePlayers.random(), k = FakePlayers.random();
                killMsg(c, v.name, k.name);
            }
        });

        // ---- HUD: scoreboard (+ tab w singleplayerze) ----
        HudRenderCallback.EVENT.register((ctx, delta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (fakeTab && mc.player != null && mc.world != null && !mc.options.hudHidden
                    && mc.options.playerListKey.isPressed() && mc.isInSingleplayer()
                    && mc.player.networkHandler.getListedPlayerListEntries().size() <= 1
                    && mc.world.getScoreboard().getObjectiveForSlot(0) == null) {
                FakeTab.render(ctx, ctx.getScaledWindowWidth());
            }
            if (!showBoard || mc.options.hudHidden || mc.player == null) return;

            String shown = spoofName != null ? spoofName : user;
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal("§6§l" + server));
            lines.add(Text.literal((polish ? "§fGracz: " : "§fUser: ")).append(FakePlayers.selfRank()).append(Text.literal("§e" + shown)));
            lines.add(Text.literal((polish ? "§fDrużyna: §b" : "§fTeam: §b") + team));
            lines.add(Text.literal((polish ? "§fPieniądze: §a" : "§fMoney: §a") + fmt(money)));
            if (extraBoard) {
                lines.add(Text.literal((polish ? "§fOdłamki: §d" : "§fShards: §d") + shards));
                lines.add(Text.literal((polish ? "§fZabójstwa: §c" : "§fKills: §c") + kills));
                lines.add(Text.literal((polish ? "§fŚmierci: §7" : "§fDeaths: §7") + deaths));
                lines.add(Text.literal((polish ? "§fCzas gry: §f" : "§fPlaytime: §f") + playtime()));
            }
            int w = 0;
            for (Text l : lines) w = Math.max(w, mc.textRenderer.getWidth(l));
            w += 8;
            int x2 = ctx.getScaledWindowWidth() - 4, x1 = x2 - w;
            int h = lines.size() * 10 + 6;
            int y1 = ctx.getScaledWindowHeight() / 2 - h / 2;
            ctx.fill(x1, y1, x2, y1 + h, 0x88000000);
            for (int i = 0; i < lines.size(); i++) {
                int tx = i == 0 ? x1 + (w - mc.textRenderer.getWidth(lines.get(0))) / 2 : x1 + 4;
                ctx.drawTextWithShadow(mc.textRenderer, lines.get(i), tx, y1 + 4 + i * 10, 0xFFFFFF);
            }
        });
    }
}
