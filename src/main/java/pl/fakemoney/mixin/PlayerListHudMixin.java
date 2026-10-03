package pl.fakemoney.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.fakemoney.FakeMoneyClient;
import pl.fakemoney.FakeTab;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fm$render(DrawContext ctx, int width, Scoreboard sb, ScoreboardObjective obj, CallbackInfo ci) {
        if (FakeMoneyClient.fakeTab) {
            FakeTab.render(ctx, width);
            ci.cancel();
        }
    }
}
