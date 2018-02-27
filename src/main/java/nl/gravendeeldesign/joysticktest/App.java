package nl.gravendeeldesign.joysticktest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import gnu.io.NRSerialPort;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Controller.Type;

/**
 * Joystick test app.
 *
 */
public class App {
  
  private static DataOutputStream outs;
  private static DataInputStream ins;
  
  private static void writeCommand(String command) throws Exception {
    outs.writeBytes(command + "\n");
    while(ins.available() > 0) {
      //NOTE: Just ignore the command for now, and keep reading until no more data is available
    }
  }
  
  public static void main(String[] args) throws Exception {
    
    for(String serialPort : NRSerialPort.getAvailableSerialPorts()) {
      System.out.println("P: " + serialPort);
    }
    String port = "/dev/tty.usbserial-AL01H41X";
    int baudRate = 115200;
    NRSerialPort serial = new NRSerialPort(port, baudRate);
    serial.connect();
    ins = new DataInputStream(serial.getInputStream());
    outs = new DataOutputStream(serial.getOutputStream());
    
    System.out.println("Starting...");
    Thread.sleep(3000);
    Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

    for(Controller controller : controllers) {
      if(controller.getType() == Type.STICK) {
        // Found a joystick USB input device, start the application with it
        start(controller);
        break;
      }
    }
    
  }
  
  /**
   * Sleep utility, ignores exceptions.
   * @param millis
   */
  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch(InterruptedException ex) {}// NOP
  }
  
  /**
   * Start joystick control for the uArm.
   * @param stick Joystick controller to use
   * @throws Exception
   */
  private static void start(Controller stick) throws Exception {
    int i = 0;
    
    // Initial hardware state
    double x = 100;
    double y = 100;
    double z = 100;
    double rot = 90;
    boolean pumpOn = false;
    
    while(true) {
      stick.poll();
      i++;
      Component[] components = stick.getComponents();
      Map<String, Float> vals = new HashMap<String, Float>();
      for(Component component : components) {
        vals.put(component.getName(), component.getPollData());
        //System.out.println("C: " + component.getName() + ": " + component.getPollData());
      }
      if(Math.abs(vals.get("x")) > 0.15) {
        x -= vals.get("x");
      }
      if(Math.abs(vals.get("y")) > 0.15) {
        y -= vals.get("y");//NOTE: Have to negate this for correct movement
      }
      if(Math.abs(vals.get("rz")) > 0.15) {
        rot += vals.get("rz");
      }
      
      //NOTE: Make sure to cap the rotation to 10..170 - exact numbers will depend on the connected Arduino
      if(rot < 10) {
        rot = 10;
      } else if(rot > 170) {
        rot = 170;
      }
      if(vals.get("pov") == 0.25) {
        // Up
        z += 1;
      } else if(vals.get("pov") == 0.75) {
        // Up
        z -= 1;
      }
      if(!pumpOn && vals.get("0") == 1) {
        // Turn the vacuum pump on
        writeCommand("#1 M231 V1");
        pumpOn = true;
      } else if(pumpOn && vals.get("0") == 0) {
        // Turn the vacuum pump off
        writeCommand("#1 M231 V0");
        pumpOn = false;
      }
      
      if(i > 100) {
        //NOTE: This shows some debug information about the available components, to help debugging
        for(Component component : components) {
          System.out.println("C: " + component.getName() + ": " + component.getPollData());
        }
        i = 0;
      }
      //NOTE: Optionally use coordinates in millimeters (G0) or polar coordinates (G201)
      //String command = "#1 G0 X" + Math.round(x) + " Y" + Math.round(y) + " Z" + Math.round(z) + " F100";
      String command = "#1 G201 S" + Math.round(y) + " R" + Math.round(x) + " H" + Math.round(z) + " F255";
      writeCommand(command);
      
      // Write out a command to update the rotation of the hand (0-180 degrees)
      writeCommand("#1 G202 N3 V" + Math.round(rot));
      
      sleep(20);
    }
  }
  
}
