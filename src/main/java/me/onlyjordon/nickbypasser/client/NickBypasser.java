package me.onlyjordon.nickbypasser.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NickBypasser implements ClientModInitializer {

    private static KeyBinding keyBinding;


    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle real names above head",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "Names"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                MutableText text = Text.literal((Values.SHOW_REAL_NAMES ? "No longer" : "Now") +  " showing real igns above people's heads!");
                text.setStyle(text.getStyle().withColor(0x227C9D));

                client.player.sendMessage(text, false);
                Values.SHOW_REAL_NAMES = !Values.SHOW_REAL_NAMES;
            }
        });
    }

}
