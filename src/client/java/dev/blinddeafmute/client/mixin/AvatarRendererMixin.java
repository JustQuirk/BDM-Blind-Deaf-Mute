package dev.blinddeafmute.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.blinddeafmute.client.AvatarCoveringsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void bdm$submitCoverings(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo info) {
        AvatarCoveringsRenderer.submit(state, poseStack, collector);
    }
}
