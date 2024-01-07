package me.onlyjordon.nickbypasser.client.mixins;

import de.snowii.GameProfile;
import de.snowii.MojangAPI;
import me.onlyjordon.nickbypasser.client.Values;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {


    @Final
    @Shadow
    protected EntityRenderDispatcher dispatcher;
    @Unique
    private final ConcurrentMap<UUID,String> nameMap = new ConcurrentHashMap<>();
    @Unique
    private final List<UUID> currentlyCalculating = new ArrayList<>();

    @Inject(method = "renderLabelIfPresent", at = @At("RETURN"))
    private void renderCustomContentAboveNameTag(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!Values.SHOW_REAL_NAMES) return;
        double d = this.dispatcher.getSquaredDistanceToCamera( entity);
        if (!(d > 512.0)) {
            if (entity.getType() == EntityType.PLAYER) {
                UUID uuid = entity.getUuid();
                if (!nameMap.containsKey(uuid)) {
                    if (!currentlyCalculating.contains(uuid)) {
                        calculateNameAsync(uuid);
                        return;
                    }
                    return;
                }
                String realName = nameMap.get(uuid);
                if (realName == null || realName.isEmpty()) {
                    return;
                }
                MutableText rlName = Text.literal(realName).styled(style -> style.withColor(0x52b788));
                if (!Objects.equals(entity.getDisplayName().getString(), realName))
                    renderCustomContentAboveNameTag(entity, rlName, matrices, vertexConsumers, light);
            }
        }
    }

    @Unique
    private void calculateNameAsync(UUID uuid) {
        currentlyCalculating.add(uuid);
        CompletableFuture<GameProfile> uuidFuture = new CompletableFuture<>();
        uuidFuture.completeAsync(() -> {
            try {
                return MojangAPI.getGameProfile(uuid);
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(g -> {
            if (g == null) {
                nameMap.put(uuid, null);
                currentlyCalculating.remove(uuid);
                return;
            }
            currentlyCalculating.remove(uuid);
            nameMap.put(uuid, g.getName());
        });
    }

    @Unique
    protected void renderCustomContentAboveNameTag(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//        double d = this.dispatcher.getSquaredDistanceToCamera(entity);
//        if (!(d > 4096.0)) {
        boolean bl = !entity.isSneaky();
        float f = entity.getNameLabelHeight();
        int i = "deadmau5".equals(text.getString()) ? -20 : -10;
        matrices.push();
        matrices.translate(0.0F, f, 0.0F);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int j = (int)(g * 255.0F) << 24;
        TextRenderer textRenderer = this.getTextRenderer();
        float h = (float)(-textRenderer.getWidth(text) / 2);
        textRenderer.draw(text, h, (float)i, 553648127, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, j, light);
        if (bl) {
            textRenderer.draw(text, h, (float)i, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }
        matrices.pop();
//        }
    }

    @Shadow
    public TextRenderer getTextRenderer() {
        return null;
    }

}