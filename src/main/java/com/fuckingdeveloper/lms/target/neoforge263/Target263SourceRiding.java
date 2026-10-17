package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Native riding policy with explicit source scalar and position callbacks. */
public final class Target263SourceRiding {
    private Target263SourceRiding() {}
    public interface PassengerOffset { double lms$passengerOffset(); }
    public interface RiderOffset { double lms$riderOffset(); }
    public interface Position { void lms$positionRider(Entity passenger); }
    private record Movement(Entity vehicle, Entity passenger, Entity.MoveFunction function) {}
    private static final ThreadLocal<Movement> MOVEMENT = new ThreadLocal<>();
    public static boolean startRiding(Entity rider, Entity vehicle) { return rider.startRiding(vehicle, false, true); }
    public static boolean startRiding(Entity rider, Entity vehicle, boolean force) { return rider.startRiding(vehicle, force, true); }
    public static Vec3 passengerAttachment(Vec3 nativePoint, double sourceOffset) { return new Vec3(nativePoint.x, sourceOffset, nativePoint.z); }
    public static Vec3 riderAttachment(Vec3 nativePoint, double sourceOffset) { return new Vec3(nativePoint.x, -sourceOffset, nativePoint.z); }
    public static double passengerOffset(Entity vehicle) {
        if (vehicle instanceof PassengerOffset callback) return callback.lms$passengerOffset();
        throw new UnsupportedOperationException("Scalar passenger-offset read on a native attachment-only vehicle: " + vehicle.getClass().getName());
    }
    public static double riderOffset(Entity rider) {
        if (rider instanceof RiderOffset callback) return callback.lms$riderOffset();
        throw new UnsupportedOperationException("Scalar rider-offset read on a native attachment-only rider: " + rider.getClass().getName());
    }
    public static double basePassengerOffset(Entity vehicle) { return vehicle.getBbHeight() * 0.75D; }
    public static double baseRiderOffset(Entity rider) { return 0D; }
    public static void positionRider(Entity vehicle, Entity passenger, Entity.MoveFunction move) {
        var previous = MOVEMENT.get();
        MOVEMENT.set(new Movement(vehicle, passenger, move));
        try { ((Position)vehicle).lms$positionRider(passenger); }
        finally { if (previous == null) MOVEMENT.remove(); else MOVEMENT.set(previous); }
    }
    /** Exact source Entity superclass operation, using native attachment and movement rules. */
    public static void basePositionRider(Entity vehicle, Entity passenger) {
        if (!vehicle.hasPassenger(passenger)) return;
        var movement = MOVEMENT.get();
        Entity.MoveFunction move = movement != null && movement.vehicle == vehicle && movement.passenger == passenger
                ? movement.function : Entity::setPos;
        Vec3 position = vehicle.getPassengerRidingPosition(passenger).subtract(passenger.getVehicleAttachmentPoint(vehicle));
        move.accept(passenger, position.x, position.y, position.z);
    }
}
