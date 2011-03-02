package com.robonobo.mina.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Tagline {
	public static String getTagLine() {
		String result = null;
		Random rand = new Random();
		switch(rand.nextInt(12)) {
		case 0:
			result = "Now I'm going to kill you, and the cake is all gone.";
			break;
		case 1:
			result = "Remember: Don't shoot food.";
			break;
		case 2:
			result = "Shots do not hurt other players - yet.";
			break;
		case 3:
			result = "Time to kick ass and chew bubblegum.  And I'm all out of gum.";
			break;
		case 4:
			result = "Gimme some sugar, baby.";
			break;
		case 5:
			result = "Look at you, hacker.  A pathetic creature of meat and bone, panting and sweating as you run through my corridors.";
			break;
		case 6:
			result = "Hail to the King, baby.";
			break;
		case 7:
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
			result = "robonobo network reports self-awareness achieved " + dateFormat.format(new Date()) + " local time.";
			break;
		case 8:
			result = "Greetings Professor Falken.  Would you like to play a game?";
			break;
		case 9:
			result = "More Piggies, Gir!  BRING ME MORE PIGGIES!";
			break;
		case 10:
			result = "What we see and hear, how we work, what we think... it's all about the information!";
			break;
		case 11:
			result = "Can't stop the signal.";
			break;
		}

		return result;
	}

}
