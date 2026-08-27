package dev.blinddeafmute.voicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent;
import dev.blinddeafmute.BlindDeafMute;
import dev.blinddeafmute.role.RoleManager;

import java.util.UUID;

public final class BlindDeafMuteVoicePlugin implements VoicechatPlugin {
    private VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return BlindDeafMute.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi voicechatServerApi) {
            serverApi = voicechatServerApi;
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, event -> {
            if (event.getSenderConnection() != null && BlindDeafMute.ROLES.hasRole(event.getSenderConnection().getPlayer().getUuid(), RoleManager.Role.MUTE)) {
                event.cancel();
            }
        });
        registration.registerEvent(LocationalSoundPacketEvent.class, this::cancelForDeafReceiver);
        registration.registerEvent(EntitySoundPacketEvent.class, this::cancelForDeafReceiver);
        registration.registerEvent(StaticSoundPacketEvent.class, this::cancelForDeafReceiver);
    }

    private void cancelForDeafReceiver(de.maxhenkel.voicechat.api.events.SoundPacketEvent<?> event) {
        if (event.getReceiverConnection() != null) {
            UUID receiver = event.getReceiverConnection().getPlayer().getUuid();
            if (BlindDeafMute.ROLES.hasRole(receiver, RoleManager.Role.DEAF)) {
                event.cancel();
            }
        }
    }
}
