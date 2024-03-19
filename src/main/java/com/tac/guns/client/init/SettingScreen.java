package com.tac.guns.client.init;

import com.tac.guns.GunMod;
import net.minecraft.client.CycleOption;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT)
public class SettingScreen {
    private static Field mouseOptionsField;

    // 注册鼠标设置界面按钮
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof MouseSettingsScreen screen) {
            if (mouseOptionsField == null) {
                mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
                mouseOptionsField.setAccessible(true);
            }
            try {
                OptionsList list = (OptionsList) mouseOptionsField.get(screen);
                list.addSmall(new CycleOption[]{GunOptions.HOLD_TO_AIM});
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
