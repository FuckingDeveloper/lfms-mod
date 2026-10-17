package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyIngredient;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge;
import com.google.gson.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.*;

/** Native composition, display, static-data publication and exact native/source view lifetime. */
public final class Target263SourceIngredients {
    private Target263SourceIngredients() {}
    private static final java.util.concurrent.atomic.AtomicBoolean INSTALLED=new java.util.concurrent.atomic.AtomicBoolean();
    public static void install(){if(INSTALLED.compareAndSet(false,true))net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            net.neoforged.bus.api.EventPriority.HIGHEST,false,net.neoforged.neoforge.event.TagsUpdatedEvent.class,
            event->{if(event.shouldUpdateStaticData())LegacyIngredient.lms$nativeReload();});}
    public static final class State {
        private final long epoch;
        private final boolean snapshot;
        private final WeakReference<LegacyIngredient> original;
        private final List<ItemStack> stacks;
        private WeakReference<LegacyIngredient> view;
        private State(LegacyIngredient source,List<ItemStack> stacks,boolean snapshot){epoch=LegacyIngredient.lms$epoch();this.snapshot=snapshot;original=new WeakReference<>(source);view=new WeakReference<>(source);this.stacks=stacks;}
        public void validate(){
            if(!snapshot){if(view.get() instanceof NativeView nativeView)nativeView.validateArray();return;}
            if(epoch!=LegacyIngredient.lms$epoch())throw new UnsupportedOperationException("Source ingredient native snapshot crossed a data reload; rebuild its native view");
            var source=original.get();if(source==null){if(view.get() instanceof NativeView nativeView)nativeView.validateArray();return;}
            var current=source.lms$callback(source::getItems);
            if(current.length!=stacks.size())throw new UnsupportedOperationException("Source ingredient enumeration changed outside native reload");
            for(int i=0;i<current.length;i++)if(!ItemStack.matches(current[i],stacks.get(i)))throw new UnsupportedOperationException("Source ingredient stack data changed outside native reload");
        }
    }
    private static Target263SourceIngredientAccess access(Ingredient ingredient){return (Target263SourceIngredientAccess)(Object)ingredient;}
    public static LegacyIngredient wrap(Ingredient ingredient){
        Objects.requireNonNull(ingredient);
        synchronized(ingredient){
            var state=access(ingredient).lms$ingredientState();
            if(state!=null){state.validate();var source=state.view.get();if(source!=null)return source;}
            var source=new NativeView(ingredient);
            if(state==null){state=new State(null,List.of(),false);access(ingredient).lms$ingredientState(state);}
            state.view=new WeakReference<>(source);return source;
        }
    }
    public static Ingredient unwrap(LegacyIngredient source){
        Objects.requireNonNull(source);
        if(source instanceof NativeView nativeView)return nativeView.ingredient;
        if(source==LegacyIngredient.EMPTY)throw new UnsupportedOperationException("Source EMPTY requires a proven optional native ingredient slot");
        var nativeView=(Ingredient)source.lms$nativeView(()->create(source));
        access(nativeView).lms$ingredientState().validate();return nativeView;
    }
    public static Optional<Ingredient> optional(LegacyIngredient source){return source==LegacyIngredient.EMPTY?Optional.empty():Optional.of(unwrap(source));}
    private static Ingredient create(LegacyIngredient source){
        List<ItemStack> stacks=source.lms$callback(()->Arrays.stream(source.getItems()).map(ItemStack::copy).toList());
        var candidates=source.isVanilla()?source.lms$callback(()->source.lms$sourceValues().stream().flatMap(value->{
            if(value.getClass()==LegacyIngredient.TagValue.class)return realTagItems(((LegacyIngredient.TagValue)value).lms$tag()).stream();
            return value.getItems().stream();
        }).toList()):stacks;
        var items=candidates.stream().filter(stack->!stack.isEmpty()).map(ItemStack::typeHolder).distinct().toList();
        SlotDisplay display=new SlotDisplay.Composite(stacks.stream().map(stack->stack.isEmpty()?(SlotDisplay)SlotDisplay.Empty.INSTANCE:new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack))).toList());
        Ingredient result;
        if(source.isVanilla()&&!items.isEmpty())result=CustomDisplayIngredient.of(Ingredient.of(items.stream().map(Holder::value)),display);
        else result=new Ingredient(new SourceDelegate(source,items,display,source.isVanilla()));
        access(result).lms$ingredientState(new State(source,stacks,true));return result;
    }
    private record SourceDelegate(LegacyIngredient source,List<Holder<Item>> snapshot,SlotDisplay display,boolean nativeDefault) implements ICustomIngredient {
        public boolean test(ItemStack stack){return nativeDefault?stack!=null&&snapshot.stream().anyMatch(stack::is):source.lms$callback(()->source.test(stack));}
        public Stream<Holder<Item>> items(){return snapshot.stream();}
        public boolean isSimple(){return source.lms$callback(source::isSimple);}
        public IngredientType<?> getType(){throw new UnsupportedOperationException("Source custom ingredient callbacks require a separately proven native IngredientType codec");}
        @Override public boolean equals(Object other){return other instanceof SourceDelegate delegate&&source.lms$callback(()->source.equals(delegate.source));}
        @Override public int hashCode(){return source.lms$callback(source::hashCode);}
    }
    private static final class NativeView extends LegacyIngredient {
        private final Ingredient ingredient;
        private ItemStack[] array;
        private List<ItemStack> arraySnapshot;
        private long arrayEpoch;
        private NativeView(Ingredient ingredient){super(Stream.empty(),!ingredient.isCustom());this.ingredient=ingredient;}
        private void validateArray(){
            if(array==null||arrayEpoch!=LegacyIngredient.lms$epoch())return;
            for(int i=0;i<array.length;i++)if(!ItemStack.matches(array[i],arraySnapshot.get(i)))throw new UnsupportedOperationException("Native ingredient source stack view changed outside native reload");
        }
        @Override protected void invalidate(){super.invalidate();array=null;arraySnapshot=null;}
        @Override public ItemStack[] getItems(){
            checkInvalidation();validateArray();
            if(array==null){
                if(ingredient.isCustom())array=displayStacks(ingredient.display()).toArray(ItemStack[]::new);
                else array=ingredient.items().map(holder->Forge1192EarlyItemStackBridge.create(holder.value())).toArray(ItemStack[]::new);
                arraySnapshot=Arrays.stream(array).map(ItemStack::copy).toList();arrayEpoch=LegacyIngredient.lms$epoch();
            }
            return array;
        }
        @Override public boolean test(ItemStack stack){return stack!=null&&ingredient.test(stack);}
        @Override public boolean isSimple(){return ingredient.isSimple();}
        @Override public boolean isEmpty(){return ingredient.isEmpty();}
        @Override public JsonElement toJson(){
            if(ingredient.isCustom())throw new UnsupportedOperationException("Native custom ingredient JSON is not the source JSON contract");
            if(ingredient.getValues() instanceof HolderSet.Named<Item> named)return new LegacyIngredient.TagValue(named.key()).serialize();
            var stacks=getItems();return LegacyIngredient.of(stacks).toJson();
        }
    }
    private static List<ItemStack> displayStacks(SlotDisplay display){
        if(display instanceof SlotDisplay.Empty)return List.of(ItemStack.EMPTY);
        if(display instanceof SlotDisplay.Composite composite)return composite.contents().stream().flatMap(entry->displayStacks(entry).stream()).toList();
        if(display instanceof SlotDisplay.ItemStackSlotDisplay stack)return List.of(stack.stack().create());
        if(display instanceof SlotDisplay.ItemSlotDisplay item)return List.of(Forge1192EarlyItemStackBridge.create(item.item().value()));
        if(display instanceof SlotDisplay.TagSlotDisplay tag)return tag.tag().stream().map(holder->Forge1192EarlyItemStackBridge.create(holder.value())).toList();
        if(display instanceof SlotDisplay.WithRemainder remainder)return displayStacks(remainder.input());
        throw new UnsupportedOperationException("Native ingredient display has no proven source stack-array view: "+display.getClass().getName());
    }
    public static int stackingId(ItemStack stack){return BuiltInRegistries.ITEM.getId(stack.getItem());}
    private static List<ItemStack> realTagItems(TagKey<Item> tag){
        var values=new ArrayList<ItemStack>();for(var holder:BuiltInRegistries.ITEM.getTagOrEmpty(tag))values.add(Forge1192EarlyItemStackBridge.create(holder.value()));return values;
    }
    public static Collection<ItemStack> tagItems(TagKey<Item> tag){
        var values=realTagItems(tag);
        if(values.isEmpty()){
            var diagnostic=Forge1192EarlyItemStackBridge.create(Items.BARRIER);
            diagnostic.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Empty Tag: "+tag.location()));
            values.add(diagnostic);
        }
        return values;
    }
    public static JsonObject itemJson(ItemStack stack){var result=new JsonObject();result.addProperty("item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());return result;}
    public static LegacyIngredient.Value value(JsonObject json){
        if(json.has("type"))throw new UnsupportedOperationException("Source custom ingredient JSON serializers require a separate adapter");
        if(json.has("item")==json.has("tag"))throw new JsonSyntaxException("Source ingredient requires exactly one item or tag");
        if(json.has("tag"))return new LegacyIngredient.TagValue(Target263CommonTagProjection.item(Identifier.parse(json.get("tag").getAsString())));
        Identifier id=Identifier.parse(json.get("item").getAsString());if(!BuiltInRegistries.ITEM.containsKey(id))throw new JsonSyntaxException("Unknown source ingredient item: "+id);
                Item item=BuiltInRegistries.ITEM.getValue(id);if(item==Items.AIR)throw new JsonSyntaxException("Invalid source ingredient item: "+id);
        return new LegacyIngredient.ItemValue(Forge1192EarlyItemStackBridge.create(item));
    }
}
