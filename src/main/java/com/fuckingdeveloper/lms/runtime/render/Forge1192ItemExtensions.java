package com.fuckingdeveloper.lms.runtime.render;

/** Source renderer extension, deliberately distinct from modern item extensions. */
public interface Forge1192ItemExtensions {
    default Forge1192CustomItemRenderer getCustomRenderer() {
        throw new UnsupportedOperationException("Source item extension has no custom renderer");
    }
}
