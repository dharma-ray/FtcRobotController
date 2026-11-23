package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import org.firstinspires.ftc.teamcode.Prism.Color;
import org.firstinspires.ftc.teamcode.Prism.GoBildaPrismDriver;
import org.firstinspires.ftc.teamcode.Prism.PrismAnimations;

/**
 * Bear face on the 14 x 13 zig-zag LED grid (182 LEDs) driven by the goBILDA Prism.
 * Grizzly face normally; Fury face while the ball sensor sees the ball.
 *
 * LED index map (zig-zag): row r, column c (0-based, c from left)
 *   even rows: index = r*14 + c
 *   odd rows:  index = r*14 + (13 - c)
 *
 * The Prism allows 10 layers, so each face is simplified. Layer 0 is the bottom and
 * higher layers are assumed to draw on top (check with "Prism: Face Test", button A).
 */
public class LedController {

    public static final int ROW_LENGTH = 14;
    public static final int ROWS = 13;
    public static final int STRIP_LENGTH = ROW_LENGTH * ROWS; // 182

    // ---- Colors (tweak here) ----
    static final int[] COAT   = {90, 40, 0};     // brown
    static final int[] MUZZLE = {255, 112, 80};  // pink
    static final int[] BROW   = {20, 8, 0};      // very dim brown ("unlit" brow)
    static final int[] EYES   = {255, 176, 64};  // warm tan glow
    static final int[] NOSE   = {20, 8, 0};      // very dim
    static final int[] WHITE  = {255, 255, 255}; // Fury eyes
    static final int[] AMBER  = {255, 128, 0};   // Fury mouth

    static final int BRIGHTNESS = 50;      // 0-100
    static final int TEST_BRIGHTNESS = 30; // 0-100

    private final Telemetry telemetry;
    private GoBildaPrismDriver prism;
    private DistanceSensor ballSensor;
    private boolean ballHeld = false;
    private Boolean faceShown = null; // null = nothing drawn yet, else the ballHeld value drawn

    public LedController(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    /** Finds the Prism and the ball sensor in the hardware config. Call once, before waitForStart(). */
    public void setLeds(HardwareMap hardwareMap) {
        prism = hardwareMap.get(GoBildaPrismDriver.class, "prism");
        ballSensor = hardwareMap.get(DistanceSensor.class, "ball_sensor");
        prism.setStripLength(STRIP_LENGTH);
    }

    /** Checks the ball sensor: Fury face while the ball is held, otherwise Grizzly face. */
    public void enableLeds() {
        double cm = ballSensor.getDistance(DistanceUnit.CM);
        ballHeld = cm < (ballHeld ? 6.0 : 4.0);  // caught under 4 cm, released over 6 cm

        // Only redraw when the mood changes, so we don't flood the I2C bus every loop.
        if (faceShown == null || faceShown != ballHeld) {
            if (ballHeld) showFury(); else showGrizzly();
            faceShown = ballHeld;
        }

        telemetry.addData("Ball distance", "%.1f cm", cm);
        telemetry.addData("Mood", ballHeld ? "FURY" : "GRIZZLY");
    }

    /** Calm bear: dim brows, tan eyes, pink mouth. */
    public void showGrizzly() {
        prism.clearAllAnimations();
        add(GoBildaPrismDriver.LayerHeight.LAYER_0, COAT,     0, 181); // whole head
        add(GoBildaPrismDriver.LayerHeight.LAYER_1, MUZZLE,  56, 152); // muzzle frame rows 5-11
        add(GoBildaPrismDriver.LayerHeight.LAYER_2, BROW,    30,  32); // left brow
        add(GoBildaPrismDriver.LayerHeight.LAYER_3, BROW,    37,  39); // right brow
        add(GoBildaPrismDriver.LayerHeight.LAYER_4, EYES,    45,  45); // right eye
        add(GoBildaPrismDriver.LayerHeight.LAYER_5, EYES,    52,  52); // left eye
        add(GoBildaPrismDriver.LayerHeight.LAYER_6, NOSE,    62,  63); // nose
        add(GoBildaPrismDriver.LayerHeight.LAYER_7, MUZZLE, 158, 163); // mouth
    }

    /** Angry bear: white slanted eyes, amber snarl. */
    public void showFury() {
        prism.clearAllAnimations();
        add(GoBildaPrismDriver.LayerHeight.LAYER_0, COAT,     0, 181); // whole head
        add(GoBildaPrismDriver.LayerHeight.LAYER_1, MUZZLE,  56, 152); // muzzle frame rows 5-11
        add(GoBildaPrismDriver.LayerHeight.LAYER_2, WHITE,   30,  30); // left eye, outer (row 3)
        add(GoBildaPrismDriver.LayerHeight.LAYER_3, WHITE,   39,  39); // right eye, outer (row 3)
        add(GoBildaPrismDriver.LayerHeight.LAYER_4, WHITE,   45,  46); // right eye, inner (row 4)
        add(GoBildaPrismDriver.LayerHeight.LAYER_5, WHITE,   51,  52); // left eye, inner (row 4)
        add(GoBildaPrismDriver.LayerHeight.LAYER_6, NOSE,    62,  63); // nose
        add(GoBildaPrismDriver.LayerHeight.LAYER_7, MUZZLE, 158, 163); // mouth edge
        add(GoBildaPrismDriver.LayerHeight.LAYER_8, AMBER,  159, 162); // snarl
    }

    /** Saves the Grizzly face to Artboard 0 and makes it the face shown at power-on. */
    public void saveGrizzlyAsBootFace() {
        showGrizzly();
        prism.saveCurrentAnimationsToArtboard(GoBildaPrismDriver.Artboard.ARTBOARD_0);
        prism.setDefaultBootArtboard(GoBildaPrismDriver.Artboard.ARTBOARD_0);
        prism.enableDefaultBootArtboard(true);
        faceShown = null;
    }

    /** Makes the next enableLeds() call redraw the face (use after a test pattern). */
    public void redrawFace() {
        faceShown = null;
    }

    // ---- Bench tests for building the panel ----

    /** Row 1 red, left half blue. Left half BLUE = higher layers draw on top (faces are correct). */
    public void layerOrderTest() {
        prism.clearAllAnimations();
        add(GoBildaPrismDriver.LayerHeight.LAYER_0, new int[]{255, 0, 0}, 0, 13, TEST_BRIGHTNESS);
        add(GoBildaPrismDriver.LayerHeight.LAYER_1, new int[]{0, 0, 255}, 0, 6, TEST_BRIGHTNESS);
        faceShown = null;
    }

    /** Expect top-left green, top-right blue, bottom-left yellow, bottom-right white. */
    public void cornerTest() {
        prism.clearAllAnimations();
        add(GoBildaPrismDriver.LayerHeight.LAYER_0, new int[]{0, 255, 0}, 0, 0, TEST_BRIGHTNESS);
        add(GoBildaPrismDriver.LayerHeight.LAYER_1, new int[]{0, 0, 255}, 13, 13, TEST_BRIGHTNESS);
        add(GoBildaPrismDriver.LayerHeight.LAYER_2, new int[]{255, 255, 0}, 168, 168, TEST_BRIGHTNESS);
        add(GoBildaPrismDriver.LayerHeight.LAYER_3, new int[]{255, 255, 255}, 181, 181, TEST_BRIGHTNESS);
        faceShown = null;
    }

    /** Lights only row r (0-based, 0 = top) in orange. */
    public void lightRow(int r) {
        prism.clearAllAnimations();
        add(GoBildaPrismDriver.LayerHeight.LAYER_0, new int[]{255, 80, 0}, r * ROW_LENGTH, r * ROW_LENGTH + ROW_LENGTH - 1, TEST_BRIGHTNESS);
        faceShown = null;
    }

    /** Turns every LED off. */
    public void clear() {
        prism.clearAllAnimations();
        faceShown = null;
    }

    private void add(GoBildaPrismDriver.LayerHeight layer, int[] rgb, int start, int stop) {
        add(layer, rgb, start, stop, BRIGHTNESS);
    }

    private void add(GoBildaPrismDriver.LayerHeight layer, int[] rgb, int start, int stop, int brightness) {
        PrismAnimations.Solid solid = new PrismAnimations.Solid(new Color(rgb[0], rgb[1], rgb[2]));
        solid.setBrightness(brightness);
        solid.setStartIndex(start);
        solid.setStopIndex(stop);
        prism.insertAndUpdateAnimation(layer, solid);
    }
}
