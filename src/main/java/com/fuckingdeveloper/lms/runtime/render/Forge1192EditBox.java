package com.fuckingdeveloper.lms.runtime.render;

import java.lang.invoke.*;
import java.util.function.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;

/** Source validation/callback intent over the actual native field and cursor. */
public class Forge1192EditBox extends EditBox {
    private Predicate<String> filter=value->true;
    private BiFunction<String,Integer,FormattedCharSequence> formatter;
    private boolean formatterInstalled,initialized=true,sourceShift;
    private KeyEvent activeKey;
    private MouseButtonEvent activeMouse;
    private static final VarHandle VALUE=field("value",String.class),LIMIT=field("maxLength",int.class),HIGHLIGHT=field("highlightPos",int.class);
    private static final MethodHandle CHANGED=changed();
    private static VarHandle field(String name,Class<?> type){try{return MethodHandles.privateLookupIn(EditBox.class,MethodHandles.lookup()).findVarHandle(EditBox.class,name,type);}catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}}
    private static MethodHandle changed(){try{return MethodHandles.privateLookupIn(EditBox.class,MethodHandles.lookup()).findVirtual(EditBox.class,"onValueChange",MethodType.methodType(void.class,String.class));}catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}}
    public Forge1192EditBox(Font font,int x,int y,int width,int height,Component text){super(font,x,y,width,height,text);}
    public Forge1192EditBox(Font font,int x,int y,int width,int height,EditBox old,Component text){super(font,x,y,width,height,old,text);}
    private void notifySource(String value){try{CHANGED.invokeExact((EditBox)this,value);}catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Native edit-box responder failed",failure);}}
    public void lms$setFilter(Predicate<String> filter){this.filter=filter;}
    public static void setFilter(EditBox box,Predicate<String> filter){require(box).lms$setFilter(filter);}
    public static Forge1192EditBox require(EditBox box){if(box instanceof Forge1192EditBox source)return source;throw new UnsupportedOperationException("Source edit-box operation on a native-only text field");}
    public static void setFormatter(EditBox box,BiFunction<String,Integer,FormattedCharSequence> formatter){require(box).lms$setFormatter(formatter);}
    public static void tick(EditBox box){require(box).lms$tick();}
    public static void setFocus(EditBox box,boolean value){require(box).lms$setFocus(value);}
    public static void moveCursor(EditBox box,int pos){require(box).lms$moveCursor(pos);}
    public static void moveCursorTo(EditBox box,int pos){require(box).lms$moveCursorTo(pos);}
    public static void moveCursorToStart(EditBox box){require(box).lms$moveCursorToStart();}
    public static void moveCursorToEnd(EditBox box){require(box).lms$moveCursorToEnd();}
    public static boolean keyPressed(EditBox box,int key,int scan,int mods){return require(box).lms$keyPressed(key,scan,mods);}
    public static boolean charTyped(EditBox box,char character,int mods){return require(box).lms$charTyped(character,mods);}
    public static boolean mouseClicked(EditBox box,double x,double y,int button){return require(box).lms$mouseClicked(x,y,button);}
    @Override public void setValue(String value){
        if(!initialized){super.setValue(value);return;}
        if(!filter.test(value))return;
        int limit=(int)LIMIT.get(this);VALUE.set(this,value.length()>limit?value.substring(0,limit):value);
        lms$moveCursorToEnd();setHighlightPos(getCursorPosition());notifySource(value);
    }
    @Override public void insertText(String input){
        int cursor=getCursorPosition(),highlight=(int)HIGHLIGHT.get(this),start=Math.min(cursor,highlight),end=Math.max(cursor,highlight);
        int remaining=(int)LIMIT.get(this)-getValue().length()-(start-end);
        String text=StringUtil.filterText(input);int count=Math.min(remaining,text.length());
        if(count<0)throw new IllegalStateException("Source edit-box cursor/limit invariant violated");
        if(count<text.length()&&count>0&&Character.isHighSurrogate(text.charAt(count-1)))count--;
        text=text.substring(0,count);String candidate=new StringBuilder(getValue()).replace(start,end,text).toString();
        if(!filter.test(candidate))return;
        VALUE.set(this,candidate);setCursorPosition(start+count);setHighlightPos(getCursorPosition());notifySource(candidate);
    }
    @Override public void deleteCharsToPos(int pos){
        String value=getValue();if(value.isEmpty())return;
        int cursor=getCursorPosition(),highlight=(int)HIGHLIGHT.get(this);
        if(cursor!=highlight){insertText("");return;}
        int start=Math.min(pos,cursor),end=Math.max(pos,cursor);if(start==end)return;
        String candidate=new StringBuilder(value).delete(start,end).toString();if(!filter.test(candidate))return;
        VALUE.set(this,candidate);lms$moveCursorTo(start);
    }
    @Override public void moveCursorTo(int pos,boolean extend){super.moveCursorTo(pos,extend);if(initialized)notifySource(getValue());}
    public void lms$moveCursorTo(int pos){moveCursorTo(pos,sourceShift);}
    public void lms$moveCursor(int delta){lms$moveCursorTo(net.minecraft.util.Util.offsetByCodepoints(getValue(),getCursorPosition(),delta));}
    public void lms$moveCursorToStart(){lms$moveCursorTo(0);}
    public void lms$moveCursorToEnd(){lms$moveCursorTo(getValue().length());}
    public void lms$setFormatter(BiFunction<String,Integer,FormattedCharSequence> formatter){
        this.formatter=formatter;
        if(!formatterInstalled){addFormatter((text,offset)->java.util.Objects.requireNonNull(this.formatter.apply(text,offset),"Source formatter returned no sequence"));formatterInstalled=true;}
    }
    /** Native blink phase uses Util.getMillis/focusedTime, not a frame counter. */
    public void lms$tick(){}
    public void lms$setFocus(boolean focus){setFocused(focus);}
    @Override public boolean keyPressed(KeyEvent event){
        var previous=activeKey;activeKey=event;
        try{return lms$keyPressed(Forge1192KeyboardBridge.sourceValue(InputConstants.Type.KEYBOARD.getOrCreate(event.key())),event.key(),Forge1192KeyboardBridge.sourceModifiers(event.modifiers()));}finally{activeKey=previous;}
    }
    public boolean lms$keyPressed(int key,int normalizedPhysical,int modifiers){
        sourceShift=(modifiers&1)!=0;
        var event=activeKey;
        if(event==null||Forge1192KeyboardBridge.sourceValue(InputConstants.Type.KEYBOARD.getOrCreate(event.key()))!=key||event.key()!=normalizedPhysical||Forge1192KeyboardBridge.sourceModifiers(event.modifiers())!=modifiers)
            {
            int physical=Forge1192KeyboardBridge.keyboardCode(key),nativeMods=Forge1192KeyboardBridge.nativeModifiers(modifiers);
            int logical=org.lwjgl.sdl.SDLKeyboard.SDL_GetKeyFromScancode(physical,(short)nativeMods,true);
            if(logical==org.lwjgl.sdl.SDLKeycode.SDLK_UNKNOWN)throw new UnsupportedOperationException("SDL key has no current logical keycode: "+key);
            event=new KeyEvent(physical,logical,nativeMods);
        }
        return super.keyPressed(event);
    }
    @Override public boolean charTyped(CharacterEvent event){
        if(!Character.isBmpCodePoint(event.codepoint()))throw new UnsupportedOperationException("Supplementary character cannot be represented by source char callback");
        return lms$charTyped((char)event.codepoint(),Forge1192KeyboardBridge.sourceModifiers(org.lwjgl.sdl.SDLKeyboard.SDL_GetModState()));
    }
    public boolean lms$charTyped(char character,int modifiers){return super.charTyped(new CharacterEvent(character));}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){
        var previous=activeMouse;activeMouse=event;
        try{return lms$mouseClicked(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(InputConstants.Type.MOUSE.getOrCreate(event.button())));}finally{activeMouse=previous;}
    }
    public boolean lms$mouseClicked(double x,double y,int button){
        var event=activeMouse;
        if(event==null||event.x()!=x||event.y()!=y||Forge1192KeyboardBridge.sourceValue(InputConstants.Type.MOUSE.getOrCreate(event.button()))!=button)
            event=new MouseButtonEvent(x,y,new MouseButtonInfo(Forge1192KeyboardBridge.mouseCode(button),0));
        return super.mouseClicked(event,false);
    }
}
