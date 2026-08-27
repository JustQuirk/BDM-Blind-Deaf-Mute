package dev.blinddeafmute.client;

import dev.blinddeafmute.config.BdmConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class BdmConfigScreen extends Screen {
    private final Screen parent;
    private EditBox portBox;
    private Button testCommandButton;
    private Button voiceTestButton;

    public BdmConfigScreen(Screen parent) {
        super(Component.literal("BDM Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        portBox = new EditBox(font, center - 100, 62, 200, 20, Component.literal("Voice port"));
        portBox.setValue(Integer.toString(BdmConfig.getVoicePort()));
        portBox.setFilter(value -> value.length() <= 5 && value.chars().allMatch(Character::isDigit));
        addRenderableWidget(portBox);

        testCommandButton = addRenderableWidget(Button.builder(testCommandLabel(), button -> {
            BdmConfig.setTestCommandEnabled(!BdmConfig.isTestCommandEnabled());
            button.setMessage(testCommandLabel());
        }).bounds(center - 100, 98, 200, 20).build());

        voiceTestButton = addRenderableWidget(Button.builder(Component.literal("Run voice setup check"), button -> runVoiceTest()).bounds(center - 100, 134, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> saveAndClose()).bounds(center - 100, 182, 200, 20).build());
    }

    private Component testCommandLabel() {
        return Component.literal("/bdm:test: " + (BdmConfig.isTestCommandEnabled() ? "Enabled" : "Disabled"));
    }

    private void runVoiceTest() {
        if (!BdmConfig.isVoiceTestEnabled()) {
            voiceTestButton.setMessage(Component.literal("Voice test disabled"));
            return;
        }
        boolean installed = FabricLoader.getInstance().isModLoaded("voicechat");
        String result = installed ? "Voice chat installed: check V" : "Simple Voice Chat not found";
        voiceTestButton.setMessage(Component.literal(result));
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("BDM voice test: " + result + ". Configured port: " + portBox.getValue()), false);
        }
    }

    private void saveAndClose() {
        int port;
        try {
            port = Integer.parseInt(portBox.getValue());
        } catch (NumberFormatException exception) {
            port = BdmConfig.getVoicePort();
        }
        BdmConfig.setVoicePort(port);
        onClose();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 25, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Simple Voice Chat UDP port"), width / 2, 45, 0xFFCCCCCC);
        graphics.drawCenteredString(font, Component.literal("/bdm:test is disabled by default"), width / 2, 166, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, delta);
    }

}
