package dev.blinddeafmute.client;

import dev.blinddeafmute.network.RoleSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
        ClientTickEvents.END_CLIENT_TICK.register(BlindDeafMuteClient::updateRoleNameplates);
    }

    private static void updateRoleNameplates(Minecraft client) {
        if (client.level == null) {
            return;
        }
        for (Player player : client.level.players()) {
            if (player == client.player) {
                continue;
            }
            RoleFlags roles = playerRoles.get(player.getUUID());
            if (!started || roles == null || !roles.hasAny()) {
                player.setCustomName(null);
                player.setCustomNameVisible(false);
                continue;
            }
            StringBuilder badge = new StringBuilder();
            if (roles.blind()) {
                badge.append("[BLIND] ");
            }
            if (roles.deaf()) {
                badge.append("[HEADPHONES] ");
            }
            if (roles.mute()) {
                badge.append("[MUTE] ");
            }
            player.setCustomName(Component.literal(badge + player.getName().getString()));
            player.setCustomNameVisible(true);
        }
    }

    private record RoleFlags(boolean blind, boolean deaf, boolean mute) {
        private boolean hasAny() {
            return blind || deaf || mute;
        }
    }

    private static void renderStatus(GuiGraphics graphics) {
        if (!started || Minecraft.getInstance().player == null) {
            return;
        }
        int x = 8;
        int y = 8;
        if (blind) {
            drawBadge(graphics, x, y, "BLIND", 0xFF4A1F2A);
            x += 58;
        }
        if (deaf) {
            drawBadge(graphics, x, y, "HEADPHONES", 0xFF173A4A);
            x += 92;
        }
        if (mute) {
            drawBadge(graphics, x, y, "MUTE", 0xFF3B3320);
        }
    }

    private static void drawBadge(GuiGraphics graphics, int x, int y, String label, int color) {
        int width = Minecraft.getInstance().font.width(label) + 12;
        graphics.fill(x, y, x + width, y + 16, color);
        graphics.drawString(Minecraft.getInstance().font, label, x + 6, y + 4, 0xFFFFFFFF, false);
    }
}
