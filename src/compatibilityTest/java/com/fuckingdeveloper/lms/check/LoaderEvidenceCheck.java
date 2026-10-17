package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.discovery.*;
import com.fuckingdeveloper.lms.profile.InstalledSourceProfiles;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.nio.charset.StandardCharsets;

/** Declared eligibility is invariant under artifact name changes and never overrides Minecraft metadata. */
final class LoaderEvidenceCheck {
    static void run() throws Exception {
        var path=Files.createTempFile("arbitrary-name", ".jar");
        try {
            for(var test:List.of(
                    new Case("javafml","[43,)","",true), new Case("javafml","[43]","",true),
                    new Case("javafml","[44,)","",false), new Case("javafml","[42,43)","",false),
                    new Case("javafml","garbage","",false), new Case("javafml","(,)","",false),
                    new Case("javafml","[43oops,)","",false), new Case("kotlinforforge","[43,)","",false),
                    new Case("javafml","[43,)","[1.19.4]",false), new Case("javafml","[43,)","[1.19.2]",true))) {
                String metadata="modLoader=\""+test.loader+"\"\nloaderVersion=\""+test.range+"\"\nlicense=\"test\"\n[[mods]]\nmodId=\"probe\"\nversion=\"1\"\ndescription='''loaderVersion=\"[1,)\"\nmodId=\"minecraft\"\nversionRange=\"[1.19.4]\"'''\n";
                if(!test.minecraft.isEmpty())metadata+="[[dependencies.probe]]\nmodId=\"minecraft\"\nversionRange=\""+test.minecraft+"\"\n";
                try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("META-INF/mods.toml"));jar.write(metadata.getBytes(StandardCharsets.UTF_8));jar.closeEntry();}
                byte[] original=Files.readAllBytes(path);var descriptor=new LegacyJarScanner().inspect(path);
                check(descriptor.languageLoader().equals(test.loader)&&descriptor.languageLoaderVersion().equals(test.range),"loader evidence lost");
                check(InstalledSourceProfiles.create().resolve(descriptor).isPresent()==test.accept,"wrong source eligibility: "+test);
                check(Arrays.equals(original,Files.readAllBytes(path)),"source JAR mutated");
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: parsed JavaFML compatibility evidence, explicit Minecraft precedence, unknown/malformed/wrong loader rejection, quoted-description isolation and immutable randomly named JAR");
    }
    private record Case(String loader,String range,String minecraft,boolean accept){}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
