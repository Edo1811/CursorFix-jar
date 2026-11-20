package dev.ranastudios.cursorfix.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GLFW (Windows) saves the OS cursor position into a global "restore" slot whenever the
 * cursor gets disabled, including on WM_SETFOCUS after alt-tabbing back in. When Minecraft
 * unlocks the cursor for a menu, it calls glfwSetCursorPos(center) while the cursor is still
 * DISABLED (which only moves GLFW's virtual cursor), then switches to NORMAL, and GLFW
 * warps the real cursor to that stale restore spot.
 *
 * Fix: once the cursor is NORMAL again, set the position a second time so it actually lands
 * in the center.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double x;
    @Shadow private double y;

    @Inject(method = "unlockCursor", at = @At("TAIL"))
    private void cursorfix$recenterAfterUnlock(CallbackInfo ci) {
        long handle = client.getWindow().getHandle();
        if (GLFW.glfwGetInputMode(handle, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL) {
            GLFW.glfwSetCursorPos(handle, x, y);
        }
    }
}
