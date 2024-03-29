package com.tac.guns.compat.cloth.common;

import com.tac.guns.config.common.OtherConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.TranslatableComponent;

public class OtherClothConfig {
    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory other = root.getOrCreateCategory(new TranslatableComponent("config.tac.common.other"));

        other.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.tac.common.other.ammo_box_stack_size"), OtherConfig.AMMO_BOX_STACK_SIZE.get())
                .setMin(1).setMax(Integer.MAX_VALUE).setDefaultValue(5).setTooltip(new TranslatableComponent("config.tac.common.other.ammo_box_stack_size.desc"))
                .setSaveConsumer(OtherConfig.AMMO_BOX_STACK_SIZE::set).build());
    }
}
