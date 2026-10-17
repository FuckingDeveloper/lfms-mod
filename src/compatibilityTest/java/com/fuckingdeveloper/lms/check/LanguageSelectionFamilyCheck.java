package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.language.LegacyLanguageInfo;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceLanguages;
import java.lang.invoke.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class LanguageSelectionFamilyCheck {
    private static final String INFO="net/minecraft/client/resources/language/LanguageInfo", MANAGER="net/minecraft/client/resources/language/LanguageManager", HOST="fixture/LanguageQueries";
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        var a=new LegacyLanguageInfo("tr_tr","Region","First",false);
        var b=new LegacyLanguageInfo("tr_tr","Other","Second",true);
        check(a!=b && a.equals(b) && a.hashCode()==b.hashCode() && a.compareTo(b)==0,"source code identity/equality");
        check(a.getJavaLocale().equals(Locale.forLanguageTag("tr-TR")) && a.toString().equals("First (Region)"),"source locale/name formatting");
        var manager=new LanguageManager(null,"en_us",language->{});
        var initial=Target263SourceLanguages.getSelected(manager);
        check(initial==Target263SourceLanguages.getLanguage(manager,"en_us"),"catalog/selection identity");
        check(Target263SourceLanguages.getLanguage(manager,"unknown")==null,"unknown language invented");
        var detached=Target263SourceLanguages.getLanguages(manager);detached.clear();
        check(!Target263SourceLanguages.getLanguages(manager).isEmpty(),"source set mutation changed native catalog");
        Target263SourceLanguages.setSelected(manager,a);
        check(Target263SourceLanguages.getSelected(manager)==a && manager.getSelected().equals("tr_tr"),"source selection object/code lost");
        manager.setSelected("en_us");
        check(Target263SourceLanguages.getSelected(manager)==initial,"native selection did not clear source selection");
        var oldLanguage=net.minecraft.locale.Language.getInstance();
        try {
            Target263SourceLanguages.setSelected(manager,new LegacyLanguageInfo("en_us","custom","custom",true));
            manager.onResourceManagerReload(resources(Map.of("en_us",new LanguageInfo("New Region","Reloaded",false))));
            var reloaded=Target263SourceLanguages.getSelected(manager);
            check(reloaded!=initial && reloaded.getName().equals("Reloaded"),"ordinary native reload did not refresh source metadata/selection");
            check(initial.getName().equals("English"),"reload mutated old source snapshot");
            check(reloaded==Target263SourceLanguages.getLanguage(manager,"en_us"),"reload lookup/selection identity");
            manager.setSelected("missing");
            check(Target263SourceLanguages.getSelected(manager)==reloaded,"source missing-selection fallback");
        } finally {net.minecraft.locale.Language.inject(oldLanguage);manager.setSelected("en_us");}
        var path=Files.createTempFile("source-language-",".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){entry(jar,HOST,fixture());entry(jar,"fixture/LanguageOverride",overrideFixture());}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,LanguageSelectionFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                var value=(LegacyLanguageInfo)type.getMethod("create").invoke(null);
                check(value.getCode().equals("tr_tr") && value.getJavaLocale().getCountry().equals("TR"),"source constructor type projection");
                check(type.getField("kind").get(null)==LegacyLanguageInfo.class,"source class token projection");
                check(type.getMethod("rtl",LegacyLanguageInfo.class).invoke(null,value).equals(false),"source SRG getter");
                check(type.getMethod("selected",LanguageManager.class).invoke(null,manager)==Target263SourceLanguages.getSelected(manager),"source manager selected call");
                check(type.getMethod("lookup",LanguageManager.class,String.class).invoke(null,manager,"en_us")==Target263SourceLanguages.getLanguage(manager,"en_us"),"source lookup call");
                check(((SortedSet<?>)type.getMethod("languages",LanguageManager.class).invoke(null,manager)).contains(Target263SourceLanguages.getSelected(manager)),"source sorted-set call");
                type.getMethod("select",LanguageManager.class,LegacyLanguageInfo.class).invoke(null,manager,value);
                check(Target263SourceLanguages.getSelected(manager)==value,"source setter call/object identity");
                for(String method:List.of("handle","nested"))try {
                    var handle=(MethodHandle)type.getMethod(method).invoke(null);
                    check(handle.invokeWithArguments(manager)==Target263SourceLanguages.getSelected(manager),"source manager callable");
                }catch(Throwable failure){throw new AssertionError(failure);}
                var child=loader.linkOwnedClass("fixture.LanguageOverride");var source=(LegacyLanguageInfo)child.getConstructor().newInstance();
                check(source.isBidirectional() && child.getMethod("base").invoke(source).equals(false),"source override/base dispatch lost");
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
        } finally {Files.deleteIfExists(path);}
        var jei=Path.of("legacy-mods/jei-1.19.2-forge-11.8.1.1032.jar");
        if(Files.isRegularFile(jei))try(var loader=new ManagedLegacyClassLoader(jei,LanguageSelectionFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var method=loader.linkOwnedClass("mezz.jei.common.util.Translator").getDeclaredMethod("getLocale",LegacyLanguageInfo.class);method.setAccessible(true);
            check(method.invoke(null,a).equals(a.getJavaLocale()),"original JEI code/locale consumer");
        }
        System.out.println("PASS: source language values/constructor/SRG/override/base, code equality and locale, native catalog/selected identity, detached sort set, source/native selection and actual native resource reload, callable/nested manager access and original JEI locale consumer");
    }
    private static ResourceManager resources(Map<String,LanguageInfo> languages) {
        var pack=(PackResources)Proxy.newProxyInstance(LanguageSelectionFamilyCheck.class.getClassLoader(),new Class<?>[]{PackResources.class},(p,m,args)->{
            if(m.getName().equals("getMetadataSection"))return args[0]==LanguageMetadataSection.TYPE?new LanguageMetadataSection(languages):null;
            if(m.getName().equals("packId"))return "language-fixture";
            throw new UnsupportedOperationException("Unrequested pack operation: "+m);
        });
        var resource=new Resource(pack,()->new java.io.ByteArrayInputStream("{\"fixture\":\"value\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return (ResourceManager)Proxy.newProxyInstance(LanguageSelectionFamilyCheck.class.getClassLoader(),new Class<?>[]{ResourceManager.class},(p,m,args)->switch(m.getName()) {
            case "listPacks" -> java.util.stream.Stream.of(pack);
            case "getNamespaces" -> Set.of("minecraft");
            case "getResourceStack" -> List.of(resource);
            default -> throw new UnsupportedOperationException("Unrequested resource operation: "+m);
        });
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        w.visitField(ACC_PUBLIC|ACC_STATIC,"kind","Ljava/lang/Class;","Ljava/lang/Class<L"+INFO+";>;",null).visitEnd();
        var m=w.visitMethod(ACC_STATIC,"<clinit>","()V",null,null);m.visitCode();m.visitLdcInsn(Type.getObjectType(INFO));m.visitFieldInsn(PUTSTATIC,HOST,"kind","Ljava/lang/Class;");m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"create","()L"+INFO+";",null,null);m.visitCode();construct(m,INFO);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"rtl","(L"+INFO+";)Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,INFO,"m_118952_","()Z",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"selected","(L"+MANAGER+";)L"+INFO+";",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,MANAGER,"m_118983_","()L"+INFO+";",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"lookup","(L"+MANAGER+";Ljava/lang/String;)L"+INFO+";",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,MANAGER,"m_118976_","(Ljava/lang/String;)L"+INFO+";",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"languages","(L"+MANAGER+";)Ljava/util/SortedSet;",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,MANAGER,"m_118984_","()Ljava/util/SortedSet;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"select","(L"+MANAGER+";L"+INFO+";)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,MANAGER,"m_118974_","(L"+INFO+";)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        var handle=new Handle(H_INVOKEVIRTUAL,MANAGER,"m_118983_","()L"+INFO+";",false);
        var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(LanguageSelectionFamilyCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(String name:List.of("handle","nested")){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(name.equals("nested")?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle)):handle);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        w.visitEnd();return w.toByteArray();
    }
    private static byte[] overrideFixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/LanguageOverride",null,INFO,null);
        var m=w.visitMethod(ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);args(m);m.visitMethodInsn(INVOKESPECIAL,INFO,"<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_118952_","()Z",null,null);m.visitCode();m.visitInsn(ICONST_1);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"base","()Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,INFO,"m_118952_","()Z",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void args(MethodVisitor m){m.visitLdcInsn("tr_tr");m.visitLdcInsn("TR");m.visitLdcInsn("Turkish");m.visitInsn(ICONST_0);}
    private static void construct(MethodVisitor m,String owner){m.visitTypeInsn(NEW,owner);m.visitInsn(DUP);args(m);m.visitMethodInsn(INVOKESPECIAL,owner,"<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V",false);}
    private static void entry(JarOutputStream jar,String name,byte[] bytes)throws Exception{jar.putNextEntry(new JarEntry(name+".class"));jar.write(bytes);jar.closeEntry();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
