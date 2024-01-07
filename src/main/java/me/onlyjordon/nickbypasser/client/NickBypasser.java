package me.onlyjordon.nickbypasser.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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
                if (client.player == null) return;
                client.player.sendMessage(
                        Text.literal((Values.SHOW_REAL_NAMES ? "No longer" : "Now") +
                                " showing real igns above people's heads!")
                                .styled(style -> style.withColor(TextColor.fromRgb(0x227C9D))),
                        false);
                Values.SHOW_REAL_NAMES = !Values.SHOW_REAL_NAMES;
            }
        });
    }

}
