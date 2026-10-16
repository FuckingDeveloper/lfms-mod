package com.fuckingdeveloper.lms.target.neoforge263.entity;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;
import java.lang.invoke.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;

/** Source constructors/item defaults over native simulation; native item types keep their native ancestry. */
public abstract class Target263Forge1192ThrowableItemProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<ItemStack> ITEM=itemAccessor();
    private static final MethodHandle SAVE=parentData("addAdditionalSaveData",ValueOutput.class);
    private static final MethodHandle LOAD=parentData("readAdditionalSaveData",ValueInput.class);
    @SuppressWarnings("unchecked")
    private static EntityDataAccessor<ItemStack> itemAccessor() {
        try {return (EntityDataAccessor<ItemStack>)MethodHandles.privateLookupIn(ThrowableItemProjectile.class,MethodHandles.lookup())
                .findStaticVarHandle(ThrowableItemProjectile.class,"DATA_ITEM_STACK",EntityDataAccessor.class).get();}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static MethodHandle parentData(String name,Class<?> argument) {
        try {return MethodHandles.privateLookupIn(ThrowableProjectile.class,MethodHandles.lookup())
                .findSpecial(Projectile.class,name,MethodType.methodType(void.class,argument),ThrowableProjectile.class);}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    public Target263Forge1192ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type,Level level){super(type,level);}
    public Target263Forge1192ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type,double x,double y,double z,Level level){this(type,level);setPos(x,y,z);}
    public Target263Forge1192ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> type,LivingEntity owner,Level level){this(type,owner.getX(),owner.getEyeY()-0.1F,owner.getZ(),level);setOwner(owner);}

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(ITEM,ItemStack.EMPTY);}
    protected ItemStack getItemRaw(){return getEntityData().get(ITEM);}
    public static ItemStack raw(ThrowableItemProjectile entity){
        return entity instanceof Target263Forge1192ThrowableItemProjectile source?source.getItemRaw():entity.getEntityData().get(ITEM);
    }
    @Override public ItemStack getItem(){var raw=getItemRaw();return raw.isEmpty()?com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(getDefaultItem()):raw;}
    @Override public void setItem(ItemStack stack){
        if(!stack.is(getDefaultItem())||Forge1192ItemBridge.hasTag(stack))getEntityData().set(ITEM,stack.copyWithCount(1));
    }
    public static float gravity(ThrowableItemProjectile entity){
        if(entity instanceof Target263Forge1192ThrowableItemProjectile source)return source.lms$sourceGravity();
        throw new UnsupportedOperationException("Source float gravity query on native-only projectile has no source override contract");
    }
    protected float lms$sourceGravity(){return 0.03F;}
    @Override protected double getDefaultGravity(){return lms$sourceGravity();}

    @Override protected void addAdditionalSaveData(ValueOutput output){
        // Source ThrowableProjectile inherits Projectile's writer. Calling the new item writer would eagerly materialize the source fallback.
        try {SAVE.invokeExact((ThrowableProjectile)this,output);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native projectile parent save failed",failure);}
        var raw=getItemRaw();if(!raw.isEmpty())output.store("Item",ItemStack.CODEC,raw);
    }
    @Override protected void readAdditionalSaveData(ValueInput input){
        try {LOAD.invokeExact((ThrowableProjectile)this,input);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native projectile parent load failed",failure);}
        setItem(input.read("Item",ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }
}
