package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceLanguages;
import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
abstract class Target263LanguageManagerMixin implements Target263SourceLanguages.Carrier {
    @Unique private Target263SourceLanguages.State lms$languageState;
    @Override public synchronized Target263SourceLanguages.State lms$sourceLanguages() {
        if (lms$languageState == null) lms$languageState = new Target263SourceLanguages.State();
        return lms$languageState;
    }
    @Inject(method="setSelected", at=@At("TAIL"), require=1, allow=1)
    private void lms$selectionChanged(CallbackInfo callback) { lms$sourceLanguages().selectionChanged(); }
    @Inject(method="onResourceManagerReload", at=@At(value="FIELD",
            target="Lnet/minecraft/client/resources/language/LanguageManager;languages:Ljava/util/Map;",
            opcode=org.objectweb.asm.Opcodes.PUTFIELD, shift=At.Shift.AFTER), require=1, allow=1)
    private void lms$reloadLanguages(CallbackInfo callback) { lms$sourceLanguages().reloading(); }
}
