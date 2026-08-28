package dev.blinddeafmute.client;

import dev.blinddeafmute.network.RoleSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BlindDeafMuteClient implements ClientModInitializer {
    private static boolean started;
    private static boolean blind;
    private static boolean deaf;
    private static boolean mute;
    private static final Map<UUID, RoleFlags> playerRoles = new ConcurrentHashMap<>();

    public static boolean isStarted() {
        return started;
    }

    public static RoleFlags rolesFor(UUID playerId) {
        return playerRoles.get(playerId);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RoleSyncPayload.TYPE, (payload, context) -> {
            boolean nextStarted = payload.started();
            boolean nextBlind = payload.blind();
            boolean nextDeaf = payload.deaf();
            boolean nextMute = payload.mute();
            UUID playerId = payload.playerId();
            Minecraft client = context.client();
            client.execute(() -> {
                started = nextStarted;
                blind = nextBlind;
                deaf = nextDeaf;
                mute = nextMute;
                playerRoles.put(playerId, new RoleFlags(nextBlind, nextDeaf, nextMute));
            });
        });
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> renderStatus(guiGraphics));
    }

    public record RoleFlags(boolean blind, boolean deaf, boolean mute) {
        public boolean hasAny() {
            return blind || deaf || mute;
        }
    }

    private static void renderStatus(GuiGraphics graphics) {
        if (!started || Minecraft.getInstance().player == null) {
            return;
        }
        int y = 8;
        for (Player player : Minecraft.getInstance().level.players()) {
            RoleFlags roles = playerRoles.get(player.getUUID());
            if (roles != null && roles.hasAny()) {
                drawPlayerRoleRow(graphics, player, roles, 8, y);
                y += 40;
            }
        }
    }

    private static void drawPlayerRoleRow(GuiGraphics graphics, Player player, RoleFlags roles, int x, int y) {
        String roleLabel = roleLabel(roles);
        int frameX = x + 38;
        int frameWidth = Minecraft.getInstance().font.width(roleLabel) + 14;
        graphics.fill(frameX, y + 7, frameX + frameWidth, y + 29, 0xCC10151D);
        if (player instanceof AbstractClientPlayer clientPlayer) {
            var texture = clientPlayer.getSkin().body().texturePath();
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + 2, y + 2, 8, 8, 32, 32, 64, 64);
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + 2, y + 2, 40, 8, 32, 32, 64, 64);
        }
        graphics.drawString(Minecraft.getInstance().font, roleLabel, frameX + 7, y + 14, 0xFFFFFFFF, false);
    }

    private static String roleLabel(RoleFlags roles) {
        StringBuilder label = new StringBuilder();
        if (roles.blind()) label.append("BLIND ");
        if (roles.deaf()) label.append("DEAF ");
        if (roles.mute()) label.append("MUTE");
        return label.toString().trim();
    }
}
