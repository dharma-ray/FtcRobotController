package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.LED;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/** Grizzly LED on normally; Fury LED on while the color sensor sees the ball. */
@TeleOp(name = "Simple LED Control", group = "TeleOp")
public class SimpleLedOpMode extends LinearOpMode {

    private LED grizzlyLed;
    private LED furyLed;
    private DistanceSensor ballSensor;
    private boolean ballHeld = false;

    @Override
    public void runOpMode() {
        setLeds();

        waitForStart();

        while (opModeIsActive()) {
            enableLeds();
            telemetry.update();
        }
    }

    /** Finds both LEDs and the ball sensor in the hardware config. Call once, before waitForStart(). */
    private void setLeds() {
        grizzlyLed = hardwareMap.get(LED.class, "grizzly_led");
        furyLed = hardwareMap.get(LED.class, "fury_led");
        ballSensor = hardwareMap.get(DistanceSensor.class, "ball_sensor");
    }

    /** Checks the ball sensor: Fury LED on while the ball is held, otherwise Grizzly LED on. */
    private void enableLeds() {
        double cm = ballSensor.getDistance(DistanceUnit.CM);
        ballHeld = cm < (ballHeld ? 6.0 : 4.0);  // caught under 4 cm, released over 6 cm

        grizzlyLed.enable(!ballHeld);
        furyLed.enable(ballHeld);

        telemetry.addData("Ball distance", "%.1f cm", cm);
        telemetry.addData("Mood", ballHeld ? "FURY" : "GRIZZLY");
    }
}
