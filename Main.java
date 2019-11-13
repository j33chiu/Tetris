package V2_3;

import java.awt.event.*;
import java.util.*;
import java.awt.*;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("Instructions: WASD keys or arrow keys to control block. q or spacebar to drop the block. p to pause the game");
		Scanner in = new Scanner(System.in); //initialize the Scanner to take user input
		System.out.print("Rows: "); //rows prompt
		int r = in.nextInt(); //takes user input for number of rows
		r = Math.max(r, 4); //ensures the number of rows is not less than 4
		System.out.print("Columns: "); //columns prompt
		int c = in.nextInt(); //takes user input for number of columns
		c = Math.max(c, 4); //ensures the number of columns is not less than 4
		System.out.print("Level: "); //level number prompt
		int l = in.nextInt(); //takes user input for the level
		l = Math.min(l, 29); //ensures the level is not greater than 29
		l = Math.max(0, l); //ensures the level is not less than 0
		System.out.print("Players: "); //player number prompt
		int p = in.nextInt(); //takes user input for player number
		p = Math.min(2, p); //ensures the number of players does not exceed 2
		p = Math.max(1, p); //ensures the number of players is not less than 1

		ProgramWindow window = new ProgramWindow(r, c, l, p); //initialize the window which the user sees
		window.addWindowListener(new WindowAdapter(){ //allows the user to exit the game
			public void windowClosing(WindowEvent e) {
        		System.exit(0);
        	}
		});
	}
}