package V2_3;

import java.awt.*;

public class ProgramWindow extends Frame
{
	ProgramPanel panel;

	public ProgramWindow(int r, int c, int l, int p)
	{
		panel = new ProgramPanel(r, c, l, p); //creates a new ProgramPanel to display game to user
		setTitle("Tetris"); //sets the title of the window
		setSize(710*p, 737); //sets the dimensions of the window
		setLocation(0,0); //sets the location of the window on the monitor
		setVisible(true); //the window is visible
		add(panel); //the ProgramPanel to display the game is added to the window
		panel.setBackground(Color.BLACK); //the panel's background is set to black
	}
}