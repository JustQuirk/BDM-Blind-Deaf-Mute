package dev.blinddeafmute.network;

import dev.blinddeafmute.BlindDeafMute;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.UUID;

public record RoleSyncPayload(UUID playerId, boolean started, boolean blind, boolean deaf, boolean mute) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(BlindDeafMute.MOD_ID, "role_sync");
    public static final StreamCodec<RegistryFriendlyByteBuf, RoleSyncPayload> CODEC = new StreamCodec<>() {
        @Override
        public RoleSyncPayload decode(RegistryFriendlyByteBuf buf) {
            return new RoleSyncPayload(buf.readUUID(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RoleSyncPayload payload) {
            buf.writeUUID(payload.playerId());
            buf.writeBoolean(payload.started());
            buf.writeBoolean(payload.blind());
            buf.writeBoolean(payload.deaf());
            buf.writeBoolean(payload.mute());
        }
    };

    public static final Type<RoleSyncPayload> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
