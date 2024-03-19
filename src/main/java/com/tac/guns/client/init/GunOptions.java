package com.tac.guns.client.init;

import com.tac.guns.config.ClientConfig;
import com.tac.guns.config.client.KeyConfig;
import net.minecraft.client.CycleOption;

import java.text.DecimalFormat;

public class GunOptions {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#");

    // 按住开镜按钮定义
    public static final CycleOption<Boolean> HOLD_TO_AIM =
            CycleOption.createOnOff("tac.options.holdToAim", (settings) ->
                    KeyConfig.HOLD_TO_AIM.get(), (settings, function, value) -> {
                    KeyConfig.HOLD_TO_AIM.set(value);
                    KeyConfig.HOLD_TO_AIM.save();
            });
}
