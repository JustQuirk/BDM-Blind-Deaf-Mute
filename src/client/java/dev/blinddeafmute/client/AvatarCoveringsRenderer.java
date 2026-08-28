package dev.blinddeafmute.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class AvatarCoveringsRenderer {
    private AvatarCoveringsRenderer() {
    }

    public static void submit(EntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!(renderState instanceof AvatarRenderState) || !BlindDeafMuteClient.isStarted()) {
            return;
        }
        Player player = findPlayer(renderState);
        if (player == null) {
            return;
        }
        BlindDeafMuteClient.RoleFlags roles = BlindDeafMuteClient.rolesFor(player.getUUID());
        if (roles == null || !roles.hasAny()) {
            return;
        }
        OrderedSubmitNodeCollector ordered = collector.order(0);
        if (roles.blind()) {
            submitCuboid(ordered, poseStack, -0.32f, 1.49f, -0.45f, 0.32f, 1.68f, -0.36f, 0xFF1F5CC7);
        }
        if (roles.deaf()) {
            submitCuboid(ordered, poseStack, -0.62f, 1.30f, -0.18f, -0.42f, 1.84f, 0.24f, 0xFF6B6B6B);
            submitCuboid(ordered, poseStack, 0.42f, 1.30f, -0.18f, 0.62f, 1.84f, 0.24f, 0xFF6B6B6B);
            submitCuboid(ordered, poseStack, -0.50f, 1.70f, -0.16f, 0.50f, 1.84f, 0.18f, 0xFF6B6B6B);
        }
        if (roles.mute()) {
            submitCuboid(ordered, poseStack, -0.25f, 1.19f, -0.45f, 0.25f, 1.33f, -0.36f, 0xFFB82E36);
        }
    }

    private static Player findPlayer(EntityRenderState renderState) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        Player closest = null;
        double closestDistance = 0.18;
        for (Player player : Minecraft.getInstance().level.players()) {
            double distance = player.distanceToSqr(renderState.x, renderState.y, renderState.z);
            if (distance < closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private static void submitCuboid(OrderedSubmitNodeCollector collector, PoseStack poseStack, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, vertices) -> drawCuboid(pose, vertices, minX, minY, minZ, maxX, maxY, maxZ, color));
    }

    private static void drawCuboid(PoseStack.Pose pose, VertexConsumer vertices, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
        face(pose, vertices, color, 0, 0, -1, minX, minY, minZ, maxX, maxY, minZ);
        face(pose, vertices, color, 0, 0, 1, maxX, minY, maxZ, minX, maxY, maxZ);
        face(pose, vertices, color, -1, 0, 0, minX, minY, maxZ, minX, maxY, minZ);
        face(pose, vertices, color, 1, 0, 0, maxX, minY, minZ, maxX, maxY, maxZ);
        face(pose, vertices, color, 0, 1, 0, minX, maxY, minZ, maxX, maxY, maxZ);
        face(pose, vertices, color, 0, -1, 0, minX, minY, maxZ, maxX, minY, minZ);
    }

    private static void face(PoseStack.Pose pose, VertexConsumer vertices, int color, float normalX, float normalY, float normalZ, float x1, float y1, float z1, float x2, float y2, float z2) {
        vertices.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, normalX, normalY, normalZ);
        vertices.addVertex(pose, x2, y1, z2).setColor(color).setNormal(pose, normalX, normalY, normalZ);
        vertices.addVertex(pose, x2, y2, z2).setColor(color).setNormal(pose, normalX, normalY, normalZ);
        vertices.addVertex(pose, x1, y2, z1).setColor(color).setNormal(pose, normalX, normalY, normalZ);
    }
}
