package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.tree.ClassNode;

final class Forge1192TagNotificationRewriter {
    static final String SOURCE="net/minecraftforge/event/TagsUpdatedEvent";
    private static final String VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/event/LegacyTagsUpdatedEvent";
    private Forge1192TagNotificationRewriter(){}
    static String type(String name){
        if(name.equals(SOURCE))return VIEW;
        if(name.equals(SOURCE+"$UpdateCause"))return VIEW+"$UpdateCause";
        return null;
    }
    static int validate(ClassNode node){
        if(SOURCE.equals(node.superName))throw new UnsupportedOperationException("Source tag-event subclasses require a separate identity/dispatch adapter: "+node.name);
        return 0;
    }
}
