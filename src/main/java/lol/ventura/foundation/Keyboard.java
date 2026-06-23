package lol.ventura.foundation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

public final class Keyboard extends Service {
    @Getter @Setter
    private static Keyboard instance;

    @RequiredArgsConstructor
    @Getter @Setter
    public static class KeyCombo
    {
        private final int[] keycodes;
        private final Runnable onFire;
        private int pointer = 0;
    }

    private ArrayList<KeyCombo> combos = new ArrayList<>();
    private ArrayList<KeyCombo> matchingCombos = new ArrayList<>();

    //chujowy kod jest w Keyboard.java ! trzeba to zrobic lepiej kiedys ? !
    public final void register(final KeyCombo combo)
    {
        combos.add(combo);
    }

    public final void unregister(final KeyCombo combo)
    {
        combos.remove(combo);
    }

    public void onKey(int keycode)
    {
        if(MinecraftClient.getInstance().currentScreen != null)
            return;

        if(matchingCombos.isEmpty())
            matchingCombos = new ArrayList<>(combos);

        ArrayList<KeyCombo> combos = new ArrayList<>(matchingCombos);

        for(KeyCombo combo : combos)
        {
            if(combo.keycodes[combo.pointer] != keycode)
            {
                combo.pointer = 0;
                matchingCombos.remove(combo);
            } else combo.pointer++;

            if(combo.pointer == combo.keycodes.length)
            {
                combo.onFire.run();
                combo.pointer = 0;
                matchingCombos.clear();
            }
        }
    }

    public void onKeyRelease(int keycode)
    {
        matchingCombos.clear();
    }
}
