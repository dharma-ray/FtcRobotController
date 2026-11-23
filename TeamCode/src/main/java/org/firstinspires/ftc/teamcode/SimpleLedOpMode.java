package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * The only OpMode: bear face on the Prism. All LED logic lives in LedController.
 *
 * Normal mode: Grizzly face, Fury face while the ball sensor sees the ball.
 *
 * gamepad1 BACK toggles TEST mode (for building the panel):
 *   A       = layer order test (left half BLUE = faces are correct)
 *   B       = corner test (TL green, TR blue, BL yellow, BR white)
 *   X       = row walk (one row at a time, top to bottom)
 *   Y       = all LEDs off
 *   DPAD UP = save Grizzly as the power-on face (Artboard 0)
 */
@TeleOp(name = "Bear Face LED", group = "TeleOp")
public class SimpleLedOpMode extends LinearOpMode {

    private boolean testMode = false;
    private boolean lastBack = false;

    @Override
    public void runOpMode() {
        LedController leds = new LedController(telemetry);
        leds.setLeds(hardwareMap);

        telemetry.addLine("Ready. BACK = toggle test mode.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            boolean back = gamepad1.back;
            if (back && !lastBack) {
                testMode = !testMode;
                if (testMode) leds.clear(); else leds.redrawFace();
            }
            lastBack = back;

            if (testMode) {
                runTests(leds);
            } else {
                leds.enableLeds();
            }
            telemetry.update();
        }
    }

    /** One pass of the bench-test controls. */
    private void runTests(LedController leds) {
        telemetry.addLine("TEST MODE (BACK to exit)");
        telemetry.addLine("A = layer order | B = corners | X = row walk | Y = off | DPAD UP = save boot face");

        if (gamepad1.a) {
            leds.layerOrderTest();
            telemetry.addLine("Left half BLUE -> faces are correct as-is");
            telemetry.addLine("Whole row RED -> reverse the layer numbers in LedController");
            sleep(400);
        } else if (gamepad1.b) {
            leds.cornerTest();
            telemetry.addLine("Expect: TL green, TR blue, BL yellow, BR white");
            sleep(400);
        } else if (gamepad1.x) {
            for (int r = 0; r < LedController.ROWS && opModeIsActive(); r++) {
                leds.lightRow(r);
                telemetry.addData("Lighting row", r + 1);
                telemetry.update();
                sleep(700);
            }
        } else if (gamepad1.y) {
            leds.clear();
            sleep(400);
        } else if (gamepad1.dpad_up) {
            leds.saveGrizzlyAsBootFace();
            telemetry.addLine("Grizzly saved to Artboard 0 and set as boot face.");
            sleep(400);
        }
    }
}
