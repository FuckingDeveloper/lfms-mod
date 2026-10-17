package com.fuckingdeveloper.lms.runtime.event;

import java.util.Objects;
import java.util.Optional;

/** One connection's latest configuration publication; callbacks execute outside this state lock. */
public final class ClientDataReadiness<K,V> {
    private K connection;
    private V pending;
    private boolean ready;
    private final java.lang.ref.ReferenceQueue<K> retiredQueue=new java.lang.ref.ReferenceQueue<>();
    private final java.util.Set<IdentityReference<K>> retired=new java.util.HashSet<>();
    private static final class IdentityReference<K> extends java.lang.ref.WeakReference<K> {
        private final int hash;
        IdentityReference(K key,java.lang.ref.ReferenceQueue<K> queue){super(key,queue);hash=System.identityHashCode(key);}
        @Override public int hashCode(){return hash;}
        @Override public boolean equals(Object other){return this==other||other instanceof IdentityReference<?> reference&&get()!=null&&get()==reference.get();}
    }
    private void prune(){java.lang.ref.Reference<? extends K> reference;while((reference=retiredQueue.poll())!=null)retired.remove(reference);}
    private boolean isRetired(K key){prune();return retired.contains(new IdentityReference<>(key,null));}
    private void retire(K key){if(key!=null)retired.add(new IdentityReference<>(key,retiredQueue));}
    private boolean select(K key){
        Objects.requireNonNull(key);
        if(isRetired(key))return false;
        if(connection!=key){retire(connection);connection=key;ready=false;}
        return true;
    }
    public synchronized void configuration(K connection,V publication) {
        Objects.requireNonNull(publication);
        if(select(connection)){pending=publication;ready=false;}
    }
    public synchronized Optional<V> publication(K connection,V publication) {
        Objects.requireNonNull(connection);Objects.requireNonNull(publication);
        if(!select(connection))return Optional.empty();
        if(ready)return Optional.of(publication);
        pending=publication;return Optional.empty();
    }
    public synchronized Optional<V> loginCompleted(K connection) {
        if(this.connection!=Objects.requireNonNull(connection))return Optional.empty();
        ready=true;V value=pending;pending=null;return Optional.ofNullable(value);
    }
    public synchronized void disconnected(K connection) {
        Objects.requireNonNull(connection);prune();retire(connection);
        if(this.connection==connection){this.connection=null;pending=null;ready=false;}
    }
    public synchronized boolean hasPending(){return pending!=null;}
}
