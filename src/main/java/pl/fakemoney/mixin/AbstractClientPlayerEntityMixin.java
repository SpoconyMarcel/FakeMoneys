package pl.fakemoney.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.fakemoney.FakeMoneyClient;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void fm$skin(CallbackInfoReturnable<Identifier> cir) {
        if (FakeMoneyClient.spoofSkin != null && (Object) this == MinecraftClient.getInstance().player)
            cir.setReturnValue(FakeMoneyClient.spoofSkin);
    }

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void fm$model(CallbackInfoReturnable<String> cir) {
        if (FakeMoneyClient.spoofSkin != null && (Object) this == MinecraftClient.getInstance().player)
            cir.setReturnValue(FakeMoneyClient.spoofModel);
    }
}
