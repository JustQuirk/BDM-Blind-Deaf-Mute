package dev.blinddeafmute.role;

import dev.blinddeafmute.network.RoleSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RoleManager {
    public enum Role {
        BLIND("Blind"), DEAF("Deaf"), MUTE("Mute");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private final Map<UUID, EnumSet<Role>> roles = new ConcurrentHashMap<>();
    private final Map<UUID, Long> testBlindnessUntil = new ConcurrentHashMap<>();
    private volatile boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean toggle(ServerPlayer player, Role role) {
        EnumSet<Role> playerRoles = roles.computeIfAbsent(player.getUUID(), ignored -> EnumSet.noneOf(Role.class));
        if (playerRoles.contains(role)) {
            playerRoles.remove(role);
            return false;
        }
        playerRoles.add(role);
        return true;
    }

    public void addRole(ServerPlayer player, Role role) {
        roles.computeIfAbsent(player.getUUID(), ignored -> EnumSet.noneOf(Role.class)).add(role);
    }

    public void addTestRoles(ServerPlayer player, long currentTick) {
        addRole(player, Role.BLIND);
        addRole(player, Role.DEAF);
        addRole(player, Role.MUTE);
        testBlindnessUntil.put(player.getUUID(), currentTick + 200L);
    }

    public boolean hasRole(UUID playerId, Role role) {
        return roles.getOrDefault(playerId, EnumSet.noneOf(Role.class)).contains(role);
    }

    public void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Long testExpiry = testBlindnessUntil.get(player.getUUID());
            if (hasRole(player.getUUID(), Role.BLIND) && (testExpiry == null || server.getTickCount() < testExpiry)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, false));
            }
            sync(player);
        }
    }

    public void sync(ServerPlayer player) {
        syncTo(player, player);
    }

    public void syncToAll(MinecraftServer server, ServerPlayer target) {
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            syncTo(receiver, target);
        }
    }

    private void syncTo(ServerPlayer receiver, ServerPlayer target) {
        ServerPlayNetworking.send(receiver, new RoleSyncPayload(target.getUUID(), started,
            hasRole(target.getUUID(), Role.BLIND),
            hasRole(target.getUUID(), Role.DEAF),
            hasRole(target.getUUID(), Role.MUTE)));
    }

    public void syncAll(MinecraftServer server) {
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            syncToAll(server, target);
        }
    }

    public void syncAllTo(MinecraftServer server, ServerPlayer receiver) {
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            syncTo(receiver, target);
        }
    }
}
