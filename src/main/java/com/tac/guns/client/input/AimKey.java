package com.tac.guns.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.tac.guns.api.client.player.IClientPlayerGunOperator;
import com.tac.guns.api.item.IGun;
import com.tac.guns.config.client.KeyConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.tac.guns.util.InputExtraCheck.isInGame;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AimKey {
    private static boolean isPressed = false;

    public static final KeyMapping AIM_KEY = new KeyMapping("key.tac.aim.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.category.tac");

    @SubscribeEvent
    public static void onAimPress(InputEvent.MouseInputEvent event) {
        if (isInGame() && AIM_KEY.matchesMouse(event.getButton())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (IGun.mainhandHoldGun(player)) {
                if (KeyConfig.HOLD_TO_AIM.get()) {
                    if (event.getAction() == GLFW.GLFW_PRESS) {
                        IClientPlayerGunOperator.fromLocalPlayer(player).aim(true);
                    }
                    if (event.getAction() == GLFW.GLFW_RELEASE) {
                        IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
                    }
                } else {
                    boolean currentlyAiming = IClientPlayerGunOperator.fromLocalPlayer(player).isAim();
                    if (event.getAction() == GLFW.GLFW_PRESS && !isPressed) {
                        IClientPlayerGunOperator.fromLocalPlayer(player).aim(!currentlyAiming);
                        isPressed = true;
                    }
                    if (event.getAction() == GLFW.GLFW_RELEASE) {
                        isPressed = false;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void cancelAim(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!(player instanceof IClientPlayerGunOperator operator)) {
            return;
        }
        if (operator.isAim() && !isInGame()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
        }
    }
}
