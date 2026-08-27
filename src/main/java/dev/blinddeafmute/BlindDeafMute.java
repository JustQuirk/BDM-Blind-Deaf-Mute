package dev.blinddeafmute;

import dev.blinddeafmute.role.RoleManager;
import dev.blinddeafmute.config.BdmConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import dev.blinddeafmute.network.RoleSyncPayload;

public final class BlindDeafMute implements ModInitializer {
    public static final String MOD_ID = "blinddeafmute";
    public static final RoleManager ROLES = new RoleManager();

    @Override
    public void onInitialize() {
        BdmConfig.load();
        PayloadTypeRegistry.playS2C().register(RoleSyncPayload.TYPE, RoleSyncPayload.CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                    dispatcher.register(literal("blind").requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                        .then(argument("player", EntityArgument.player()).executes(context -> toggle(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"), RoleManager.Role.BLIND))));
                    dispatcher.register(literal("deaf").requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                        .then(argument("player", EntityArgument.player()).executes(context -> toggle(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"), RoleManager.Role.DEAF))));
                    dispatcher.register(literal("mute").requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
                        .then(argument("player", EntityArgument.player()).executes(context -> toggle(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player"), RoleManager.Role.MUTE))));
                    dispatcher.register(literal("start").requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))).executes(context -> {
                ROLES.setStarted(true);
                context.getSource().sendSuccess(() -> Component.literal("Blind, Deaf, Mute roles are now active."), true);
                ROLES.syncAll(context.getSource().getServer());
                return 1;
            }));
            dispatcher.register(literal("bdm:test").executes(context -> {
                if (!BdmConfig.isTestCommandEnabled()) {
                    context.getSource().sendFailure(Component.literal("/bdm:test is disabled in the BDM config."));
                    return 0;
                }
                ServerPlayer player = context.getSource().getPlayerOrException();
                ROLES.addTestRoles(player, context.getSource().getServer().getTickCount());
                ROLES.setStarted(true);
                ROLES.sync(player);
                player.sendSystemMessage(Component.literal("Test roles applied. Blindness lasts 10 seconds; voice roles remain active."));
                return 1;
            }));
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ROLES.isStarted()) {
                ROLES.tick(server);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ROLES.sync(handler.getPlayer()));
    }

    private static int toggle(ServerPlayer source, ServerPlayer target, RoleManager.Role role) {
        boolean enabled = ROLES.toggle(target, role);
        ROLES.sync(target);
        source.sendSystemMessage(Component.literal(role.displayName() + " " + (enabled ? "enabled" : "disabled") + " for " + target.getName().getString()));
        target.sendSystemMessage(Component.literal("Role: " + role.displayName() + " " + (enabled ? "enabled" : "disabled")));
        return 1;
    }
}
