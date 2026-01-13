package com.ghostipedia.cosmiccore.common.reflection;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Translation key helper for the Reflection system.
 * All translatable strings should go through this class.
 *
 * Key structure:
 * - reflection.cosmiccore.bargain.[id].name - Bargain display name
 * - reflection.cosmiccore.bargain.[id].description - Bargain description
 * - reflection.cosmiccore.bargain.[id].dialogue.[n] - Offer dialogue lines
 * - reflection.cosmiccore.bargain.[id].question - The philosophical question
 * - reflection.cosmiccore.bargain.[id].answer.[answerId].text - Answer button text
 * - reflection.cosmiccore.bargain.[id].answer.[answerId].response - Reflection's response
 * - reflection.cosmiccore.bargain.[id].answer.[answerId].power.[n] - Power description lines
 * - reflection.cosmiccore.bargain.[id].answer.[answerId].drawback.[n] - Drawback description lines
 * - reflection.cosmiccore.bargain.[id].accept.[n] - Post-accept dialogue
 * - reflection.cosmiccore.bargain.[id].refuse.[n] - Refuse dialogue
 * - reflection.cosmiccore.bargain.[id].defy - Message when defying
 * - reflection.cosmiccore.bargain.[id].visual - Soul visual description
 *
 * - reflection.cosmiccore.threshold.[n].dialogue.[m] - Threshold dialogue lines
 * - reflection.cosmiccore.threshold.[n].question - Threshold question
 * - reflection.cosmiccore.threshold.[n].response - Acknowledge response
 *
 * - reflection.cosmiccore.ui.[key] - UI strings
 */
public class ReflectionLang {

    private static final String PREFIX = "reflection.cosmiccore.";

    // =========================================================================
    // Bargain Keys
    // =========================================================================

    public static MutableComponent bargainName(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".name");
    }

    public static MutableComponent bargainDescription(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".description");
    }

    public static MutableComponent bargainDialogue(String bargainId, int index) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".dialogue." + index);
    }

    public static MutableComponent bargainQuestion(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".question");
    }

    public static MutableComponent answerText(String bargainId, String answerId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".answer." + answerId + ".text");
    }

    public static MutableComponent answerResponse(String bargainId, String answerId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".answer." + answerId + ".response");
    }

    public static MutableComponent answerPower(String bargainId, String answerId, int index) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".answer." + answerId + ".power." + index);
    }

    public static MutableComponent answerDrawback(String bargainId, String answerId, int index) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".answer." + answerId + ".drawback." + index);
    }

    public static MutableComponent bargainAccept(String bargainId, int index) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".accept." + index);
    }

    public static MutableComponent bargainRefuse(String bargainId, int index) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".refuse." + index);
    }

    public static MutableComponent bargainOnAccept(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".on_accept");
    }

    public static MutableComponent bargainOnDefy(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".on_defy");
    }

    public static MutableComponent bargainSuffocation(String bargainId) {
        return Component.translatable(PREFIX + "bargain." + bargainId + ".suffocation");
    }

    // =========================================================================
    // Threshold Encounter Keys
    // =========================================================================

    public static MutableComponent thresholdDialogue(int thresholdIndex, int lineIndex) {
        return Component.translatable(PREFIX + "threshold." + thresholdIndex + ".dialogue." + lineIndex);
    }

    public static MutableComponent thresholdQuestion(int thresholdIndex) {
        return Component.translatable(PREFIX + "threshold." + thresholdIndex + ".question");
    }

    public static MutableComponent thresholdResponse(int thresholdIndex) {
        return Component.translatable(PREFIX + "threshold." + thresholdIndex + ".response");
    }

    // =========================================================================
    // UI Keys
    // =========================================================================

    public static MutableComponent ui(String key) {
        return Component.translatable(PREFIX + "ui." + key);
    }

    // Common UI strings
    public static MutableComponent uiYourBargains() {
        return ui("your_bargains");
    }

    public static MutableComponent uiAvailableBargains() {
        return ui("available_bargains");
    }

    public static MutableComponent uiDefiance() {
        return ui("defiance");
    }

    public static MutableComponent uiScrollUp() {
        return ui("scroll_up");
    }

    public static MutableComponent uiScrollDown() {
        return ui("scroll_down");
    }

    public static MutableComponent uiSoulErosion() {
        return ui("soul_erosion");
    }

    public static MutableComponent uiUnlockCost(int cost) {
        return Component.translatable(PREFIX + "ui.unlock_cost", cost);
    }

    public static MutableComponent uiDefianceCost(int cost) {
        return Component.translatable(PREFIX + "ui.defiance_cost", cost);
    }

    public static MutableComponent uiBack() {
        return ui("back");
    }

    public static MutableComponent uiContinue() {
        return ui("continue");
    }

    public static MutableComponent uiAcknowledge() {
        return ui("acknowledge");
    }

    public static MutableComponent uiReviewBargains(int count) {
        return Component.translatable(PREFIX + "ui.review_bargains", count);
    }

    public static MutableComponent uiBrowseBargains(int count) {
        return Component.translatable(PREFIX + "ui.browse_bargains", count);
    }

    public static MutableComponent uiGazeConstellation() {
        return ui("gaze_constellation");
    }

    public static MutableComponent uiJustLook() {
        return ui("just_look");
    }

    public static MutableComponent uiLeave() {
        return ui("leave");
    }

    public static MutableComponent uiForeverScarred() {
        return ui("forever_scarred");
    }

    public static MutableComponent uiClickToBargain() {
        return ui("click_to_bargain");
    }

    public static MutableComponent uiClickToDefy(int cost) {
        return Component.translatable(PREFIX + "ui.click_to_defy", cost);
    }

    public static MutableComponent uiPower() {
        return ui("power");
    }

    public static MutableComponent uiDrawback() {
        return ui("drawback");
    }

    public static MutableComponent uiCost() {
        return ui("cost");
    }

    // Hub dialogue responses
    public static MutableComponent hubReviewResponse() {
        return ui("hub.review_response");
    }

    public static MutableComponent hubBrowseResponse() {
        return ui("hub.browse_response");
    }

    public static MutableComponent hubReflectResponse() {
        return ui("hub.reflect_response");
    }

    public static MutableComponent hubLeaveResponse() {
        return ui("hub.leave_response");
    }

    // Defiance confirmation
    public static MutableComponent defianceConfirm() {
        return ui("defiance.confirm");
    }

    public static MutableComponent defianceCancel() {
        return ui("defiance.cancel");
    }

    public static MutableComponent defianceWarning1(String bargainName) {
        return Component.translatable(PREFIX + "ui.defiance.warning1", bargainName);
    }

    public static MutableComponent defianceWarning2(int cost) {
        return Component.translatable(PREFIX + "ui.defiance.warning2", cost);
    }

    public static MutableComponent defianceWarning3() {
        return ui("defiance.warning3");
    }

    public static MutableComponent defianceWarning4() {
        return ui("defiance.warning4");
    }

    // =========================================================================
    // Reflection Dialogue (Hub mode contextual greetings)
    // =========================================================================

    public static MutableComponent hubGreeting(String key) {
        return Component.translatable(PREFIX + "hub.greeting." + key);
    }

    // =========================================================================
    // Key generators for lang file generation
    // =========================================================================

    public static String keyBargainName(String bargainId) {
        return PREFIX + "bargain." + bargainId + ".name";
    }

    public static String keyBargainDescription(String bargainId) {
        return PREFIX + "bargain." + bargainId + ".description";
    }

    public static String keyBargainDialogue(String bargainId, int index) {
        return PREFIX + "bargain." + bargainId + ".dialogue." + index;
    }

    public static String keyBargainQuestion(String bargainId) {
        return PREFIX + "bargain." + bargainId + ".question";
    }

    public static String keyAnswerText(String bargainId, String answerId) {
        return PREFIX + "bargain." + bargainId + ".answer." + answerId + ".text";
    }

    public static String keyAnswerResponse(String bargainId, String answerId) {
        return PREFIX + "bargain." + bargainId + ".answer." + answerId + ".response";
    }

    public static String keyAnswerPower(String bargainId, String answerId, int index) {
        return PREFIX + "bargain." + bargainId + ".answer." + answerId + ".power." + index;
    }

    public static String keyAnswerDrawback(String bargainId, String answerId, int index) {
        return PREFIX + "bargain." + bargainId + ".answer." + answerId + ".drawback." + index;
    }

    public static String keyThresholdDialogue(int thresholdIndex, int lineIndex) {
        return PREFIX + "threshold." + thresholdIndex + ".dialogue." + lineIndex;
    }

    public static String keyThresholdQuestion(int thresholdIndex) {
        return PREFIX + "threshold." + thresholdIndex + ".question";
    }

    public static String keyThresholdResponse(int thresholdIndex) {
        return PREFIX + "threshold." + thresholdIndex + ".response";
    }

    public static String keyUi(String key) {
        return PREFIX + "ui." + key;
    }
}
