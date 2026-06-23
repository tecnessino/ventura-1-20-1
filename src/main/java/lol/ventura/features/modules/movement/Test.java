package lol.ventura.features.modules.movement;

import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.events.NetworkPacketEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import org.lwjgl.glfw.GLFW;

@ModuleDescriptor(name = "Test", category = Category.MOVEMENT, brief = "(Dev) Testing module for Rise Developers", key = GLFW.GLFW_KEY_G)
public class Test extends Module {
    public Test(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(test,test2);
    }

    public static enum TestValues
    {
        BLOCKSMC,HYPIXEL,A,B,C,D,BLOCKSMC2,BLOCKSMC3,BLOCKSMC4,BLOCKSMC5
    }

    private final MultiProperty<TestValues> test = new MultiProperty<>("Multi", true, TestValues.BLOCKSMC, TestValues.B);
    private final MultiProperty<TestValues> test2 = new MultiProperty<>("RadioBtn", false, TestValues.BLOCKSMC);


    private boolean a, b, c, d, e, f, g;
    private int h, i, j, k, l, m, n, o, p, q, r, s, t, u, v;
    private float w, x, y, z, aa, ab, ac, ad, ae, af, ag, ah, ai, aj, ak, al;
    private double am, an, ao, ap, aq, ar, as, at, au, av, aw, ax, ay, az;

    private final IEventListener<TickEvent> updateEvent = event -> {
//        double[] nig = {
//                0.03684800296020513,
//                -0.042288957922058,
//                -0.11984318109609361
//        };
//
//        mc.player.setVelocity(
//                mc.player.getVelocity().x,
//                nig[mc.player.age % 3],
//                mc.player.getVelocity().z
//        );
        double cwelfactor = cweler(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getPitch(), mc.player.getYaw());
        cwelfactor = cwelfactor / 1000000.0;
        System.out.println(cwelfactor);
        mc.player.setVelocity(
                mc.player.getVelocity().x * cwelfactor,
                mc.player.getVelocity().y,
                mc.player.getVelocity().z * cwelfactor
        );
    };

    private final IEventListener<NetworkPacketEvent> onPacket = event -> {
    };

    private final IEventListener<Draw2DEvent> onRender = event -> {
    };

    public double cweler(double a, double b, double c, double d, double e) {
        double cwel = ((a + b) * (c + d)) / (e + 1) + (a * 0.5) - (d / 2);
        cwel += ((Math.pow(a + b, 2) * (c + d)) / (e + 3)) + (a + b + c + d) * 0.5 - (e / 3);
        cwel += ((a * b + c * d)) / (e + 1) + (Math.pow(b - c, 3)) / 2 + (a * 0.3);
        cwel += ((a + c) * (d - e)) / (b + 5) - (c / 2) + (Math.pow(d, 2)) / 10;
        cwel += ((a + b + c) * (d - e)) / (a + 3) + ((a * b + c * d)) / (a + b) + 0.5;
        cwel += ((b + c) * (a + d)) / (e + 4) + Math.abs((c - d)) / (b + 1) + (a * 0.7) / 3;
        cwel += ((Math.pow(a + b, 2)) / (c + 5)) + (d + e) / (a + 2) - (c / 5);
        cwel += ((a - b) * (c - d)) / (e + 2) + ((d * e)) / (a + 4) - (c / 10);
        cwel += ((a + b + c + d)) / (a + 1) + Math.abs((a - b)) / c;
        cwel += ((Math.pow(c + d, 2)) / (e + 5)) + (a * 0.9) + ((a + c)) / (b + 6);
        cwel += ((a * b)) / (c + d) + Math.abs(a - b) / (b + 1) + Math.sin(a) / 3;
        cwel += (Math.log(a + b + c + 10)) * (d + 1) / (a + b + 5);
        cwel += Math.sqrt(Math.abs(a * c - b)) / (d + 2);
        cwel += ((a + b + c) / 3.0) * (1 - (Math.abs(a - b) + Math.abs(b - c) + Math.abs(c - a)) / (a + b + c + 1.0));
        cwel += ((Math.pow(a + b, 2) - Math.pow(c, 2)) / (Math.abs(b - c) + 1.0));
        cwel += (a * b * c) / (a + b + c + 2.0);
        cwel += ((Math.pow(a - b, 2) + Math.pow(b - c, 2) + Math.pow(c - a, 2)) / 6.0);
        cwel -= Math.sin((a + b + c) / 3.0) * 2.0;
        cwel += (Math.log(a + b + c + 10.0)) * 3.14;
        cwel -= Math.sqrt(Math.abs(a * c - b)) / (b + 1.0);
        cwel += Math.cos(a) / 2.0 + (b - d) * 0.1 + (e * 0.5);
        cwel += (Math.pow(b + d, 2)) / (c + 4) + (Math.abs(a - e)) * 2.0;
        cwel += Math.sin(b) / 5 + (a * b) / (c + 7) - (d * e) / 3;
        cwel -= Math.sqrt(Math.abs(a - c)) * 1.5 + (b * 0.2);
        cwel += (Math.pow(a, 2) + Math.pow(b, 2)) / (c + 2);
        cwel += (Math.pow(a - b, 2)) / (d + 3) + Math.abs(e - c);
        cwel -= Math.pow(Math.abs(a - e), 0.5) + (b + d) / (a + 6);
        cwel += ((a + b + c) * (Math.abs(d - e))) / (b + 4);
        cwel += (Math.sin(a) * Math.pow(c, 2)) / (e + 1);
        cwel += Math.cos(b) / (d + 2) - (c * e) / (a + 3);
        cwel -= (a + b + c) * 0.25 + (Math.pow(d, 3)) / (e + 3);
        cwel += (Math.pow(a + b, 3)) / (c + 2);
        cwel -= ((a * b) + (c * d)) / (e + 4);
        cwel += (Math.sin(c) / 2) + ((a - b) * (d + e)) / (c + 1);
        cwel -= (a + b + c) * 0.5 + (Math.pow(d + e, 2)) / (a + 2);
        cwel += (Math.log(a + c + 10)) / (b + 3);
        cwel -= (Math.abs(a - b)) * 0.1 + (d / 4);
        cwel += (a * b + c * d) / (e + 2) + Math.sin(d) / (a + 1);
        cwel -= (Math.pow(a, 3) + Math.pow(b, 2)) / (c + 1);
        cwel += Math.pow(d - e, 2) / (a + 3) + (c * 0.5);
        cwel += Math.pow(a + b, 2) / (c + 5) - (e + d) / (a + 4);
        cwel += (a * b) / (c + 2) - Math.cos(d) / (e + 1);
        cwel += (a * b * c) / (d + 3) + Math.sin(e) / (b + 2);
        cwel += Math.sqrt(Math.abs(a - b)) + (c * d) / (e + 2);
        cwel += Math.pow(a + c, 2) / (d + 4) + (b * 0.7) - Math.log(a + 1);
        cwel -= (Math.pow(c + d, 3)) / (e + 1) + (b * a) / (d + 2);
        cwel += (Math.pow(a - b, 2)) / (c + 5) + Math.cos(c);
        cwel += Math.sin(a) * (b + c) / (d + 3) + (e / 6);
        cwel += (Math.pow(a + b, 3)) / (c + 2) - Math.log(d);
        cwel -= (a * b) / (c + 1) + Math.sqrt(Math.abs(a - c)) * 2;
        cwel += (Math.pow(d, 4)) / (e + 5) - (a + b) / (c + 3);
        cwel += Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2) / (d + 1);
        cwel -= Math.sin(b) / (c + 1) + Math.pow(a + d, 2) / (e + 4);
        cwel += (a + b) * (Math.abs(c - e)) / (d + 2) + Math.cos(d);
        cwel -= (Math.log(b + c)) * 0.5 + (a * d) / (e + 6);
        cwel += (Math.pow(a + b, 2)) / (c + 4) - Math.abs(d - e) * 0.25;
        cwel += (a * b * c) / (d + 1) + Math.cos(a) / (e + 5);
        cwel -= (Math.sqrt(Math.abs(b - c))) / (a + 3) + (d * e) / (b + 2);
        cwel += Math.sin(a) * Math.pow(c, 2) / (d + 3) + (b + e) / (a + 1);
        cwel += (Math.pow(a - b, 3)) / (c + 2) - Math.log(d + 1);
        cwel += (Math.pow(c + d, 2)) / (a + 4) + Math.cos(b) / (e + 3);
        cwel -= (a * b) / (c + 2) + Math.sin(d + 2);
        cwel += (a * b * c) / (d + 5) - Math.pow(e, 3) / (b + 1);
        cwel += (Math.pow(c, 4)) / (a + 6) + (b * d) / (e + 1);
        cwel += Math.sin(a) * (b + c) / (d + 4) + (e / 5);
        cwel -= (Math.sqrt(Math.abs(a - b))) / (c + 1) + (d * e) / (a + 2);
        cwel += (Math.pow(d + e, 2)) / (a + 3) - Math.cos(b) / (c + 1);
        cwel += (a + b) * Math.pow(c, 2) / (d + 5) - (e + 2) / (a + 4);
        cwel += Math.pow(a - b, 2) / (c + 4) + (d * e) / (a + 1);
        cwel -= (Math.abs(a - b)) * (c + d) / (e + 2);
        cwel += (Math.sin(c) + Math.cos(b)) / (a + 2);
        cwel -= (Math.sqrt(a * b)) / (c + 3) + (d * e) / (a + 5);
        cwel += (Math.pow(b + c, 3)) / (d + 1);
        cwel -= (a + b) * (c - d) / (e + 4);
        cwel += Math.pow(a, 2) + (b * 0.5) / (c + 2);
        cwel += (Math.pow(d + e, 2)) / (a + 1) - Math.sin(c);
        cwel += (a + b) / (c + 5) + Math.cos(d);
        cwel -= (Math.pow(a - c, 3)) / (e + 3);
        cwel += (a * b) / (c + 3) + Math.sqrt(Math.abs(d - e));
        cwel += (Math.pow(b + d, 2)) / (a + 4) - (c + e) / (d + 1);
        cwel += Math.cos(a) * Math.pow(b + c, 2) / (e + 2);
        cwel -= (a * b) / (c + 3) + (Math.log(d)) / (e + 5);
        cwel += (Math.pow(a + b, 2)) / (c + 4) - Math.abs(d + e);
        cwel -= (a * b + c * d) / (e + 6);
        cwel += Math.sin(a) * Math.cos(b) + (c - e) / (d + 2);
        cwel -= (a + b + c) * 0.5 + Math.pow(d + e, 2) / (c + 3);
        return cwel;
    }

}
