package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.energy.Target263EnergyHandlerAdapter;
import java.util.concurrent.atomic.AtomicBoolean;
import net.neoforged.neoforge.transfer.energy.*;
import net.neoforged.neoforge.transfer.transaction.*;

final class NativeEnergyOperationsCheck {
    static void run() {
        var valid=new AtomicBoolean(true);var commits=new int[1];
        var nativeHandler=new SimpleEnergyHandler(100,30,20,40){@Override protected void onEnergyChanged(int previous){commits[0]++;}};
        var source=new Target263EnergyHandlerAdapter(nativeHandler,valid::get);
        check(source.getEnergyStored()==40&&source.getMaxEnergyStored()==100,"amount/capacity");
        check(source.receiveEnergy(80,true)==30&&source.getEnergyStored()==40&&commits[0]==0,"insertion simulation leaked");
        check(source.receiveEnergy(80,false)==30&&source.getEnergyStored()==70&&commits[0]==1,"insertion commit/count");
        check(source.extractEnergy(80,true)==20&&source.getEnergyStored()==70&&commits[0]==1,"extraction simulation leaked");
        check(source.extractEnergy(80,false)==20&&source.getEnergyStored()==50&&commits[0]==2,"extraction commit/count");
        check(source.receiveEnergy(0,false)==0&&source.extractEnergy(0,true)==0&&commits[0]==2,"zero transfer changes");
        try{source.extractEnergy(-1,false);throw new AssertionError("negative request accepted");}catch(IllegalArgumentException expected){}
        for(int level:new int[]{0,100}) {
            nativeHandler.set(level);
            for(Runnable operation:new Runnable[]{source::canReceive,source::canExtract}) {
                try{operation.run();throw new AssertionError("direction flag inferred from amount");}catch(UnsupportedOperationException expected){}
            }
        }
        try(var parent=Transaction.openRoot()) {
            try{source.receiveEnergy(1,false);throw new AssertionError("nested root accepted");}catch(IllegalStateException expected){}
        }
        valid.set(false);
        for(Runnable operation:new Runnable[]{source::getEnergyStored,source::getMaxEnergyStored,source::canReceive,source::canExtract,
                ()->source.receiveEnergy(1,false),()->source.extractEnergy(1,true)}) {
            try{operation.run();throw new AssertionError("invalidated view remained usable");}catch(IllegalStateException expected){}
        }
        var oversized=new Target263EnergyHandlerAdapter(new SimpleEnergyHandler(1){public long getAmountAsLong(){return 1L+Integer.MAX_VALUE;}public long getCapacityAsLong(){return 1L+Integer.MAX_VALUE;}},()->true);
        for(Runnable operation:new Runnable[]{oversized::getEnergyStored,oversized::getMaxEnergyStored}) {
            try{operation.run();throw new AssertionError("oversized source integer silently truncated");}catch(ArithmeticException expected){}
        }
        var faultyNative=new SimpleEnergyHandler(100,100,100,20){@Override public int insert(int amount,TransactionContext transaction){super.insert(amount,transaction);return amount+1;}};
        var faulty=new Target263EnergyHandlerAdapter(faultyNative,()->true);
        try{faulty.receiveEnergy(10,false);throw new AssertionError("invalid native result committed");}catch(IllegalStateException expected){}
        check(faultyNative.getAmountAsLong()==20,"invalid provider transaction failed rollback");
        System.out.println("PASS: native energy actual transactional insert/extract, simulation rollback, commit callbacks, limits, invalidation, int overflow, bad-provider rollback and explicit flag/nested boundaries");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
