package edu.mccc.cos210.leddisplay.transition;
import edu.mccc.cos210.leddisplay.*;
import com.cbthinkx.util.Debug;

import java.awt.Graphics.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;

public class Transition0 extends LEDDisplayTransition {
    private static final int FLASH_TIME = 100;
    private static final int NUM_FLASHES = 3;
	public void go(LEDDisplayView view, LEDDisplayView.LED[][] leds) {
		Debug.println("Transition0.go()");

        flashLEDs(view);

        //String toWrite = getData();
        String toWrite = "Kento!";
        LinkedList<boolean[][]> bitmaps = new LinkedList<boolean[][]>();
        /** Measures the length of the word in pixels */
        int wordLength = 0;
        for (char character : toWrite.toCharArray()) {
            Letter letter = new Letter(character);
            boolean[][] bmp = letter.getBitmap();
            bitmaps.add(bmp);
            wordLength += bmp.length;
        }
        int offset = (leds[0].length - wordLength) / 2;
        Blitter blitter = new Blitter(leds, 0);
        blitter.incrementOffset(offset, 0);

        for (boolean[][] bmp : bitmaps) {
            blitter.blitBitmap(bmp);
        }

		try {
            Thread.sleep(3000);
		} catch (Exception ex) {
		}
        flashLEDs(view);
        System.out.println("One write completed");
	}
    private void flashLEDs(LEDDisplayView view) {
		try {
            for (int i = 0; i < NUM_FLASHES; i++) {
                view.setLEDs();
                Thread.sleep(FLASH_TIME);
                view.clearLEDs();
                Thread.sleep(FLASH_TIME);
            }
		} catch (Exception ex) {
		}
    }
}