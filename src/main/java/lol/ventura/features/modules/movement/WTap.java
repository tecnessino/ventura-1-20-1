package lol.ventura.features.modules.movement;

import lol.ventura.features.events.PlayerAttackEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;

@ModuleDescriptor(name = "W Tap", category = Category.MOVEMENT, brief = "WTap")
public class WTap extends Module {

    private long ilecip = 0;
    private boolean walisz = false;

    public WTap(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<PlayerAttackEvent> onAttack = event -> {
        walisz = true;
        if (walisz == true) {
            if (System.currentTimeMillis() - ilecip > 2000) {
                if (System.currentTimeMillis() - ilecip > 100) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 105) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 110) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 115) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 120) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 125) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 130) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 135) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 140) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 145) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 150) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 155) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 160) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 165) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 170) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 175) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 180) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 185) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 190) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 195) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 200) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 205) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 210) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 215) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 220) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 225) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 230) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 235) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 240) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 245) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 250) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 255) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 260) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 265) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 270) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 275) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 280) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 285) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 290) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 295) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 300) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 305) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 310) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 315) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 320) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 325) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 330) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 335) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 340) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 345) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 350) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 355) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 360) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 365) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 370) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 375) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 380) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 385) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 390) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 395) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 400) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 405) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 410) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 415) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 420) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 425) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 430) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 435) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 440) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 445) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 450) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 455) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 460) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 465) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 470) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 475) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 480) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 485) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 490) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 495) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 500) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 505) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 510) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 515) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 520) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 525) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 530) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 535) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 540) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 545) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 550) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 555) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 560) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 565) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 570) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 575) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 580) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 585) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 590) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 595) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 600) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 605) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 610) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 615) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 620) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 625) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 630) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 635) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 640) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 645) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 650) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 655) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 660) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 665) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 670) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 675) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 680) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 685) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 690) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 695) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 700) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 705) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 710) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 715) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 720) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 725) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 730) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 735) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 740) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 745) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 750) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 755) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 760) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 765) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 770) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 775) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 780) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 785) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 790) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 795) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 800) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 805) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 810) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 815) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 820) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 825) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 830) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 835) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 840) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 845) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 850) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 855) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 860) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 865) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 870) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 875) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 880) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 885) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 890) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 895) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 900) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 905) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 910) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 915) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 920) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 925) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 930) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 935) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 940) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 945) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 950) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 955) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 960) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 965) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 970) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 975) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 980) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 985) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 990) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 995) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1000) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1005) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1010) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1015) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1020) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1025) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1030) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1035) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1040) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1045) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1050) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1055) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1060) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1065) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1070) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1075) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1080) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1085) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1090) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1095) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1100) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1105) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1110) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1115) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1120) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1125) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1130) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1135) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1140) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1145) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1150) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1155) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1160) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1165) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1170) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1175) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1180) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1185) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1190) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1195) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1200) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1205) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1210) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1215) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1220) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1225) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1230) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1235) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1240) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1245) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1250) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1255) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1260) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1265) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1270) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1275) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1280) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1285) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1290) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1295) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1300) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1305) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1310) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1315) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1320) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1325) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1330) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1335) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1340) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip > 1345) {
                    mc.options.forwardKey.setPressed(true);
                } else if (System.currentTimeMillis() - ilecip < 1350) {
                    mc.options.forwardKey.setPressed(false);
                    ilecip = 0;
                }
            }
        } else if (mc.options.backKey.isPressed()) {
            return;
        }
    };

    private final IEventListener<TickEvent> onTick = event -> {
        walisz = false;
    };

}
