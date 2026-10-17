package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import java.util.*;
import org.objectweb.asm.*;

/** Pinned final source enum identity; native named TextColor and formatting codes. */
public final class Forge1192ChatFormattingRules {
    public static final String SOURCE = "net/minecraft/ChatFormatting";
    private static final String TYPE = "L" + SOURCE + ";";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/chat/Forge1192ChatFormattingBridge";
    private Forge1192ChatFormattingRules() {}
    public static List<MethodRuleCatalog.Rule> entries() {
        var result=new ArrayList<MethodRuleCatalog.Rule>();
        add(result,"getChar","m_178510_","getChar","()C",false);
        add(result,"getId","m_126656_","getId","()I",false);
        add(result,"isFormat","m_126661_","isFormat","()Z",false);
        add(result,"isColor","m_126664_","isColor","()Z",false);
        add(result,"getColor","m_126665_","getColor","()Ljava/lang/Integer;",false);
        add(result,"getName","m_126666_","getName","()Ljava/lang/String;",false);
        add(result,"getSerializedName","m_7912_","getName","()Ljava/lang/String;",false);
        add(result,"getByName","m_126657_","byName","(Ljava/lang/String;)"+TYPE,true);
        add(result,"getById","m_126647_","byId","(I)"+TYPE,true);
        add(result,"getByCode","m_126645_","byCode","(C)"+TYPE,true);
        add(result,"stripFormatting","m_126649_","stripFormatting","(Ljava/lang/String;)Ljava/lang/String;",true);
        add(result,"getNames","m_126653_","getNames","(ZZ)Ljava/util/Collection;",true);
        return List.copyOf(result);
    }
    private static void add(List<MethodRuleCatalog.Rule> rules,String named,String srg,String target,String descriptor,boolean isStatic) {
        for(String name:List.of(named,srg)) rules.add(new MethodRuleCatalog.Rule("chat-formatting-"+name,"CHAT_FORMATTING",
                new MethodRuleCatalog.Member(SOURCE,name,descriptor),isStatic?Opcodes.INVOKESTATIC:Opcodes.INVOKEVIRTUAL,
                new Handle(Opcodes.H_INVOKESTATIC,BRIDGE,target,isStatic?descriptor:"("+TYPE+descriptor.substring(1),false),
                "Pinned Forge43.4.0 named ChatFormatting bytecode/client.txt/joined.tsrg; native26.3 enum codes and TextColor named RGB mapping"));
    }
}
