package com.fuckingdeveloper.lms.runtime.render;

import org.joml.Matrix4f;
import org.joml.Matrix3x2f;

/** Bounded parallel GUI planes; never flattens arbitrary 3D or descending depth. */
public final class Forge1192GuiPlaneBridge {
    private Forge1192GuiPlaneBridge() {}
    public static Matrix3x2f planar(Matrix4f matrix) {
        if(!matrix.isFinite() || matrix.m02()!=0 || matrix.m12()!=0 || matrix.m20()!=0 || matrix.m21()!=0
                || matrix.m03()!=0 || matrix.m13()!=0 || matrix.m23()!=0 || matrix.m22()!=1 || matrix.m33()!=1)
            throw new UnsupportedOperationException("Source GUI transform is not an XY plane with independent Z translation");
        return new Matrix3x2f(matrix.m00(),matrix.m01(),matrix.m10(),matrix.m11(),matrix.m30(),matrix.m31());
    }
    public static void submit(float depth) {
        var scope=Forge1192TooltipGuiBridge.scope();
        if(scope.planes.advance(depth,scope.depthTest))scope.graphics.nextStratum();
    }
    public static final class Order {
        private float enabledPlane;
        public boolean advance(float depth,boolean depthTest) {
            if(!Float.isFinite(depth)||depth<0||depth>=1000)throw new UnsupportedOperationException("Source GUI plane is outside the proven visible interval: "+depth);
            if(!depthTest)return false;
            if(depth<enabledPlane)throw new UnsupportedOperationException("Descending source GUI planes require depth-buffer occlusion: "+enabledPlane+" -> "+depth);
            boolean next=depth>enabledPlane;enabledPlane=depth;return next;
        }
    }
}
