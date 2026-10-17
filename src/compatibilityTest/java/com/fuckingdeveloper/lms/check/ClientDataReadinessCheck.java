package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.event.ClientDataReadiness;

public final class ClientDataReadinessCheck {
    public static void run() {
        var gate=new ClientDataReadiness<Object,String>();var a=new Object();var b=new Object();
        gate.configuration(a,"a1");check(gate.hasPending(),"configuration not retained");
        check(gate.loginCompleted(b).isEmpty(),"unrelated login consumed pending data");
        check(gate.loginCompleted(a).orElseThrow().equals("a1")&&!gate.hasPending(),"ready publication not released once");
        check(gate.loginCompleted(a).isEmpty(),"duplicate login repeated publication");
        check(gate.publication(a,"a2").orElseThrow().equals("a2"),"ready reload was deferred");
        gate.configuration(a,"a3");check(gate.hasPending(),"same-connection reconfiguration not gated");
        gate.configuration(a,"a4");check(gate.loginCompleted(a).orElseThrow().equals("a4"),"obsolete configuration snapshot reused");
        gate.configuration(a,"old");gate.configuration(b,"new");check(gate.publication(a,"stale").isEmpty(),"stale publication reclaimed a replaced connection");gate.configuration(a,"stale-config");gate.disconnected(a);
        check(gate.loginCompleted(a).isEmpty()&&gate.loginCompleted(b).orElseThrow().equals("new"),"connection replacement/disconnect lost ownership");
        gate.configuration(b,"discard");gate.disconnected(b);check(!gate.hasPending()&&gate.loginCompleted(b).isEmpty(),"disconnected state retained");
        var newKey=new Object();
        check(gate.publication(newKey,"early").isEmpty()&&gate.loginCompleted(newKey).orElseThrow().equals("early"),"early publication bypassed readiness");
        System.out.println("PASS: typed connection publication gate, post-login once-only delivery, reload/reconfiguration, replacement and disconnect cleanup; no callback executes under its state lock");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
