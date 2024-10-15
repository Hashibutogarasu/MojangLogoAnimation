package io.github.hashibutogarasu.mla.mixin.client;

import io.github.hashibutogarasu.mla.MojangLogoAnimationClient;
import io.github.hashibutogarasu.mla.config.ModConfig;
import io.github.hashibutogarasu.mla.sounds.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Unique
    private static boolean firstLoad = true;

    @Shadow
    @Final
    private ResourceReload reload;

    @Shadow
    private float progress;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private boolean reloading;

    @Unique
    private boolean animationStarting = false;

    @Unique
    private boolean animationEnded = false;

    @Unique
    private int animProgress = 0;

    @Unique
    private final PositionedSoundInstance MOJANG_SOUND = PositionedSoundInstance.master(mode == ModConfig.Mode.MOJANG_STUDIOS ? ModSounds.MOJANG_LOGO_SOUND_EVENT : ModSounds.MOJANG_APRIL_FOOL_SOUND_EVENT, 1.0f, 1.0f);
    @Unique
    private static ModConfig.Mode mode;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private static void init(CallbackInfo ci) {
        mode = MojangLogoAnimationClient.config.mode;
    }

    @Redirect(method = "render",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashOverlay;LOGO:Lnet/minecraft/util/Identifier;"))
    private Identifier logo() {
        return mode == ModConfig.Mode.MOJANG_STUDIOS ? getMojang(this.animProgress) : getAprilfool(this.animProgress);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReload;getProgress()F"))
    private float getProgress(ResourceReload instance) {
        return this.reload.getProgress();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIII)V"))
    private void fill(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 0))
    private void drawTexture0(DrawContext context, Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        double d = Math.min((double) context.getScaledWindowWidth() * 0.75, context.getScaledWindowHeight()) * 0.25;
        double e = d * 4.0;
        int r = (int) (e);

        if (progress > 0) {
            if (!animationStarting && firstLoad) {
                this.reload.whenComplete().thenAccept(object -> {
                    client.getSoundManager().play(MOJANG_SOUND);
                    getAnimationThread().start();
                });
                animationStarting = true;
            }
        }
        context.drawTexture(mode == ModConfig.Mode.MOJANG_STUDIOS ? getMojang(this.animProgress) : getAprilfool(this.animProgress), x, y, r, (int) d, u, v, regionWidth, regionHeight + 60, textureWidth, textureHeight);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIFFIIII)V", ordinal = 1))
    private void drawTexture1(DrawContext context, Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {

    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashOverlay;reloading:Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private boolean isReloading(SplashOverlay instance) {
        return (firstLoad && !animationEnded) || reloading;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "render")
    private void init(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        firstLoad = false;
    }

    @Unique
    private Identifier getMojang(int index) {
        return firstLoad ? Identifier.of("mla", "textures/gui/title/mojang/mojang" + index + ".png") : Identifier.of("mla", "textures/gui/title/mojang/mojang38.png");
    }

    @Unique
    private Identifier getAprilfool(int index) {
        return firstLoad ? Identifier.of("mla", "textures/gui/title/mojang_april_fool/mojang" + index + ".png") : Identifier.of("mla", "textures/gui/title/mojang/mojang38.png");
    }

    @Unique
    private @NotNull Thread getAnimationThread() {
        var animthread = new Thread(() -> {
            this.animProgress = 0;
            for (int i = 0; i < 38; i++) {
                animProgress++;
                try {
                    Thread.sleep(70);
                } catch (InterruptedException ignored) {

                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
            animationEnded = true;
        });

        animthread.setName("animthread");
        return animthread;
    }
}
