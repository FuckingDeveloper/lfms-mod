package com.fuckingdeveloper.lms.profile.forge1192.runtime.energy;

/**
 * Structural contract for Forge Energy implementations compiled against
 * Forge 1.19.2. Modern transactional EnergyHandler interop is intentionally a
 * separate capability boundary; its semantics cannot be expressed by a type
 * rename alone.
 */
public interface LegacyIEnergyStorage {
    int receiveEnergy(int maxReceive, boolean simulate);
    int extractEnergy(int maxExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
