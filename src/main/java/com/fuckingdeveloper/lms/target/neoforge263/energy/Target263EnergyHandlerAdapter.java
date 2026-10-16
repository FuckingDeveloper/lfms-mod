package com.fuckingdeveloper.lms.target.neoforge263.energy;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.energy.LegacyIEnergyStorage;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Source imperative energy operations over the authoritative native transactional provider. */
public final class Target263EnergyHandlerAdapter implements LegacyIEnergyStorage {
    private final EnergyHandler handler;
    private final BooleanSupplier valid;
    public Target263EnergyHandlerAdapter(EnergyHandler handler, BooleanSupplier valid) {
        this.handler=Objects.requireNonNull(handler);this.valid=Objects.requireNonNull(valid);
    }
    private void checkValid() {
        if(!valid.getAsBoolean())throw new IllegalStateException("Legacy target energy capability was invalidated");
    }
    @Override public int receiveEnergy(int maximum,boolean simulate){return transfer(maximum,simulate,true);}
    @Override public int extractEnergy(int maximum,boolean simulate){return transfer(maximum,simulate,false);}
    private int transfer(int maximum,boolean simulate,boolean insert) {
        checkValid();
        if(maximum<0)throw new IllegalArgumentException("Negative source energy transfer request");
        try(var transaction=Transaction.openRoot()) {
            int amount=insert?handler.insert(maximum,transaction):handler.extract(maximum,transaction);
            if(amount<0||amount>maximum)throw new IllegalStateException("Invalid native energy transfer amount: "+amount+" / "+maximum);
            if(!simulate)transaction.commit();
            return amount;
        }
    }
    @Override public int getEnergyStored(){checkValid();return sourceAmount(handler.getAmountAsLong());}
    @Override public int getMaxEnergyStored(){checkValid();return sourceAmount(handler.getCapacityAsLong());}
    private static int sourceAmount(long amount) {
        if(amount<0)throw new IllegalStateException("Native energy amount/capacity is negative: "+amount);
        return Math.toIntExact(amount);
    }
    @Override public boolean canReceive(){checkValid();throw directionFlag("canReceive");}
    @Override public boolean canExtract(){checkValid();throw directionFlag("canExtract");}
    private static UnsupportedOperationException directionFlag(String operation) {
        return new UnsupportedOperationException("Native EnergyHandler does not expose source direction flag "+operation);
    }
}
