package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.mapping.Forge1192SrgRuntimeResolver;
import java.nio.file.Path;

public final class SourceCanonicalInformationCheck {
    public static void run() {
        var resolver=Forge1192SrgRuntimeResolver.load(Path.of(System.getProperty("user.dir"))).orElseThrow();
        String rs="com/mojang/blaze3d/systems/RenderSystem";
        check(resolver.canonicalMethodName(rs,"m_157427_","(Ljava/util/function/Supplier;)V").orElseThrow().equals("setShader"),"platform supplier identity");
        check(resolver.canonicalMethodName(rs,"m_157453_","(II)V").orElseThrow().equals("setShaderTexture"),"platform two-argument identity");
        check(resolver.canonicalMethodName("net/minecraft/client/gui/Font","m_92889_","(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I").orElseThrow().equals("draw"),"five-argument font identity");
        check(resolver.canonicalMethodName(rs,"m_157453_","(IF)V").isEmpty(),"wrong descriptor selected");
        check(resolver.canonicalMethodName("com/mojang/Unrelated","m_157453_","(II)V").isEmpty(),"wrong owner selected");
        check(!resolver.resolve(rs,"m_157453_","(II)V").resolved(),"source declaration enabled blind target platform linking");
        System.out.println("PASS: pinned platform/source method identities, one/two/five argument parsing, wrong owner/descriptor rejection and independent target linkage boundary");
    }
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
