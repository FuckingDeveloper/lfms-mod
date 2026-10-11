package com.fuckingdeveloper.lms.runtime.recipe;

/**
 * Forge 1.19.2 marker contract for recipes whose grid shape is significant.
 * Recipe behavior remains supplied by the recipe's independent superclass;
 * this interface preserves only the Forge-added dimensions.
 */
public interface LegacyIShapedRecipe {
    int getRecipeWidth();

    int getRecipeHeight();
}
