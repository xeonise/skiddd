package wtf.moonlight.features.modules.impl.visual;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.MoonLight;
import wtf.moonlight.events.annotations.EventTarget;
import wtf.moonlight.events.impl.misc.TickEvent;
import wtf.moonlight.events.impl.misc.WorldEvent;
import wtf.moonlight.events.impl.packet.PacketEvent;
import wtf.moonlight.events.impl.render.Render2DEvent;
import wtf.moonlight.events.impl.render.RenderGuiEvent;
import wtf.moonlight.events.impl.render.Shader2DEvent;
import wtf.moonlight.features.modules.Module;
import wtf.moonlight.features.modules.ModuleCategory;
import wtf.moonlight.features.modules.ModuleInfo;
import wtf.moonlight.features.modules.impl.combat.KillAura;
import wtf.moonlight.features.values.impl.*;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.utils.animations.Animation;
import wtf.moonlight.utils.animations.Direction;
import wtf.moonlight.utils.animations.Translate;
import wtf.moonlight.utils.animations.impl.DecelerateAnimation;
import wtf.moonlight.utils.player.MovementUtils;
import wtf.moonlight.utils.render.ColorUtils;
import wtf.moonlight.utils.render.RenderUtils;
import wtf.moonlight.utils.render.RoundedUtils;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

@ModuleInfo(name = "Interface", category = ModuleCategory.Visual)
public class Interface extends Module {
    public final TextValue cao = new TextValue("Client Name", "MoonLight", this);

    public final MultiBoolValue elements = new MultiBoolValue("Elements", Arrays.asList(
            new BoolValue("Watermark",true),
            new BoolValue("Module List",true),
            new BoolValue("Armor",true),
            new BoolValue("Info",true),
            new BoolValue("Health",true),
            new BoolValue("Potion HUD",true),
            new BoolValue("Target HUD",true),
            new BoolValue("Inventory",true),
            new BoolValue("Notification",true),
            new BoolValue("Session Info",true)
    ), this);

    public final BoolValue cFont = new BoolValue("C Fonts",true,this, () -> elements.isEnabled("Module List"));
    public final ModeValue fontMode = new ModeValue("C Fonts Mode", new String[]{"Bold","Semi Bold","Regular"}, "Semi Bold", this,() -> cFont.canDisplay() && cFont.get());
    public final ModeValue watemarkMode = new ModeValue("Watermark Mode", new String[]{"Text", "Styles","Nursultan","Exhi"}, "Text", this,() -> elements.isEnabled("Watermark"));
    public final ModeValue animation = new ModeValue("Animation", new String[]{"ScaleIn", "MoveIn","Slide In"}, "ScaleIn", this, () -> elements.isEnabled("Module List"));
    public final ModeValue arrayPosition = new ModeValue("Position", new String[]{"Right","Left"}, "Right", this, () -> elements.isEnabled("Module List"));
    public final SliderValue x = new SliderValue("Module List X", 0, -50, 50, this, () -> elements.isEnabled("Module List"));
    public final SliderValue y = new SliderValue("Module List Y", 0, -50, 50, this, () -> elements.isEnabled("Module List"));
    public final SliderValue textHeight = new SliderValue("Text Height", 9, 8, 12, this, () -> elements.isEnabled("Module List"));
    public final ModeValue tags = new ModeValue("Suffix", new String[]{"None", "Simple", "Bracket", "Dash"}, "None", this, () -> elements.isEnabled("Module List"));
    public final BoolValue background = new BoolValue("Background",true,this, () -> elements.isEnabled("Module List"));
    public final BoolValue line = new BoolValue("Line",true,this, () -> elements.isEnabled("Module List"));
    public final ModeValue armorMode = new ModeValue("Armor Mode", new String[]{"Default"}, "Default", this,() -> elements.isEnabled("Armor"));
    public final ModeValue infoMode = new ModeValue("Info Mode", new String[]{"Exhi"}, "Exhi", this,() -> elements.isEnabled("Info"));
    public final ModeValue potionHudMode = new ModeValue("Potion Mode", new String[]{"Default","Nursultan","Exhi"}, "Default", this);
    public final ModeValue targetHudMode = new ModeValue("TargetHUD Mode", new String[]{"Astolfo", "Type 1", "Type 2","Exhi"}, "Astolfo", this);
    public final ModeValue notificationMode = new ModeValue("Notification Mode", new String[]{"Default", "Test", "Test2","Exhi"}, "Default", this);
    public final ModeValue sessionInfoMode = new ModeValue("Session Info Mode", new String[]{"Default","Exhi","Rise"}, "Default", this,() -> elements.isEnabled("Session Info"));
    public final BoolValue centerNotif = new BoolValue("Center Notification",true,this,() -> notificationMode.is("Exhi"));
    public final ModeValue color = new ModeValue("Color Setting", new String[]{"Custom", "Rainbow", "Dynamic", "Fade","Astolfo"}, "Custom", this);
    private final ColorValue mainColor = new ColorValue("Main Color", new Color(128, 128, 255), this);
    private final ColorValue secondColor = new ColorValue("Second Color", new Color(128, 255, 255), this, () -> color.is("Fade"));
    public final SliderValue fadeSpeed = new SliderValue("Fade Speed", 1, 1, 10, 1, this, () -> color.is("Dynamic") || color.is("Fade"));
    public final ModeValue bgColor = new ModeValue("Background", new String[]{"Dark", "Synced"}, "Dark", this);
    public final BoolValue hideScoreRed = new BoolValue("Hide Scoreboard Red Points", true, this);
    public final BoolValue chatCombine = new BoolValue("Chat Combine", true, this);

    public final BoolValue cape = new BoolValue("Cape", true, this);
    public final BoolValue wavey = new BoolValue("Wavey Cape", true, this);
    public final BoolValue waveyTest = new BoolValue("Wavey Test", true, this,()-> cape.get() && wavey.get());
    public final BoolValue enchanted = new BoolValue("Enchanted", true, this, () -> cape.get() && !wavey.get());
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat xyzFormat = new DecimalFormat("0");
    private final DecimalFormat healthFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    public final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();
    public int lost = 0, killed = 0, won = 0;
    public int prevMatchKilled = 0,matchKilled = 0,match;
    private final Random random = new Random();

    @EventTarget
    public void onRender2D(Render2DEvent event) {

        if (watemarkMode.canDisplay()) {
            switch (watemarkMode.get()) {
                case "Text":
                    Fonts.interBold.get(30).drawStringWithShadow(cao.get(), 10, 10, color(0));
                    break;
                case "Styles":
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                    String dateString = dateFormat.format(new Date());

                    String name = " | " + MoonLight.INSTANCE.getVersion() +
                            EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + dateString +
                            EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + mc.thePlayer.getName() +
                            EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + mc.getCurrentServerData().serverIP;

                    int x = 7;
                    int y = 7;
                    int width = Fonts.interBold.get(17).getStringWidth("ML") + Fonts.interRegular.get(17).getStringWidth(name) + 5;
                    int height = Fonts.interRegular.get(17).getHeight() + 3;

                    RoundedUtils.drawRound(x, y, width, height, 4, new Color(bgColor()));
                    Fonts.interBold.get(17).drawOutlinedString("ML", x + 2, y + 4.5f, -1, color());
                    Fonts.interRegular.get(17).drawStringWithShadow(name, Fonts.interBold.get(17).getStringWidth("ML") + x + 2, y + 4.5f, -1);
                    break;
                case "Nursultan":
                    RoundedUtils.drawRound(7, 7.5f, 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 5, 15, 4, new Color(17, 17, 17, 215));
                    Fonts.nursultan.get(16).drawString("P", 13, 14, color(0));
                    RenderUtils.drawRect(25, 10.5f, 1, 8.5f, new Color(47, 47, 47).getRGB());
                    Fonts.interMedium.get(15).drawString(INSTANCE.getVersion(), 29, 13, color(0));

                    RenderUtils.drawRect(7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 15, 10.5f, 1, 8.5f, new Color(47, 47, 47).getRGB());
                    RoundedUtils.drawRound(7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11, 7.5f, Fonts.interMedium.get(15).getStringWidth("user") + 25, 15, 4, new Color(18, 18, 18, 215));
                    Fonts.nursultan.get(16).drawString("W", 7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 5, 14, color(0));
                    Fonts.interMedium.get(15).drawString("user", 7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 15 + 5, 13, -1);
                    break;
                case "Exhi":
                    boolean shouldChange = RenderUtils.COLOR_PATTERN.matcher(cao.get()).find();
                    String text = shouldChange ? "§r" + cao.get() : cao.get().charAt(0) + "§r§f" + cao.get().substring(1) +
                            "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r " + "§7[§f" +
                            mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + "ms§7]§r ";
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, color());
                    break;
            }
        }

        if (infoMode.canDisplay()) {
            switch (infoMode.get()) {
                case "Exhi":
                    float textY = (event.getScaledResolution().getScaledHeight() - 9) + (mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f);
                    mc.fontRendererObj.drawStringWithShadow("XYZ: " + EnumChatFormatting.WHITE +
                                    xyzFormat.format(mc.thePlayer.posX) + " " +
                                    xyzFormat.format(mc.thePlayer.posY) + " " +
                                    xyzFormat.format(mc.thePlayer.posZ) + " " + EnumChatFormatting.RESET + "BPS: " + EnumChatFormatting.WHITE + this.bpsFormat.format(MovementUtils.getBPS())
                            , 2, textY, color(0));
                    break;
            }
        }

        if (armorMode.canDisplay()) {
            switch (armorMode.get()) {
                case "Default":
                    ArrayList<ItemStack> stuff = new ArrayList<>();
                    boolean onWater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
                    int split = -3;
                    for (int index = 3; index >= 0; --index) {
                        ItemStack armor = mc.thePlayer.inventory.armorInventory[index];
                        if (armor == null) continue;
                        stuff.add(armor);
                    }
                    if (mc.thePlayer.getCurrentEquippedItem() != null) {
                        stuff.add(mc.thePlayer.getCurrentEquippedItem());
                    }
                    for (ItemStack everything : stuff) {
                        split += 16;
                        RenderUtils.renderItemStack(everything, split + (double) event.getScaledResolution().getScaledWidth() / 2 - 4, event.getScaledResolution().getScaledHeight() - (onWater ? 65 : 55) + (mc.thePlayer.capabilities.isCreativeMode ? 14 : 0), 1, true, 0.5f);
                    }
                    break;
            }
        }

        if (elements.isEnabled("Module List")) {
            int count = 1;
            int screenWidth = event.getScaledResolution().getScaledWidth();
            float y = ((arrayPosition.is("Right") ? 2 : 12) + this.y.get());
            Comparator<Module> sort = (m1, m2) -> {
                double ab = cFont.get() ? getFr().getStringWidth(m1.getName() + m1.getTag()) : mc.fontRendererObj.getStringWidth(m1.getName() + m1.getTag());
                double bb = cFont.get() ? getFr().getStringWidth(m2.getName() + m2.getTag()) : mc.fontRendererObj.getStringWidth(m2.getName() + m2.getTag());
                return Double.compare(bb, ab);
            };
            ArrayList<Module> enabledMods = new ArrayList<>(INSTANCE.getModuleManager().getModules());

            if (animation.is("Slide In")) {
                enabledMods.sort(sort);
                for (Module module : enabledMods) {
                    if (module.isHidden())
                        continue;
                    Translate translate = module.getTranslate();
                    float moduleWidth = cFont.get() ? getFr().getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());
                    if (arrayPosition.is("Right")) {
                        if (module.isEnabled() && !module.isHidden()) {
                            translate.translate((screenWidth - moduleWidth - 1.0f) + this.x.get(), y);
                            y += (int) textHeight.get();
                        } else {
                            translate.animate((screenWidth - 1) + this.x.get(), -25.0);
                        }
                    } else if (module.isEnabled() && !module.isHidden()) {
                        translate.translate((2.0f) + this.x.get(), y);
                        y += (int) textHeight.get();
                    } else {
                        translate.animate((-moduleWidth) + this.x.get(), -25.0);
                    }
                    if (translate.getX() >= screenWidth) {
                        continue;
                    }

                    if (background.get()) {
                        if (cFont.get()) {
                            RenderUtils.drawRect((float) translate.getX(), (float) translate.getY() - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                        } else {
                            RenderUtils.drawRect((float) translate.getX(), (float) translate.getY() - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                        }
                    }

                    if (line.get()) {
                        if (cFont.get()) {
                            RenderUtils.drawRect((float) (translate.getX() + moduleWidth) + 1, (float) (translate.getY() - 0.5f), 1, textHeight.get(), color(count));
                        } else {
                            RenderUtils.drawRect((float) (translate.getX() + moduleWidth) + 1, (float) (translate.getY() - 0.5f), 1, textHeight.get(), color(count));
                        }
                    }

                    if (cFont.get()) {
                        getFr().drawStringWithShadow(module.getName() + module.getTag(), (float) translate.getX(), (float) translate.getY() - 1, color(count));
                    } else {
                        mc.fontRendererObj.drawStringWithShadow(module.getName() + module.getTag(), (float) translate.getX(), (float) translate.getY() - 1, color(count));
                    }

                    count -= 1;
                }
            }

            if (!animation.is("Slide In")) {
                enabledMods.sort(sort);
                for (Module module : enabledMods) {
                    if (module.isHidden())
                        continue;
                    Animation moduleAnimation = module.getAnimation();
                    moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                    if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
                    float moduleWidth = cFont.get() ? getFr().getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());
                    float x = (arrayPosition.is("Right") ? screenWidth - moduleWidth - 1.0f : 2) + this.x.get();
                    float alphaAnimation = 1.0f;

                    switch (animation.get()) {
                        case "MoveIn": {
                            x += (float) Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + moduleWidth));
                            break;
                        }
                        case "ScaleIn": {
                            RenderUtils.scaleStart(x + (moduleWidth / 2.0f), y + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
                            alphaAnimation = (float) moduleAnimation.getOutput();
                        }
                    }

                    if (background.get()) {
                        if (cFont.get()) {
                            RenderUtils.drawRect(x, y - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                        } else {
                            RenderUtils.drawRect(x, y - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                        }
                    }

                    if (line.get()) {
                        if (cFont.get()) {
                            RenderUtils.drawRect(x + moduleWidth + 1, y - 0.5f, 1, textHeight.get(), color(count));
                        } else {
                            RenderUtils.drawRect(x + moduleWidth + 1, y - 0.5f, 1, textHeight.get(), color(count));
                        }
                    }

                    if (cFont.get()) {
                        getFr().drawStringWithShadow(module.getName() + module.getTag(), x, y, ColorUtils.applyOpacity(color(count), alphaAnimation));
                    } else {
                        mc.fontRendererObj.drawStringWithShadow(module.getName() + module.getTag(), x, y, ColorUtils.applyOpacity(color(count), alphaAnimation));
                    }

                    if (animation.get().equals("ScaleIn")) {
                        RenderUtils.scaleEnd();
                    }

                    y += (float) (moduleAnimation.getOutput() * textHeight.get());
                    count -= 2;
                }
            }
        }

        if (elements.isEnabled("Potion HUD") && potionHudMode.is("Exhi")) {
            ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
            potions.sort(Comparator.comparingDouble(effect -> -mc.fontRendererObj.getStringWidth(I18n.format(Potion.potionTypes[effect.getPotionID()].getName()))));
            float y = mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f;
            for (PotionEffect potionEffect : potions) {
                Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
                String potionName = I18n.format(potionType.getName());
                String type = "";
                if (potionEffect.getAmplifier() == 1) {
                    potionName = potionName + " II";
                } else if (potionEffect.getAmplifier() == 2) {
                    potionName = potionName + " III";
                } else if (potionEffect.getAmplifier() == 3) {
                    potionName = potionName + " IV";
                }
                if (potionEffect.getDuration() < 600 && potionEffect.getDuration() > 300) {
                    type = type + " §6" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() < 300) {
                    type = type + " §c" + Potion.getDurationString(potionEffect);
                } else if (potionEffect.getDuration() > 600) {
                    type = type + " §7" + Potion.getDurationString(potionEffect);
                }
                GlStateManager.pushMatrix();
                mc.fontRendererObj.drawString(potionName, (float) event.getScaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(type + potionName) - 2.0f, (event.getScaledResolution().getScaledHeight() - 9) + y, new Color(potionType.getLiquidColor()).getRGB(), true);
                mc.fontRendererObj.drawString(type, (float) event.getScaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(type) - 2.0f, (event.getScaledResolution().getScaledHeight() - 9) + y, new Color(255, 255, 255).getRGB(), true);

                GlStateManager.popMatrix();
                y -= 9.0f;
            }
        }

        if(elements.isEnabled("Health")){
            renderHealth();
        }

        if (elements.isEnabled("Session Info") && potionHudMode.is("Exhi")) {
            mc.fontRendererObj.drawStringWithShadow(RenderUtils.sessionTime(), event.getScaledResolution().getScaledWidth() / 2.0f - mc.fontRendererObj.getStringWidth(RenderUtils.sessionTime()) / 2.0f,BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? 47 : 30.0f, -1);
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {

        switch (watemarkMode.get()) {
            case "Text":
                Fonts.interBold.get(30).drawStringWithShadow(cao.get(), 10, 10, color(0));
                break;
            case "Styles":
                DateFormat dateFormat = new SimpleDateFormat("hh:mm");
                String dateString = dateFormat.format(new Date());

                String name = " | " + MoonLight.INSTANCE.getVersion() +
                        EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + dateString +
                        EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + mc.thePlayer.getName() +
                        EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + mc.getCurrentServerData().serverIP;

                int x = 7;
                int y = 7;
                int width = Fonts.interBold.get(17).getStringWidth("ML") + Fonts.interRegular.get(17).getStringWidth(name) + 5;
                int height = Fonts.interRegular.get(17).getHeight() + 3;

                RoundedUtils.drawRound(x, y, width, height, 4, new Color(color()));
                break;
        }

        if (elements.isEnabled("Module List")) {
            int count = 1;
            int screenWidth = new ScaledResolution(mc).getScaledWidth();
            float y = ((arrayPosition.is("Right") ? 2 : 12) + this.y.get());
            Comparator<Module> sort = (m1, m2) -> {
                double ab = cFont.get() ? getFr().getStringWidth(m1.getName() + m1.getTag()) : mc.fontRendererObj.getStringWidth(m1.getName() + m1.getTag());
                double bb = cFont.get() ? getFr().getStringWidth(m2.getName() + m2.getTag()) : mc.fontRendererObj.getStringWidth(m2.getName() + m2.getTag());
                return Double.compare(bb, ab);
            };
            ArrayList<Module> enabledMods = new ArrayList<>(INSTANCE.getModuleManager().getModules());

            if (animation.is("Slide In")) {
                enabledMods.sort(sort);
                for (Module module : enabledMods) {
                    if (module.isHidden())
                        continue;
                    Translate translate = module.getTranslate();
                    float moduleWidth = cFont.get() ? getFr().getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());
                    if (arrayPosition.is("Right")) {
                        if (module.isEnabled() && !module.isHidden()) {
                            translate.translate((screenWidth - moduleWidth - 1.0f) + this.x.get(), y);
                            y += (int) textHeight.get();
                        } else {
                            translate.animate((screenWidth - 1) + this.x.get(), -25.0);
                        }
                    } else if (module.isEnabled() && !module.isHidden()) {
                        translate.translate((2.0f) + this.x.get(), y);
                        y += (int) textHeight.get();
                    } else {
                        translate.animate((-moduleWidth) + this.x.get(), -25.0);
                    }
                    if (translate.getX() >= screenWidth) {
                        continue;
                    }

                    if (background.get()) {
                        if(event.getShaderType() == Shader2DEvent.ShaderType.BLUR || event.getShaderType() == Shader2DEvent.ShaderType.SHADOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth), (float) translate.getY() - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                            } else {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth), (float) translate.getY() - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                            }
                        }
                        if(event.getShaderType() == Shader2DEvent.ShaderType.GLOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth), (float) translate.getY() - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), color(count,0.2f));
                            } else {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth), (float) translate.getY() - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), color(count,0.2f));
                            }
                        }
                    }

                    if (line.get()) {
                        if(event.getShaderType() == Shader2DEvent.ShaderType.GLOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth) + moduleWidth + 1, (float) translate.getY() - 0.5f, 1, textHeight.get(), color(count));
                            } else {
                                RenderUtils.drawRect((float) (translate.getX() + moduleWidth) + moduleWidth + 1, (float) translate.getY() - 0.5f, 1, textHeight.get(), color(count));
                            }
                        }
                    }

                    count -= 1;
                }
            }

            if (!animation.is("Slide In")) {
                enabledMods.sort(sort);
                for (Module module : enabledMods) {
                    if (module.isHidden())
                        continue;
                    Animation moduleAnimation = module.getAnimation();
                    moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                    if (!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
                    float moduleWidth = cFont.get() ? getFr().getStringWidth(module.getName() + module.getTag()) : mc.fontRendererObj.getStringWidth(module.getName() + module.getTag());
                    float x = (arrayPosition.is("Right") ? screenWidth - moduleWidth - 1.0f : 2) + this.x.get();

                    switch (animation.get()) {
                        case "MoveIn": {
                            x += (float) Math.abs((moduleAnimation.getOutput() - 1.0) * (2.0 + moduleWidth));
                            break;
                        }
                        case "ScaleIn": {
                            RenderUtils.scaleStart(x + (moduleWidth / 2.0f), y + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput());
                        }
                    }

                    if (background.get()) {
                        if(event.getShaderType() == Shader2DEvent.ShaderType.BLUR || event.getShaderType() == Shader2DEvent.ShaderType.SHADOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect(x, y - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                            } else {
                                RenderUtils.drawRect(x, y - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), bgColor(count));
                            }
                        }
                        if(event.getShaderType() == Shader2DEvent.ShaderType.GLOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect(x, y - 0.5f, getFr().getStringWidth(module.getName() + module.getTag()), textHeight.get(), color(count));
                            } else {
                                RenderUtils.drawRect(x, y - 0.5f, mc.fontRendererObj.getStringWidth(module.getName() + module.getTag()), textHeight.get(), color(count));
                            }
                        }
                    }

                    if (line.get()) {
                        if(event.getShaderType() == Shader2DEvent.ShaderType.GLOW) {
                            if (cFont.get()) {
                                RenderUtils.drawRect(x + moduleWidth + 1, y - 0.5f, 1, textHeight.get(), color(count));
                            } else {
                                RenderUtils.drawRect(x + moduleWidth + 1, y - 0.5f, 1, textHeight.get(), color(count));
                            }
                        }
                    }

                    if (animation.get().equals("ScaleIn")) {
                        RenderUtils.scaleEnd();
                    }

                    y += (float) (moduleAnimation.getOutput() * textHeight.get());
                    count -= 2;
                }
            }
        }
    }

    @EventTarget
    public void onRenderGui(RenderGuiEvent event){
        if(elements.isEnabled("Health")) {
            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiContainerCreative) {
                renderHealth();
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        mainColor.setRainbow(color.is("Rainbow"));
        KillAura aura = getModule(KillAura.class);
        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        }
        if (!aura.isEnabled() && !(mc.currentScreen instanceof GuiChat)) {
            Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
                DecelerateAnimation animation = entry.getValue();

                animation.setDirection(Direction.BACKWARDS);
                if (animation.finished(Direction.BACKWARDS)) {
                    iterator.remove();
                }
            }
        }
        if (aura.targets != null && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.putIfAbsent((EntityPlayer) entity, new DecelerateAnimation(175, 1));
                    animationEntityPlayerMap.get(entity).setDirection(Direction.FORWARDS);
                }
            }
        }
        if (aura.isEnabled() && aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
            Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
                DecelerateAnimation animation = entry.getValue();

                animation.setDirection(Direction.BACKWARDS);
                if (animation.finished(Direction.BACKWARDS)) {
                    iterator.remove();
                }
            }
        }
        if (mc.currentScreen instanceof GuiChat) {
            animationEntityPlayerMap.putIfAbsent(mc.thePlayer, new DecelerateAnimation(175, 1));
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.FORWARDS);
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event){
        prevMatchKilled = matchKilled;
        matchKilled = 0;
        match += 1;

        if(match > 6)
            match = 6;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02 = (S02PacketChat) event.getPacket();
            String xd = s02.getChatComponent().getUnformattedText();
            if (xd.contains("was killed by " + mc.thePlayer.getName())) {
                ++this.killed;
                prevMatchKilled = matchKilled;
                ++matchKilled;
            }

            if (xd.contains("You Died! Want to play again?")) {
                ++lost;
            }
        }

        if (packet instanceof S45PacketTitle && ((S45PacketTitle) packet).getType().equals(S45PacketTitle.Type.TITLE)) {
            String unformattedText = ((S45PacketTitle) packet).getMessage().getUnformattedText();
            if (unformattedText.contains("VICTORY!")) {
                ++this.won;
            }
            if (unformattedText.contains("GAME OVER!") || unformattedText.contains("DEFEAT!") || unformattedText.contains("YOU DIED!")) {
                ++this.lost;
            }
        }
    }

    public void renderHealth(){
        ScaledResolution sr = new ScaledResolution(mc);
        int xWidth = 0;
        GuiScreen screen = mc.currentScreen;
        float absorptionHealth = mc.thePlayer.getAbsorptionAmount();
        String string = this.healthFormat.format(mc.thePlayer.getHealth() / 2.0f) + "§c\u2764 " + (absorptionHealth <= 0.0f ? "" : "§e" + this.healthFormat.format(absorptionHealth / 2.0f) + "§6\u2764");
        int offsetY = 0;
        if (mc.thePlayer.getHealth() >= 0.0f && mc.thePlayer.getHealth() < 10.0f || mc.thePlayer.getHealth() >= 10.0f && mc.thePlayer.getHealth() < 100.0f) {
            xWidth = 3;
        }
        if (screen instanceof GuiInventory) {
            offsetY = 70;
        } else if (screen instanceof GuiContainerCreative) {
            offsetY = 80;
        } else if (screen instanceof GuiChest) {
            offsetY = ((GuiChest)screen).ySize / 2 - 15;
        }
        int x = new ScaledResolution(mc).getScaledWidth() / 2 - xWidth;
        int y = new ScaledResolution(mc).getScaledHeight() / 2 + 25 + offsetY;
        Color color = new Color(ColorUtils.getHealthColor(mc.thePlayer));
        mc.fontRendererObj.drawString(string, absorptionHealth > 0.0f ? x - 15.5f : x - 3.5f, y, color.getRGB(), true);
        GL11.glPushMatrix();
        mc.getTextureManager().bindTexture(Gui.icons);
        random.setSeed(mc.ingameGUI.getUpdateCounter() * 312871L);
        float width = sr.getScaledWidth() / 2.0f - mc.thePlayer.getMaxHealth() / 2.5f * 10.0f / 2.0f;
        float maxHealth = mc.thePlayer.getMaxHealth();
        int lastPlayerHealth = mc.ingameGUI.lastPlayerHealth;
        int healthInt = MathHelper.ceiling_float_int(mc.thePlayer.getHealth());
        int l2 = -1;
        boolean flag = mc.ingameGUI.healthUpdateCounter > (long) mc.ingameGUI.getUpdateCounter() && (mc.ingameGUI.healthUpdateCounter - (long) mc.ingameGUI.getUpdateCounter()) / 3L % 2L == 1L;
        if (mc.thePlayer.isPotionActive(Potion.regeneration)) {
            l2 = mc.ingameGUI.getUpdateCounter() % MathHelper.ceiling_float_int(maxHealth + 5.0f);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i6 = MathHelper.ceiling_float_int(maxHealth / 2.0f) - 1; i6 >= 0; --i6) {
            int xOffset = 16;
            if (mc.thePlayer.isPotionActive(Potion.poison)) {
                xOffset += 36;
            } else if (mc.thePlayer.isPotionActive(Potion.wither)) {
                xOffset += 72;
            }
            int k3 = 0;
            if (flag) {
                k3 = 1;
            }
            float renX = width + (float)(i6 % 10 * 8);
            float renY = (float)sr.getScaledHeight() / 2.0f + 15.0f + (float)offsetY;
            if (healthInt <= 4) {
                renY += (float)random.nextInt(2);
            }
            if (i6 == l2) {
                renY -= 2.0f;
            }
            int yOffset = 0;
            if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                yOffset = 5;
            }
            Gui.drawTexturedModalRect(renX, renY, 16 + k3 * 9, 9 * yOffset, 9, 9);
            if (flag) {
                if (i6 * 2 + 1 < lastPlayerHealth) {
                    Gui.drawTexturedModalRect(renX, renY, xOffset + 54, 9 * yOffset, 9, 9);
                }
                if (i6 * 2 + 1 == lastPlayerHealth) {
                    Gui.drawTexturedModalRect(renX, renY, xOffset + 63, 9 * yOffset, 9, 9);
                }
            }
            if (i6 * 2 + 1 < healthInt) {
                Gui.drawTexturedModalRect(renX, renY, xOffset + 36, 9 * yOffset, 9, 9);
            }
            if (i6 * 2 + 1 != healthInt) continue;
            Gui.drawTexturedModalRect(renX, renY, xOffset + 45, 9 * yOffset, 9, 9);
        }
        GL11.glPopMatrix();
    }

    public FontRenderer getFr() {

        FontRenderer fr = null;
        switch (fontMode.get()) {
            case "Bold":
                fr = Fonts.interBold.get(15);
                break;

            case "Semi Bold":
                fr = Fonts.interSemiBold.get(15);
                break;

            case "Regular":
                fr = Fonts.interRegular.get(15);
                break;
        }

        return fr;
    }

    public Color getMainColor() {
        return mainColor.get();
    }

    public Color getSecondColor() {
        return secondColor.get();
    }

    public int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }
    public static int astolfoRainbow(final int offset, final float saturation, final float brightness) {
        double currentColor = Math.ceil((double)(System.currentTimeMillis() + offset * 130L)) / 6.0;
        return Color.getHSBColor(((float)((currentColor %= 360.0) / 360.0) < 0.5) ? (-(float)(currentColor / 360.0)) : ((float)(currentColor / 360.0)), saturation, brightness).getRGB();
    }

    public float[] getRainbowHSB(int counter) {
        final int width = 20;

        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;

        float hue = (float) (rainbowState / 360);
        float saturation = mainColor.getSaturation();
        float brightness = mainColor.getBrightness();

        return new float[]{hue, saturation, brightness};
    }

    public int color() {
        return color(0);
    }


    public int color(int counter, float alpha) {
        int colors = getMainColor().getRGB();
        switch (color.get()) {
            case "Rainbow":
                colors = ColorUtils.applyOpacity(getRainbow(counter), alpha);
                break;
            case "Dynamic":
                colors = ColorUtils.applyOpacity(ColorUtils.colorSwitch(getMainColor(), new Color(ColorUtils.darker(getMainColor().getRGB(), 0.25F)), 2000.0F, counter, 75L, fadeSpeed.get()).getRGB(), alpha);
                break;
            case "Fade":
                colors = ColorUtils.applyOpacity((ColorUtils.colorSwitch(getMainColor(), getSecondColor(), 2000.0F, counter, 75L, fadeSpeed.get()).getRGB()), alpha);
                break;
            case "Astolfo":
                colors = astolfoRainbow(0,mainColor.getSaturation(),mainColor.getBrightness());
                break;
        }
        return colors;
    }

    public int color(int counter) {
        return color(counter, 1);
    }

    public int bgColor(int counter, float opacity) {
        int colors = getMainColor().getRGB();
        switch (bgColor.get()) {
            case "Dark":
                colors = (new Color(17, 17, 17, 215).getRGB());
                break;
            case "Synced":
                colors = new Color(ColorUtils.darker(color(counter,opacity),0.2f)).getRGB();
                break;
            case "None":
                colors = new Color(0, 0, 0, 0).getRGB();
                break;
        }
        return colors;
    }
    public int bgColor(int counter) {
        return bgColor(counter, 1);
    }

    public int bgColor() {
        return bgColor(0);
    }
}