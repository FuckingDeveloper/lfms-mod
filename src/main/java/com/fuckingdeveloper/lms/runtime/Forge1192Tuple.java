package com.fuckingdeveloper.lms.runtime;

/**
 * Exact mutable value semantics of the removed Minecraft 1.19.2 Tuple.
 *
 * <p>The SRG entry points remain explicit because managed legacy bytecode uses
 * those names. Named accessors are provided for target-independent LMS code;
 * no target Minecraft class is claimed as a semantic replacement.</p>
 */
public final class Forge1192Tuple<A, B> {
    private A first;
    private B second;

    public Forge1192Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getA() {
        return first;
    }

    public void setA(A first) {
        this.first = first;
    }

    public B getB() {
        return second;
    }

    public void setB(B second) {
        this.second = second;
    }

    public A m_14418_() {
        return getA();
    }

    public B m_14419_() {
        return getB();
    }

    public void m_145023_(A first) {
        setA(first);
    }

    public void m_145025_(B second) {
        setB(second);
    }
}
