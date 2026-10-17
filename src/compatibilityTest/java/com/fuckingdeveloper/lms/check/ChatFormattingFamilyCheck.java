package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.chat.Forge1192ChatFormattingBridge;
import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import net.minecraft.ChatFormatting;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class ChatFormattingFamilyCheck {
    public static void run() throws Exception {
        var declarations=new Forge1192MethodDeclarations(name->null);
        for(var rule:Forge1192ChatFormattingRules.entries()) {
            var source=rule.source();
            check(Forge1192MethodRules.resolve(rule.opcode(),source.owner(),source.name(),source.descriptor(),declarations).isPresent(),"catalog identity lost");
            var node=new ClassNode();node.name="fixture/Formatting";node.superName="java/lang/Object";
            var method=new MethodNode(ACC_PUBLIC|ACC_STATIC,"test","()V",null,null);node.methods.add(method);
            method.instructions.add(new MethodInsnNode(rule.opcode(),source.owner(),source.name(),source.descriptor(),false));
            var handle=new Handle(rule.opcode()==INVOKESTATIC?H_INVOKESTATIC:H_INVOKEVIRTUAL,source.owner(),source.name(),source.descriptor(),false);
            method.instructions.add(new LdcInsnNode(handle));
            method.instructions.add(new LdcInsnNode(new ConstantDynamic("value","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","get","()V",false),new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","get","()V",false),handle))));
            int changes=Forge1192MethodRules.rewrite(node,declarations,new SemanticInvocationRewriter(String::equals));
            check(changes==3,"direct/handle/nested source formatting rule lost: "+source);
            check(((MethodInsnNode)method.instructions.getFirst()).owner.equals(rule.bridge().getOwner()),"bridge owner wrong");
            check(Forge1192MethodRules.bridge(rule.opcode(),"fixture/Unrelated",source.name(),source.descriptor(),declarations)==null,"descriptor-only match");
            check(Forge1192MethodRules.resolve(rule.opcode()==INVOKESTATIC?INVOKEVIRTUAL:INVOKESTATIC,source.owner(),source.name(),source.descriptor(),declarations).isEmpty(),"wrong opcode admitted");
        }
        String codes="0123456789abcdefklmnor";
        int[] colors={0,170,43520,43690,11141120,11141290,16755200,11184810,5592405,5592575,5635925,5636095,16733525,16733695,16777045,16777215};
        var values=ChatFormatting.values();check(values.length==codes.length(),"source enum set changed");
        for(int i=0;i<values.length;i++) {
            var value=values[i];check(Forge1192ChatFormattingBridge.getChar(value)==codes.charAt(i),"source code");
            check(Forge1192ChatFormattingBridge.getId(value)==(i<16?i:-1),"source ID");
            check(Forge1192ChatFormattingBridge.isColor(value)==(i<16),"source color predicate");
            check(Forge1192ChatFormattingBridge.isFormat(value)==(i>=16&&i<21),"source format predicate");
            check(Objects.equals(Forge1192ChatFormattingBridge.getColor(value),i<16?colors[i]:null),"source RGB/null");
            check(Forge1192ChatFormattingBridge.byName("!"+value.name()+"-")==value&&Forge1192ChatFormattingBridge.byCode(Character.toUpperCase(codes.charAt(i)))==value,"normalized lookup");
        }
        check(Forge1192ChatFormattingBridge.byId(-99)==ChatFormatting.RESET&&Forge1192ChatFormattingBridge.byId(16)==null&&Forge1192ChatFormattingBridge.byName(null)==null&&Forge1192ChatFormattingBridge.byName("unknown")==null,"lookup absence");
        for(boolean colorsEnabled:List.of(false,true))for(boolean formats:List.of(false,true)) {
            var names=Forge1192ChatFormattingBridge.getNames(colorsEnabled,formats);
            check(names.size()==1+(colorsEnabled?16:0)+(formats?5:0)&&names.contains("reset"),"source list filtering/reset");
            names.clear();check(!Forge1192ChatFormattingBridge.getNames(colorsEnabled,formats).isEmpty(),"mutable list alias");
        }
        check(Forge1192ChatFormattingBridge.stripFormatting("a§cRed§r!").equals("aRed!")&&Forge1192ChatFormattingBridge.stripFormatting(null)==null,"strip semantics");
        try{Forge1192ChatFormattingBridge.isColor(null);throw new AssertionError("null receiver accepted");}catch(NullPointerException expected){}
        System.out.println("PASS: full source formatting metadata/code/name/ID/filter family, native enum/RGB identity, direct/handles/nested constants and wrong-owner/opcode negatives");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
