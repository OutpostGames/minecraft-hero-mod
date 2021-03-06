package gr8pefish.heroreactions.minecraft.client;

import gr8pefish.heroreactions.hero.data.HeroData;
import gr8pefish.heroreactions.hero.network.websocket.WebSocketClient;
import gr8pefish.heroreactions.minecraft.client.gui.login.GuiButtonLogin;
import gr8pefish.heroreactions.minecraft.client.gui.login.GuiLogin;
import gr8pefish.heroreactions.minecraft.client.gui.overlay.GuiIngameOverlay;
import gr8pefish.heroreactions.minecraft.client.gui.overlay.GuiLocations;
import gr8pefish.heroreactions.minecraft.config.ConfigHandler;
import gr8pefish.heroreactions.minecraft.lib.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

/**
 * Deals with client code, specifically the main logic loop of rendering the overlay and the keybinding handler to toggle the overlay.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {

    //Main overlay rendering code

    //The overlay to render (one instance, with internal data changed depending)
    public static final GuiIngameOverlay overlay = new GuiIngameOverlay(Minecraft.getMinecraft());

    @SubscribeEvent
    public void onRenderOverlayGUI(RenderGameOverlayEvent.Text event) { //can do pre/post also //TODO: Ensure correct event
        if (ConfigHandler.generalConfigSettings.enableOverlay && WebSocketClient.isConnected()) {

            //Don't show anything unless the stream is online
            if (!HeroData.Online.isOnline) return;

            //Scale the rendering location data to fit current screen size
            GuiLocations.applyPositionScaling(overlay.getGuiLocation(), event.getResolution());

            //"reset" GL states (just in case)
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            //Render the overlay
            overlay.renderOverlay(event.getResolution());
        }
    }

    //Counter for rendering popup URL

    private static int tickCounter;
    private static int secondCounter; //20 ticks in a game second
    private static int displayStartTime = ConfigHandler.overlayConfigSettings.urlPopupSpacing;
    private static int displayEndTime = displayStartTime + ConfigHandler.overlayConfigSettings.urlPopupDuration;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        //increment tick counter
        tickCounter++;

        //increment second counter
        if (tickCounter >= 20) {
            secondCounter++;
            tickCounter = 0;
        }

        //reset if over total time
        if (secondCounter >= displayEndTime) {
            overlay.setRenderPopupURL(false);
            secondCounter = 0;
        //otherwise, check if should draw info
        } else if (secondCounter >= displayStartTime) {
            overlay.setRenderPopupURL(true);
        }

    }

    //Keybinding for toggling the overlay

    public static final KeyBinding KEY_TOGGLE_OVERLAY = new KeyBinding("key." + ModInfo.MODID + ".toggle", KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_H, ModInfo.MOD_NAME);

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent event) {
        if (KEY_TOGGLE_OVERLAY.isPressed())
            ConfigHandler.generalConfigSettings.enableOverlay = !ConfigHandler.generalConfigSettings.enableOverlay; //client side only is fine, no need to send info to the server
    }

    // Login UI flow

    private static final int LOGIN_BUTTON_ID = 27; //TODO: ensure no duplicate

    @SubscribeEvent
    public static void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen currentGui = event.getGui();
        if (currentGui instanceof GuiMainMenu) {
            int x = currentGui.width / 2 + 104;
            int y = currentGui.height / 4 + 48 + 72 + 12;
            event.getButtonList().add(new GuiButtonLogin(LOGIN_BUTTON_ID, x, y)); //place to the right of cancel, mirroring lang
        }
        if (currentGui instanceof GuiOptions) {
            int i = 1; //oh vanilla code (these ridiculous numbers taken from it)
            int x = currentGui.width / 2 - 155 + i % 2 * 160 + 155;
            int y = currentGui.height / 6 - 12 + 24 * (i >> 1);
            event.getButtonList().add(new GuiButtonLogin(LOGIN_BUTTON_ID, x, y)); //place to the right of realms notifications, out of the way

        }
    }

    @SubscribeEvent
    public static void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiOptions) {
            if (event.getButton().id == LOGIN_BUTTON_ID) {
                event.getGui().mc.displayGuiScreen(new GuiLogin(event.getGui()));
            }
        }
    }


}
