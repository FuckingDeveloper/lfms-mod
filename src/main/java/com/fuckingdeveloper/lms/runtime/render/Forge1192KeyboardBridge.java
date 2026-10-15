package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.neoforge.client.event.InputEvent;

/** Named-key intent on SDL. Raw operating-system GLFW scancodes are not reproduced. */
public final class Forge1192KeyboardBridge {
    private static final Map<Integer,String> KEYS=new LinkedHashMap<>(),MOUSE=new LinkedHashMap<>();
    private static final Map<String,Integer> SOURCE_CODES=new HashMap<>();
    static {
        try(var in=Objects.requireNonNull(Forge1192KeyboardBridge.class.getResourceAsStream("/lms/source/forge1192/input-keys.tsv"));var reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){
            for(String line;(line=reader.readLine())!=null;){
                if(line.startsWith("#")||line.isBlank())continue;
                String[] parts=line.split("\t");Map<Integer,String> map=parts[0].equals("KEYSYM")?KEYS:MOUSE;
                if(map.putIfAbsent(Integer.parseInt(parts[1]),parts[2])!=null)throw new IllegalStateException("Duplicate source input key");
                SOURCE_CODES.put(parts[2],Integer.parseInt(parts[1]));
            }
        }catch(IOException failure){throw new ExceptionInInitializerError(failure);}
    }
    private Forge1192KeyboardBridge(){}
    public static InputConstants.Key key(InputConstants.Type type,int sourceCode){
        var names=type==InputConstants.Type.KEYBOARD?KEYS:type==InputConstants.Type.MOUSE?MOUSE:null;
        String name=names==null?null:names.get(sourceCode);
        if(name==null)throw new UnsupportedOperationException("Unmapped source input key "+type+":"+sourceCode);
        return InputConstants.getKey(name);
    }
    public static int keyboardCode(int sourceCode){return key(InputConstants.Type.KEYBOARD,sourceCode).getValue();}
    public static int mouseCode(int sourceCode){return key(InputConstants.Type.MOUSE,sourceCode).getValue();}
    public static int sourceValue(InputConstants.Key key){
        return SOURCE_CODES.getOrDefault(key.getName(),-1);
    }
    public static InputConstants.Type rawScanType(){throw new UnsupportedOperationException("Raw GLFW scancode binding is not adapted to SDL; use named key intent");}
    public static int getKey(InputEvent.Key event){return sourceValue(InputConstants.Type.KEYBOARD.getOrCreate(event.getKey()));}
    /** Normalized target physical code; it is not an OS-specific GLFW scan number. */
    public static int getScanCode(InputEvent.Key event){return event.getKey();}
    public static int getAction(InputEvent.Key event){return event.getAction();}
    public static int getModifiers(InputEvent.Key event){
        int nativeFlags=event.getModifiers(),source=0;
        if((nativeFlags&InputConstants.MOD_SHIFT)!=0)source|=1;
        if((nativeFlags&InputConstants.MOD_CONTROL)!=0)source|=2;
        if((nativeFlags&InputConstants.MOD_ALT)!=0)source|=4;
        if((nativeFlags&InputConstants.MOD_SUPER)!=0)source|=8;
        if((nativeFlags&InputConstants.MOD_CAPS_LOCK)!=0)source|=16;
        if((nativeFlags&InputConstants.MOD_NUM_LOCK)!=0)source|=32;
        return source;
    }
    public static boolean matches(KeyMapping mapping,int sourceKey,int normalizedPhysical){
        if(sourceKey==-1)return false; // No raw scan-only bindings are admitted.
        return mapping.matches(new KeyEvent(keyboardCode(sourceKey),0,0));
    }
    public static boolean matchesMouse(KeyMapping mapping,int button){
        return MOUSE.containsKey(button) && mapping.matchesMouse(new MouseButtonEvent(0,0,new net.minecraft.client.input.MouseButtonInfo(mouseCode(button),0)));
    }
}
