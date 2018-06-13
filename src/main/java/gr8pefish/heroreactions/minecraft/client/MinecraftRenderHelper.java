package gr8pefish.heroreactions.minecraft.client;

import gr8pefish.heroreactions.hero.data.FeedbackTypes;
import gr8pefish.heroreactions.hero.data.HeroUtils;
import gr8pefish.heroreactions.hero.data.HeroData;
import gr8pefish.heroreactions.minecraft.client.gui.GuiReactions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class MinecraftRenderHelper {

    //fields
    /**set by {@link HeroUtils#setStageSize()} during a viewer message event */
    public static double stageSize = -1;

    //time tracker
    private final int MAX_RANDOM_MS = 500;

    final double waitTime = ( //int?
            16
            + (
                500
                * (1 / (HeroData.FeedbackActivity.activity * 3)) //
                * (1 / (Math.max(stageSize, 0.1) * 5))
//                * (1 / (percentage * 5)) //ratio of each reaction to total
                * (0.6 + (Math.random() * 0.8))
                )
            );

//    public static void setReactionOpacity(long timeDifference) {
//
//        GlStateManager.enableAlpha(); //can cause weird transparent cutout issues, but positive affects performance (dependent on transparent pixel %) if no issues present
//        GlStateManager.enableBlend(); //enable blending
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA); //black magic that is necessary
//
//        //base opacity of 0 (fully transparent)
//        float opacity = 0f;
//        //add delta to total
//        GuiReactions.timestampOpacity += timeDifference;
//
//        //if over total time, reset
//        if (GuiReactions.timestampOpacity >= GuiReactions.maxFadeInTime) {
//            GuiReactions.timestampOpacity = 0d; //reset total //TODO: end spawning?
//        //otherwise set opacity
//        } else {
//            opacity = (float) MathHelper.clamp((GuiReactions.timestampOpacity / GuiReactions.maxFadeInTime) / 1d, 0, 1); //simply progress over lifespan ratio (clamp shouldn't theoretically be necessary)
//        }
////        System.out.println(opacity);
//
//        //set transparency
//        GlStateManager.color(1, 1, 1, opacity); //1=fully opaque, 0=fully transparent
//    }

    public static void applyOpacity(long timeDifference) {
        getReactionOverlay().setOpacity(timeDifference);
    }

    public static void applySize(long timeDifference) {
        getReactionOverlay().setSize(timeDifference);
        //TODO
    }

    public static void setReactionPosition(double currentTime, double totalTime) {
        //TODO
    }

    public static void renderFeedbackBubble(FeedbackTypes feedbackType) {
        getReactionOverlay().renderFeedbackBubbleOnly(feedbackType);
        //bind texture
        //TODO: How Do?
        //render in location
        //TODO
    }

    public static void renderViewCount(int viewCount) {
        //update the data to show
        ClientEventHandler.overlay.setViewCount(viewCount);
        //will automatically render new info next view tick
    }

    private static GuiReactions getReactionOverlay() {
        return ClientEventHandler.overlay.getReactions();
    }

//    if (timediff >= waitTime && percentage !== 0 && this.activity !== 0)
//    this.bubbleTimestamps[type] = timestamp; //emit
//    this.bubbleTimestamps[type] = timestamp + (Math.random() * MAX_RANDOM_MS); //pass

    //fade in


    //fade out


    //expand image


    //contract image


    //translate up


    //translate down


    //translate
}
