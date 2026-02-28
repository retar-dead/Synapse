package myau.mixin;

import myau.Myau;
import myau.module.modules.Sprint;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
@Mixin(value = {AbstractClientPlayer.class}, priority = 9999)
public abstract class MixinAbstractClientPlayer extends MixinEntityPlayer {
    @Redirect(
            method = {"getFovModifier"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"
            )
    )
    private double getFovModifier(IAttributeInstance iAttributeInstance) {
        double attributeValue = iAttributeInstance.getAttributeValue();
        if ((((Entity) (Object) this)) instanceof EntityPlayerSP && Myau.moduleManager != null) {
            Sprint sprint = (Sprint) Myau.moduleManager.modules.get(Sprint.class);
            return sprint.isEnabled() && sprint.shouldApplyFovFix(iAttributeInstance) ? attributeValue * 1.300000011920929 : attributeValue;
        } else {
            return attributeValue;
        }
    }

    // Inject to return custom skin for local player
    @Inject(method = "getLocationSkin", at = @At("HEAD"), cancellable = true)
    private void onGetLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        try {
            if (Myau.customSkin != null && ((Entity) (Object) this) instanceof EntityPlayerSP) {
                cir.setReturnValue(Myau.customSkin);
            }
        } catch (Throwable ignored) {
        }
    }

    // Inject to return correct model type (slim/default) when custom skin is applied
    @Inject(method = "getSkinType", at = @At("HEAD"), cancellable = true)
    private void onGetSkinType(CallbackInfoReturnable<String> cir) {
        try {
            if (Myau.customSkin != null && ((Entity) (Object) this) instanceof EntityPlayerSP) {
                if (Myau.customSkinSlim) {
                    cir.setReturnValue("slim");
                } else {
                    cir.setReturnValue("default");
                }
            }
        } catch (Throwable ignored) {
        }
    }
}