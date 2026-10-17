package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.mixin.Target263ItemPropertiesAccess;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.LegacyDeferredRegister;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BannerPattern;

/** Real native item with the native delayed pattern holder-set component. */
public class Target263BannerPatternItem extends Item implements com.fuckingdeveloper.lms.profile.forge1192.runtime.banner.LegacyBannerPatternItemIntent {
    private final TagKey<BannerPattern> pattern;
    public Target263BannerPatternItem(TagKey<BannerPattern> pattern,Properties properties) {
        super(properties(pattern,properties));this.pattern=Objects.requireNonNull(pattern);
    }
    private static Properties properties(TagKey<BannerPattern> pattern,Properties properties) {
        Objects.requireNonNull(pattern);
        var scoped=LegacyDeferredRegister.atItemConstruction(properties);
        var id=((Target263ItemPropertiesAccess)(Object)scoped).lms$itemId();
        if(id==null)throw new IllegalStateException("Source banner pattern item requires proven construction identity");
        return Target263ItemPropertiesSnapshots.withId(scoped,id)
                .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS,context->context.getOrThrow(pattern));
    }
    public TagKey<BannerPattern> getBannerPattern() {return pattern;}
    public MutableComponent getDisplayName() {return Component.translatable(getDescriptionId()+".desc");}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> output,TooltipFlag flag) {
        output.accept(getDisplayName().withStyle(ChatFormatting.GRAY));
    }
}
