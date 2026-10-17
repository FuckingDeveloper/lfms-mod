package com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Stream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;

/** Source inheritance/query identity; native recipe consumers require an explicit target view. */
public class LegacyIngredient implements Predicate<ItemStack> {
    private static final AtomicLong EPOCH=new AtomicLong();
    public static final LegacyIngredient EMPTY=new LegacyIngredient(Stream.empty());
    private static final ThreadLocal<Boolean> INVALIDATING=ThreadLocal.withInitial(()->false);
    private final Value[] values;
    private ItemStack[] stacks;
    private IntList stackingIds;
    private long observedEpoch;
    private long nativeEpoch=-1;
    private Object nativeView;
    private final boolean vanilla;
    private final String callbackOwner;
    protected LegacyIngredient(Stream<? extends Value> values){this(values,null);}
    protected LegacyIngredient(Stream<? extends Value> values,Boolean nativeVanilla){
        this.values=values.toArray(Value[]::new);vanilla=nativeVanilla!=null?nativeVanilla:getClass()==LegacyIngredient.class;
        observedEpoch=lms$epoch();callbackOwner=nativeVanilla!=null||this.values.length==0&&getClass()==LegacyIngredient.class?null:Forge1192LifecycleBridge.currentContext().map(Forge1192LifecycleBridge.Context::modId).orElse(null);
    }
    public final List<Value> lms$sourceValues(){return Arrays.asList(values.clone());}
    public static long lms$epoch(){return EPOCH.get();}
    /** Called only by the installed native tag/reload publication hook. */
    public static void lms$nativeReload(){EPOCH.incrementAndGet();}
    public final <T> T lms$callback(Supplier<T> action){
        if(callbackOwner==null)return action.get();
        try(var scope=Forge1192LifecycleBridge.enterCallback(callbackOwner)){return action.get();}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Exception failure){throw new IllegalStateException("Cannot close source ingredient callback scope",failure);}
    }
    public final synchronized Object lms$nativeView(Supplier<Object> factory){
        long epoch=lms$epoch();if(nativeView==null||nativeEpoch!=epoch){nativeView=factory.get();nativeEpoch=epoch;}return nativeView;
    }
    public ItemStack[] getItems(){
        checkInvalidation();
        if(stacks==null)stacks=Arrays.stream(values).flatMap(value->value.getItems().stream()).distinct().toArray(ItemStack[]::new);
        return stacks;
    }
    @Override public boolean test(ItemStack stack){
        if(stack==null)return false;var items=getItems();
        if(items.length==0)return stack.isEmpty();
        for(var accepted:items)if(accepted.is(stack.getItem()))return true;return false;
    }
    public IntList getStackingIds(){
        checkInvalidation();if(stackingIds==null){var ids=new IntArrayList();for(var stack:getItems())ids.add(InstalledTargetAdapters.sourceIngredientStackingId(stack));ids.sort(IntComparators.NATURAL_COMPARATOR);stackingIds=ids;}return stackingIds;
    }
    public boolean isEmpty(){return values.length==0&&(stacks==null||stacks.length==0)&&(stackingIds==null||stackingIds.isEmpty());}
    public boolean isSimple(){return true;}
    public final boolean isVanilla(){return vanilla;}
    public static void invalidateAll(){throw new UnsupportedOperationException("Source ingredient cache invalidation outside native reload is not adapted");}
    public final boolean checkInvalidation(){
        long next=lms$epoch();if(observedEpoch==next)return false;
        long previous=observedEpoch;observedEpoch=next;boolean scope=INVALIDATING.get();INVALIDATING.set(true);
        try{lms$callback(()->{invalidate();return null;});return true;}
        catch(RuntimeException|Error failure){observedEpoch=previous;throw failure;}
        finally{if(scope)INVALIDATING.set(true);else INVALIDATING.remove();}
    }
    protected final void markValid(){observedEpoch=lms$epoch();}
    protected void invalidate(){requireInvalidationScope();stacks=null;stackingIds=null;nativeView=null;}
    public static void lms$guardedInvalidate(LegacyIngredient ingredient){requireInvalidationScope();ingredient.invalidate();}
    private static void requireInvalidationScope(){if(!INVALIDATING.get())throw new UnsupportedOperationException("Source ingredient invalidation requires the native reload lifecycle");}
    public final void toNetwork(FriendlyByteBuf buffer){throw new UnsupportedOperationException("Source ingredient wire serialization requires a separate codec adapter");}
    public static LegacyIngredient fromNetwork(FriendlyByteBuf buffer){throw new UnsupportedOperationException("Source ingredient wire decoding requires a separate codec adapter");}
    public LegacyIngredientSerializer<? extends LegacyIngredient> getSerializer(){throw new UnsupportedOperationException("Source ingredient serializer identity is not a native IngredientType codec");}
    public JsonElement toJson(){if(values.length==1)return values[0].serialize();var array=new JsonArray();for(var value:values)array.add(value.serialize());return array;}
    public static LegacyIngredient fromValues(Stream<? extends Value> values){var ingredient=new LegacyIngredient(values);return ingredient.values.length==0?EMPTY:ingredient;}
    public static LegacyIngredient of(){return EMPTY;}
    public static LegacyIngredient of(ItemLike... items){return of(Arrays.stream(items).map(item->Forge1192EarlyItemStackBridge.create(item.asItem())));}
    public static LegacyIngredient of(ItemStack... stacks){return of(Arrays.stream(stacks));}
    public static LegacyIngredient of(Stream<ItemStack> stacks){return fromValues(stacks.filter(stack->!stack.isEmpty()).map(ItemValue::new));}
    public static LegacyIngredient of(TagKey<Item> tag){return fromValues(Stream.of(new TagValue(tag)));}
    public static LegacyIngredient merge(Collection<LegacyIngredient> ingredients){
        return fromValues(ingredients.stream().flatMap(i->Arrays.stream(i.values)));
    }
    public static LegacyIngredient fromJson(JsonElement json){
        if(json==null||json.isJsonNull())throw new JsonSyntaxException("Ingredient cannot be null");
        if(json.isJsonObject())return fromValues(Stream.of(valueFromJson(json.getAsJsonObject())));
        if(json.isJsonArray()){if(json.getAsJsonArray().isEmpty())throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");var values=new ArrayList<Value>();for(var entry:json.getAsJsonArray())values.add(valueFromJson(entry.getAsJsonObject()));return fromValues(values.stream());}
        throw new JsonSyntaxException("Expected source ingredient object or array");
    }
    public static Value valueFromJson(JsonObject json){return InstalledTargetAdapters.sourceIngredientValue(json);}
    public interface Value { Collection<ItemStack> getItems(); JsonObject serialize(); }
    public static class ItemValue implements Value {
        private final ItemStack item;
        public ItemValue(ItemStack item){this.item=item;}
        public Collection<ItemStack> getItems(){return Collections.singleton(item);}
        public JsonObject serialize(){return InstalledTargetAdapters.sourceIngredientItemJson(item);}
    }
    public static class TagValue implements Value {
        private final TagKey<Item> tag;
        public TagValue(TagKey<Item> tag){this.tag=tag;}
        public TagKey<Item> lms$tag(){return tag;}
        public Collection<ItemStack> getItems(){return InstalledTargetAdapters.sourceIngredientTagItems(tag);}
        public JsonObject serialize(){var json=new JsonObject();json.addProperty("tag",tag.location().toString());return json;}
    }
}
