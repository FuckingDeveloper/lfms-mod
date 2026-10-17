package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native caches must not silently outlive an LMS source snapshot's reload/data boundary. */
@Mixin(Ingredient.class)
public abstract class Target263SourceIngredientMixin implements Target263SourceIngredientAccess {
    @Unique private Target263SourceIngredients.State lms$ingredientState;
    public Target263SourceIngredients.State lms$ingredientState(){return lms$ingredientState;}
    public void lms$ingredientState(Target263SourceIngredients.State state){lms$ingredientState=state;}
    @Inject(method={"items","isEmpty","test(Lnet/minecraft/world/item/ItemStack;)Z","isSimple","acceptsItem","display","getCustomIngredient"},at=@At("HEAD"))
    private void lms$validateSourceSnapshot(CallbackInfoReturnable<?> result){if(lms$ingredientState!=null)lms$ingredientState.validate();}
}
