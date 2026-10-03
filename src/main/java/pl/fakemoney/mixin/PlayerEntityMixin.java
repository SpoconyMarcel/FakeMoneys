package pl.fakemoney.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.fakemoney.FakeMoneyClient;
import pl.fakemoney.FakePlayers;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void fm$name(CallbackInfoReturnable<Text> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) return;
        if (FakeMoneyClient.spoofName == null && FakeMoneyClient.selfStaff == 0 && FakeMoneyClient.selfTier == 0) return;
        String n = FakeMoneyClient.spoofName != null ? FakeMoneyClient.spoofName
                : ((PlayerEntity) (Object) this).getName().getString();
        cir.setReturnValue(Text.empty().append(FakePlayers.selfRank()).append(Text.literal(n)));
    }
}
