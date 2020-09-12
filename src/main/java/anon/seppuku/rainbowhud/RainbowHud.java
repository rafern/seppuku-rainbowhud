package me.anon.seppuku.rainbowhud;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.impl.management.ModuleManager;
import me.rigamortis.seppuku.api.command.Command;
import me.rigamortis.seppuku.impl.command.ColorCommand;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.event.render.EventRender2D;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import me.anon.seppuku.rainbowhud.util.CommandReplacer;
import me.anon.seppuku.rainbowhud.util.ColorConfigurableController;

public final class RainbowHud extends Module {
    // Unfortunately this needs to be declared here otherwise it is auto-loaded
    public final class ColorCommandReplacement extends Command {
        private ColorCommand colorCommand;

        public ColorCommandReplacement() {
            // Setup command with the same properties as the color command
            super("Color",
                  new String[] { "Col", "Colour" },
                  "Allows you to change arraylist colors",
                  "Color <Module> <Hex>"
            );

            // Create an internal color command
            colorCommand = new ColorCommand();
        }

        public void exec(String input) {
            // Re-enable the color configurable
            ColorConfigurableController.enableColorConfigurable();

            // Execute the original color command
            colorCommand.exec(input);

            // Disable the color configurable
            ColorConfigurableController.disableColorConfigurable();
        }
    }

    public final NumberValue<Float> speedVal = new NumberValue<Float>(
        "Speed",
        new String[] { "Spd", "Velocity", "Vel" },
        Float.valueOf(0.4f),    // Default speed
        Float.class,
        Float.valueOf(0.1f),    // Minimum speed
        Float.valueOf(10.0f),   // Maximum speed
        Float.valueOf(0.1f)     // Speed increment
    );

    public final NumberValue<Float> offsetVal = new NumberValue<Float>(
        "Offset",
        new String[] { "Off" },
        Float.valueOf(0.02f),   // Default offset
        Float.class,
        Float.valueOf(-1.0f),   // Minimum offset
        Float.valueOf(1.0f),    // Maximum offset
        Float.valueOf(0.01f)    // Offset increment
    );

    public final NumberValue<Float> brightnessVal = new NumberValue<Float>(
        "Brightness",
        new String[] { "Bright", "Light", "Whiteness" },
        Float.valueOf(0.25f),   // Default brightness
        Float.class,
        Float.valueOf(0.0f),    // Minimum brightness
        Float.valueOf(1.0f),    // Maximum brightness
        Float.valueOf(0.01f)    // Brightness increment
    );

    public final NumberValue<Float> opacityVal = new NumberValue<Float>(
        "Opacity",
        new String[] { "Alpha" },
        Float.valueOf(1.0f),    // Default opacity
        Float.class,
        Float.valueOf(0.0f),    // Minimum opacity
        Float.valueOf(1.0f),    // Maximum opacity
        Float.valueOf(0.01f)    // Opacity increment
    );

    public final BooleanValue waveVal = new BooleanValue(
        "Wave",
        new String[] { "Waterfall" },
        Boolean.valueOf(true)   // Toggled on by default
    );

    public RainbowHud() {
        super("RainbowHud",
              new String[] { "Rainbow", "Rbh", "Gay" },
              "Adds a rainbow effect to the hud's module list",
              "NONE",
              -1,                       // Default color (white)
              true,                     // Hidden by default
              false,                    // Disabled by default. Events listeners won't be registered if this is true
              Module.ModuleType.RENDER  // This is a render module
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ColorConfigurableController.disableColorConfigurable();
        CommandReplacer.replace(ColorCommand.class, new ColorCommandReplacement());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ColorConfigurableController.enableColorConfigurable();
        CommandReplacer.replace(ColorCommandReplacement.class, new ColorCommand());
    }

    private int rgbToHex(int mask, float r, float g, float b) {
        // Converts RGBA float values to hex colors
        return mask | ((int)(255 * r) << 16) | ((int)(255 * g) << 8) | (int)(255 * b);
    }

    private int hueToRGBHex(float hue, float b, int mask) {
        // Clamp the hue value
        hue = hue % 1.0f;
        if(hue < 0.0f)
            hue += 1.0f;

        // Basically this: https://en.wikipedia.org/wiki/File:HSV-RGB-comparison.svg
        float p = hue * 6.0f;   // Hue phase
        float si = p % 1.0f;    // Initial upwards slope value
        float di = 1.0f - si;   // Initial downwards slope value
        float ib = 1.0f - b;    // Inverse of brightness value
        float s = b + ib * si;  // Final upwards slope value with brightness
        float d = b + ib * di;  // Final downwards slope value with brightness
        switch((int)p) {
            case 0:
                return rgbToHex(mask, 1.0f,    s,    b);
            case 1:
                return rgbToHex(mask,    d, 1.0f,    b);
            case 2:
                return rgbToHex(mask,    b, 1.0f,    s);
            case 3:
                return rgbToHex(mask,    b,    d, 1.0f);
            case 4:
                return rgbToHex(mask,    s,    b, 1.0f);
            case 5:
            default:
                return rgbToHex(mask, 1.0f,    b,    d);
        }
    }

    private void hiddenRainbow() {
        // For some reason an alpha value of 4 is the minimum for hiding text. Anything
        // less and it goes back to full opacity
        int hiddenColor = 0x04000000;

        // "Hide" each module
        List<Module> modules = Seppuku.INSTANCE.getModuleManager().getModuleList();
        Iterator<Module> it = modules.iterator();
        while(it.hasNext()) {
            Module mod = it.next();
            mod.setColor(hiddenColor);
        }
    }

    private void basicRainbow(float baseHue, float brightness, int mask) {
        // Get modules
        List<Module> modules = Seppuku.INSTANCE.getModuleManager().getModuleList();
        Iterator<Module> it = modules.iterator();

        // Color each module
        while(it.hasNext()) {
            Module mod = it.next();

            // Skip if disabled or hidden
            if(mod == null || !mod.isEnabled() || mod.getType() == Module.ModuleType.HIDDEN || mod.isHidden()) {
                continue;
            }

            // Set color
            mod.setColor(hueToRGBHex(baseHue, brightness, mask));
        }
    }

    private void waveRainbow(float baseHue, float brightness, int mask) {
        // Mimic hud module's module ordering
        // Slightly changed decompiled code from hud's drawModules
        List<Module> modules = new ArrayList<Module>();

        for(Module mod : Seppuku.INSTANCE.getModuleManager().getModuleList()) {
            if(mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                modules.add(mod);
            }
        }

        Comparator<Module> comparator = new Comparator<Module>() {
            @Override
            public int compare(Module first, Module second) {
                String firstName = first.getDisplayName() + ((first.getMetaData() != null) ? (" " + first.getMetaData()) : "");
                String secondName = second.getDisplayName() + ((second.getMetaData() != null) ? (" " + second.getMetaData()) : "");
                float dif = ((Minecraft.getMinecraft()).fontRenderer.getStringWidth(secondName) - (Minecraft.getMinecraft()).fontRenderer.getStringWidth(firstName));
                return (dif != 0.0F) ? (int)dif : secondName.compareTo(firstName);
            }
        };

        Collections.sort(modules, comparator);

        // Color each module
        Iterator<Module> it = modules.iterator();
        int line = 0;
        while(it.hasNext()) {
            Module mod = it.next();

            // Skip if disabled or hidden
            if(mod == null || !mod.isEnabled() || mod.getType() == Module.ModuleType.HIDDEN || mod.isHidden()) {
                continue;
            }

            // Set color
            float thisHue = baseHue + offsetVal.getFloat() * line;
            mod.setColor(hueToRGBHex(thisHue, brightness, mask));

            // Increment line
            line++;
        }
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        // Get Minecraft instance
        Minecraft mc = Minecraft.getMinecraft();

        // Don't update colors when debug info is shown
        if(mc.gameSettings.showDebugInfo) {
            return;
        }

        // Mask for RGB convertion. Done this way so that the alpha value isn't calculated for each line, since it doesn't change
        // For some reason the text renderer discards text when the alpha value is less than 27, so, don't bother doing the effect
        // if the alpha reaches that threshold (27 ~= 10.6%). Also, clamp the alpha value
        float alpha = opacityVal.getFloat();
        if(alpha < 0.106f) {
            hiddenRainbow();
            return;
        }
        else if(alpha > 1.0f) {
            alpha = 1.0f;
        }
        int mask = (int)(255 * alpha) << 24;

        // Find base hue
        float speedSlice = 1000.0f / speedVal.getFloat();
        float baseHue = (float)(System.currentTimeMillis() % (long)speedSlice) / speedSlice;

        // Find brightness and clamp it
        float brightness = brightnessVal.getFloat();
        if(brightness > 1.0f) {
            brightness = 1.0f;
        }
        else if(brightness < 0.0f) {
            brightness = 0.0f;
        }

        // Call respective rainbow method
        if(waveVal.getBoolean()) {
            waveRainbow(baseHue, brightness, mask);
        }
        else {
            basicRainbow(baseHue, brightness, mask);
        }
    }
}
