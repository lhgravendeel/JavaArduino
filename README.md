# JavaArduino
Test project to link uArm Arduino robot arm with USB joystick or controller

## Overview

This project connects a uArm metal robot arm with Java. It also connects a USB joystick or controller with Java,
to allow those devices to control the robot arm.

The uArm metal uses an Arduino at its core. The Arduino is loaded with a program that opens the Serial port,
accepts input commands from it and converts those into servo actions.

With this program, the Arduino can connect to any computer over USB and Java can interact with it.

## Setup

 1. Load an Arduino program, such as: `https://github.com/uArm-Developer/UArmForArduino/blob/dev/examples/Metal/Metal.ino`
