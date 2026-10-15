package com.fuckingdeveloper.lms.runtime;

import com.mojang.serialization.DynamicLike;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.*;

/** Source inspection/mutation views over actual target-owned rule definitions and values. */
public final class Forge1192GameRuleViews {
    private Forge1192GameRuleViews() {}
    private static final Map<GameRule<?>,Key<?>> KEYS=new IdentityHashMap<>();
    private static final Map<GameRules,Map<Key<?>,java.lang.ref.WeakReference<Value<?>>>> VALUES=new WeakHashMap<>();
    public enum Category {
        PLAYER,MOBS,SPAWNING,DROPS,UPDATES,CHAT,MISC;
        public String getDescriptionId(){return "gamerule.category.minecraft."+name().toLowerCase(Locale.ROOT);}
    }
    public static final class Key<T extends Value<T>> {
        private final GameRule<?> rule;
        private Key(GameRule<?> rule){this.rule=Objects.requireNonNull(rule);}
        public Key(String id,Category category) {
            this(requireDefinition(id));
            if(getCategory()!=category)throw new IllegalArgumentException("Source rule category does not match native definition: "+id);
        }
        public String getId(){return rule.id();}
        public String getDescriptionId(){return rule.getDescriptionId();}
        public Category getCategory(){
            var id=rule.category().id();
            if(!id.getNamespace().equals("minecraft"))throw new UnsupportedOperationException("Native rule category has no source enum representation: "+id);
            try{return Category.valueOf(id.getPath().toUpperCase(Locale.ROOT));}
            catch(IllegalArgumentException failure){throw new UnsupportedOperationException("Native rule category has no source enum representation: "+id,failure);}
        }
        @Override public String toString(){return getId();}
        @Override public boolean equals(Object other){return other instanceof Key<?> key&&key.rule==rule;}
        @Override public int hashCode(){return System.identityHashCode(rule);}
    }
    public static final class Type<T extends Value<T>> {
        private final Key<T> key;
        private Type(Key<T> key){this.key=key;}
        @SuppressWarnings("unchecked") public T createRule(){return (T)getRule(newRules(),key);}
        public void callVisitor(Visitor visitor,Key<T> key){typedVisit(visitor,key,this);}
    }
    public interface Visitor {
        default <T extends Value<T>> void visit(Key<T> key,Type<T> type){}
        default void visitBoolean(Key<BooleanValue> key,Type<BooleanValue> type){}
        default void visitInteger(Key<IntegerValue> key,Type<IntegerValue> type){}
    }
    public abstract static class Value<T extends Value<T>> {
        protected final GameRules owner;
        protected final Key<T> key;
        private Value(GameRules owner,Key<T> key){this.owner=owner;this.key=key;}
        @SuppressWarnings("unchecked") public String serialize(){return ((GameRule<Object>)key.rule).serialize(owner.get((GameRule<Object>)key.rule));}
        @Override public String toString(){return serialize();}
        @SuppressWarnings("unchecked") public int getCommandResult(){return ((GameRule<Object>)key.rule).getCommandResult(owner.get((GameRule<Object>)key.rule));}
    }
    public static final class BooleanValue extends Value<BooleanValue> {
        private BooleanValue(GameRules owner,Key<BooleanValue> key){super(owner,key);}
        @SuppressWarnings("unchecked") public boolean get(){return owner.get((GameRule<Boolean>)key.rule);}
        @SuppressWarnings("unchecked") public void set(boolean value,MinecraftServer server){owner.set((GameRule<Boolean>)key.rule,value,server);}
        public void setFrom(BooleanValue value,MinecraftServer server){set(value.get(),server);}
    }
    public static final class IntegerValue extends Value<IntegerValue> {
        private IntegerValue(GameRules owner,Key<IntegerValue> key){super(owner,key);}
        @SuppressWarnings("unchecked") public int get(){return owner.get((GameRule<Integer>)key.rule);}
        @SuppressWarnings("unchecked") public void set(int value,MinecraftServer server){
            GameRule<Integer> rule=(GameRule<Integer>)key.rule;
            rule.valueCodec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE,value).getOrThrow();
            // encode codecs need not validate: deserialize uses the native argument constraints as well.
            int validated=rule.deserialize(Integer.toString(value)).getOrThrow();owner.set(rule,validated,server);
        }
        public void setFrom(IntegerValue value,MinecraftServer server){set(value.get(),server);}
    }
    private static GameRule<?> requireDefinition(String id){
        var key=Identifier.parse(id);
        if(!BuiltInRegistries.GAME_RULE.containsKey(key))throw new UnsupportedOperationException("Unknown source snapshot rule identity: "+id);
        return Objects.requireNonNull(BuiltInRegistries.GAME_RULE.getValue(key));
    }
    @SuppressWarnings({"unchecked","rawtypes"}) private static synchronized Key<?> key(GameRule<?> rule){return KEYS.computeIfAbsent(rule,Key::new);}
    public static GameRules newRules(){return new GameRules(FeatureFlags.VANILLA_SET);}
    public static GameRules copy(GameRules rules){var copy=new GameRules(rules.availableRules().toList());copy.setAll(rules,null);return copy;}
    public static void assignFrom(GameRules rules,GameRules other,MinecraftServer server){rules.setAll(other,server);}
    @SuppressWarnings({"unchecked","rawtypes"}) public static synchronized <T extends Value<T>> T getRule(GameRules rules,Key<T> key){
        Objects.requireNonNull(rules);Objects.requireNonNull(key);rules.get((GameRule)key.rule);
        var values=VALUES.computeIfAbsent(rules,ignored->new HashMap<>());
        var previous=values.get(key);var value=previous==null?null:previous.get();
        if(value!=null)return (T)value;
        if(key.rule.valueClass()==Boolean.class)value=new BooleanValue(rules,(Key)key);
        else if(key.rule.valueClass()==Integer.class)value=new IntegerValue(rules,(Key)key);
        else throw new UnsupportedOperationException("Native rule value has no source view: "+key.rule);
        values.put(key,new java.lang.ref.WeakReference<>(value));return (T)value;
    }
    public static boolean getBoolean(GameRules rules,Key<BooleanValue> key){return Forge1192GameRuleViews.<BooleanValue>getRule(rules,key).get();}
    public static int getInt(GameRules rules,Key<IntegerValue> key){return Forge1192GameRuleViews.<IntegerValue>getRule(rules,key).get();}
    public static void visitGameRuleTypes(Visitor visitor){
        newRules().availableRules().sorted(Comparator.comparing(rule->rule.getIdentifier().toString())).forEach(rule->visitRule(visitor,key(rule)));
    }
    @SuppressWarnings({"unchecked","rawtypes"}) private static void visitRule(Visitor visitor,Key<?> key){var type=new Type(key);visitor.visit((Key)key,type);typedVisit(visitor,key,type);}
    @SuppressWarnings({"unchecked","rawtypes"}) private static void typedVisit(Visitor visitor,Key<?> key,Type<?> type){
        if(key.rule.valueClass()==Boolean.class)visitor.visitBoolean((Key)key,(Type)type);
        else if(key.rule.valueClass()==Integer.class)visitor.visitInteger((Key)key,(Type)type);
        else throw new UnsupportedOperationException("Native rule visitor type has no source contract: "+key.rule);
    }
    @SuppressWarnings({"unchecked","rawtypes"}) public static CompoundTag createTag(GameRules rules){
        var tag=new CompoundTag();rules.availableRules().forEach(rule->tag.putString(rule.getIdentifier().toString(),((GameRule)rule).serialize(rules.get(rule))));return tag;
    }
    @SuppressWarnings({"unchecked","rawtypes"}) public static GameRuleMap mapFromDynamic(DynamicLike<?> input){
        var map=GameRuleMap.of();
        input.asMapOpt().getOrThrow().forEach(pair->{
            GameRule rule=requireDefinition(pair.getFirst().asString().getOrThrow());String text=pair.getSecond().asString().getOrThrow();
            if(!rule.isEnabled(FeatureFlags.VANILLA_SET))throw new UnsupportedOperationException("Source default snapshot contains feature-specific rule: "+rule);
            map.set(rule,rule.deserialize(text).getOrThrow());
        });return map;
    }
    public static GameRules fromDynamic(DynamicLike<?> input){return new GameRules(FeatureFlags.VANILLA_SET,mapFromDynamic(input));}
}
