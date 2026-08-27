package dev.blinddeafmute.client;

import dev.blinddeafmute.network.RoleSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class BlindDeafMuteClient implements ClientModInitializer {
    private static boolean started;
    private static boolean blind;
    private static boolean deaf;
    private static boolean mute;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RoleSyncPayload.TYPE, (payload, context) -> {
            boolean nextStarted = payload.started();
            boolean nextBlind = payload.blind();
            boolean nextDeaf = payload.deaf();
            boolean nextMute = payload.mute();
            Minecraft client = context.client();
            client.execute(() -> {
                started = nextStarted;
                blind = nextBlind;
                deaf = nextDeaf;
                mute = nextMute;
            });
        });
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> renderStatus(guiGraphics));
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
