package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.InstrumentedClassTransformer;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.*;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192Profile;
import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.registry.StagedRegistrations;
import com.fuckingdeveloper.lms.transform.TransformationPipeline;
import com.fuckingdeveloper.lms.transform.TransformationPipeline.Pass;
import com.fuckingdeveloper.lms.transform.TransformationPipeline.Phase;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.tree.ClassNode;

/** Independent composition/registration rules, without native loader startup or mod identity. */
final class CompositionContractCheck {
    static void run() throws Exception {
        var profile = new Forge1192Profile();
        var installed = InstalledSourceProfiles.create();
        var descriptor = new LegacyModDescriptor(Path.of("fixture.jar"),"fixture","FORGE_METADATA","[1.19.2,1.20)","test");
        check(installed.resolve(descriptor).orElseThrow().id().equals(profile.id()),"installed selector lost source evidence");
        check(installed.resolve(new LegacyModDescriptor(descriptor.file(),"fixture","FORGE_METADATA","1.19.4","test")).isEmpty(),"nearest source selected");
        check(installed.resolve(new LegacyModDescriptor(descriptor.file(),"fixture","UNKNOWN","1.19.2","test")).isEmpty(),"unknown loader admitted");
        rejects(IllegalArgumentException.class,()->new SourceProfiles(List.of(profile,profile)));
        var overlap = new ProbeProfile("overlap",true);
        rejects(IllegalStateException.class,()->new SourceProfiles(List.of(profile,overlap)).resolve(descriptor));
        check(overlap.factoryCalls==0,"selection created a session");
        var candidates = new ArrayList<SourceProfile<?>>();candidates.add(profile);
        var snapshot = new SourceProfiles(candidates);candidates.clear();check(snapshot.installed().size()==1,"catalog aliases mutable list");
        check(profile.evidence(Path.of("run")).runtimeJar().equals(Path.of("run/legacy-runtime/1.19.2/client.jar")),"source evidence moved to target");
        var dependencies = new ArrayList<Path>();dependencies.add(Path.of("dependency.jar"));
        var entrypoints = new ArrayList<>(List.of("fixture.Entry"));
        var request = new SessionRequest("fixture",descriptor.file(),dependencies,CompositionContractCheck.class.getClassLoader(),entrypoints,"CLIENT");
        dependencies.clear();entrypoints.clear();check(request.dependencies().size()==1&&request.entrypoints().size()==1,"factory request aliases mutable inputs");
        check(profile.createTransformer("CLIENT") != profile.createTransformer("CLIENT"),"sessions share transformer evidence caches");
        rejects(IllegalArgumentException.class,()->profile.createTransformer("UNKNOWN"));
        pipeline();registration();admission(profile);
        System.out.println("PASS: evidence-only profile selection/ambiguity, immutable factory inputs, fresh transformer ownership, target binding/contract gates, ordered source/projection/target passes and isolated partial-flush/late registration");
    }

    private static void pipeline() {
        var order = new ArrayList<String>();var node = new ClassNode();node.name="source";
        var p = new TransformationPipeline(List.of(
                new Pass("source",Phase.SOURCE_SEMANTICS,n->{check(n.name.equals("source"),"source identity lost early");order.add("source");return 1;}),
                new Pass("projection",Phase.NAMESPACE_PROJECTION,n->{n.name="target";order.add("projection");return 2;}),
                new Pass("target",Phase.TARGET_SHAPE,n->{check(n.name.equals("target"),"target shape before projection");order.add("target");return 3;})));
        check(p.apply(node)==6&&order.equals(List.of("source","projection","target")),"pass order/count changed");
        rejects(IllegalArgumentException.class,()->new TransformationPipeline(List.of(
                new Pass("a",Phase.TARGET_SHAPE,n->0),new Pass("b",Phase.SOURCE_SEMANTICS,n->0))));
        rejects(IllegalArgumentException.class,()->new TransformationPipeline(List.of(
                new Pass("a",Phase.SOURCE_SEMANTICS,n->0),new Pass("a",Phase.SOURCE_SEMANTICS,n->0))));
        rejects(IllegalArgumentException.class,()->new TransformationPipeline(List.of(
                new Pass("a",Phase.NAMESPACE_PROJECTION,n->0),new Pass("b",Phase.NAMESPACE_PROJECTION,n->0))));
        var failure = new IllegalArgumentException("pass-failure");order.clear();
        try {
            new TransformationPipeline(List.of(new Pass("fail",Phase.SOURCE_SEMANTICS,n->{throw failure;}),
                    new Pass("later",Phase.TARGET_SHAPE,n->{order.add("later");return 0;}))).apply(node);
            throw new AssertionError("pass failure swallowed");
        } catch (IllegalArgumentException actual) {check(actual==failure&&order.isEmpty(),"pass failure replaced or pipeline continued");}
        var steps = new Forge1192Profile().createTransformer("CLIENT").transformationSteps();
        check(steps.stream().filter(s->s.phase()==Phase.NAMESPACE_PROJECTION).count()==1,"source pipeline projection count");
        check(steps.getFirst().id().equals("physical-distribution"),"distribution filter lost first position");
    }

    private static void registration() {
        var queue = new StagedRegistrations<String,String,Object>();
        Object a=new Object(),b=new Object(),c=new Object();var order=new ArrayList<String>();
        queue.stage("owner","items","a",a);queue.stage("owner","items","b",b);queue.stage("owner","items","c",c);
        queue.stage("other","items","other",new Object());queue.stage("owner","blocks","block",new Object());
        var failure=new IllegalArgumentException("registration-failure");
        try {
            queue.flush("owner","items",(name,value)->{if(name.equals("b"))throw failure;order.add(name);});
            throw new AssertionError("registration failure swallowed");
        } catch (IllegalArgumentException actual) {check(actual==failure,"registration failure replaced");}
        check(order.equals(List.of("a")),"flush attempted suffix after failure");
        check(queue.nameForIdentity("owner",a).isEmpty()&&queue.nameForIdentity("owner",b).orElseThrow().equals("b"),"accepted head lost or failing entry dropped");
        queue.flush("owner","items",(name,value)->order.add(name));
        check(order.equals(List.of("a","b","c")),"retry repeated accepted work or lost suffix");
        queue.complete("owner","items");
        rejects(IllegalStateException.class,()->queue.stage("owner","items","late",new Object()));
        check(!queue.completed("other","items")&&!queue.completed("owner","blocks"),"phase completion escaped scope");
        queue.flush("other","items",(name,value)->order.add(name));queue.flush("owner","blocks",(name,value)->order.add(name));
        check(order.equals(List.of("a","b","c","other","block")),"owner/key isolation lost");
        Object shared=new Object();queue.stage("owner","first","same",shared);queue.stage("owner","second","same",shared);
        check(queue.nameForIdentity("owner",shared).orElseThrow().equals("same"),"identity name lookup changed");
        queue.stage("owner","third","ambiguous",shared);
        rejects(IllegalStateException.class,()->queue.nameForIdentity("owner",shared));
    }

    private static void admission(Forge1192Profile profile) throws Exception {
        var directory=Files.createTempDirectory("lms-composition-admission");
        try(var jar=new JarOutputStream(Files.newOutputStream(directory.resolve("fixture.jar")))) {
            jar.putNextEntry(new JarEntry("META-INF/mods.toml"));
            jar.write("modLoader=\"javafml\"\nloaderVersion=\"[43,)\"\nlicense=\"test\"\n[[mods]]\nmodId=\"fixture\"\nversion=\"1\"\n[[dependencies.fixture]]\nmodId=\"minecraft\"\nmandatory=true\nversionRange=\"[1.19.2]\"\nordering=\"NONE\"\nside=\"BOTH\"\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));jar.closeEntry();
        }
        var profiles=new SourceProfiles(List.of(profile));
        rejects(IllegalStateException.class,()->RuntimeBootstrap.start(directory,profiles,List.of()));
        int[] calls={0};
        SourceRuntimeBinding missing=new SourceRuntimeBinding() {
            public SourceProfile<?> profile(){return profile;}
            public String targetId(){return "test-target";}
            public Set<CompatibilityContract> providedContracts(){return Set.of();}
            public void start(List<LegacyModDescriptor> selected,List<LegacyModDescriptor> all){calls[0]++;}
        };
        rejects(IllegalStateException.class,()->RuntimeBootstrap.start(directory,profiles,List.of(missing)));
        check(calls[0]==0,"session admission before target contract validation");
        SourceRuntimeBinding complete=new SourceRuntimeBinding() {
            public SourceProfile<?> profile(){return profile;}
            public String targetId(){return "test-target";}
            public Set<CompatibilityContract> providedContracts(){return profile.requiredContracts();}
            public void start(List<LegacyModDescriptor> selected,List<LegacyModDescriptor> all){check(selected.size()==1&&all.size()==1,"admission descriptor selection lost");calls[0]++;}
        };
        RuntimeBootstrap.start(directory,profiles,List.of(complete));check(calls[0]==1,"selected runtime binding not used exactly once");
    }

    private static final class ProbeProfile implements SourceProfile<RuntimeSession> {
        private final String id;private final boolean matches;int factoryCalls;
        ProbeProfile(String id,boolean matches){this.id=id;this.matches=matches;}
        public String id(){return id;}
        public boolean supports(LegacyModDescriptor mod){return matches;}
        public SourceEvidencePaths evidence(Path root){throw new AssertionError("selection loaded mappings");}
        public InstrumentedClassTransformer createTransformer(String side){throw new AssertionError("selection created transformer");}
        public RuntimeSession createSession(SessionRequest request){factoryCalls++;throw new AssertionError("selection created session");}
        public Set<CompatibilityContract> requiredContracts(){return Set.of();}
        public List<com.fuckingdeveloper.lms.transform.TransformationSpec> planLaunch(Path root,LegacyModDescriptor mod){throw new AssertionError("selection planned launch");}
    }
    private interface Checked { void run() throws Exception; }
    private static void rejects(Class<? extends Exception> kind,Checked action) {
        try {action.run();}catch(Exception failure){if(kind.isInstance(failure))return;throw new AssertionError("Unexpected failure",failure);}
        throw new AssertionError("Expected " + kind.getSimpleName());
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
