package V2_3;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;

public class ProgramPanel extends Panel implements KeyListener, MouseMotionListener, MouseListener
{
	private Dimension dim = new Dimension(1406, 703); //dimensions of the window (needed for centering board)
	//offscreen graphics
	public BufferedImage osm;
	public Graphics osg;
	private ArrayList<Board> boards = new ArrayList<Board>(); //list of boards (1 or 2 (for multiplayer))
	private int gridSquareSize = 25; //default size of the board
	private int screenBorder = 50; //border around each board: there are 50 pixels at least between the edges of the window and the board
	private int row, col; //rows and columns on the boards
	private boolean paused; //state of the game

	public ProgramPanel(int row, int col, int l, int players) 
	/*constructor: creates a board with the row and col values (actual rows and columns are increased for the border of the board)
	*the current speed of the blocks falling is determined by l (level) and all the necessary listeners are added to the class here
	*the game timers and the block falling timers are added to start the game.
	*/
	{
		this.row = row;
		this.col = col;
		for(int i = 0; i < players; i++)
		{
			boards.add(new Board(row, col, l));
		}
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		initialize();
	}

	public void initialize()
	/*
	*responsible for setting all the drawing references for each board: 
	*the top left corner's coordinates for each board, block size
	*is called by paint so the changes are made whenever the user changes the size of the window
	*/
	{
		osm = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		osg = osm.getGraphics();
		gridSquareSize = Math.min(25, (dim.height - screenBorder)/row);
		gridSquareSize = Math.min(gridSquareSize, ((dim.width/boards.size()) - screenBorder)/col);
		for(int i = 0; i < boards.size(); i++)
		{
			boards.get(i).boardx = (i*dim.width/2) + ((dim.width/boards.size()) - screenBorder - (gridSquareSize*(boards.get(i).board[0].length - 2)))/2 + screenBorder/2;
			boards.get(i).boardy = (dim.height - screenBorder - (gridSquareSize*(boards.get(i).board.length - 6)))/2 + screenBorder/2;
			boards.get(i).corner = new Point(i*dim.width/2, 0);
			boards.get(i).gridSquareSize = gridSquareSize;
		}
	}

	public void paint(Graphics g) 
	/*
	*called whenever repaint() is called
	*sets the size of the blocks in each grid (whenever the screen size changes, this has to change)
	*osg and osm are initialized for off screen graphics
	*calls update to draw the actual display
	*/
	{
		dim = getSize();
		initialize();
		update(g);
	}

	public void update(Graphics g) 
	/*
	*draws the score, level, game time and the game (board, pieces, shadow block)
	*this method draws the board based on boardx and boardy (top corner of the board) and the size of each block in the board
	*the values in board[][] are used to determine the colour occupied in each block of the board.
	*/
	{
		osg.setColor(Color.BLACK);
		osg.fillRect(0, 0, dim.width, dim.height);

		for(int i = 0; i < boards.size(); i++)
		{
			boards.get(i).display(osg);
		}

		g.drawImage(osm, 0, 0, this);
		repaint();
	}

	public void keyReleased(KeyEvent k){} //does nothing

	public void keyPressed(KeyEvent k)
	/*
	called whenever a key is pressed
	*checks first if the game is paused (if paused is true, only the p key works to toggle the pause)
	*if it is not paused (paused is false), all the key functions work (gets the code of the key pressed and executes the functions accordingly)
	*/
	{
		Board b1 = boards.get(boards.size() - 1);
		Board b2 = boards.get(0);
		int code = k.getKeyCode();
		if(!paused){
			if(code == 37){// left arrow: moves piece left			
				b1.drawBlock(0);
				if(b1.isValidMove(0,-1))
					b1.blockCenter.x --;
				b1.drawBlock(b1.blockColour);
			}
			else if(code == 38){ // up arrow: rotates piece counterclockwise by changing the values in blockbuilder and checking if a rotation is possible. If not, the values in blockbuilder are reverted
				b1.drawBlock(0);
				for(int i = 0; i < 3; i++){
					int temp = b1.blockbuilder[0][i];
					b1.blockbuilder[0][i] = b1.blockbuilder[1][i] * -1;
					b1.blockbuilder[1][i] = temp;
				}
				if(!b1.isValidMove(0,0)){
					for(int i = 0; i < 3; i++){
						int temp = b1.blockbuilder[1][i];
						b1.blockbuilder[1][i] = b1.blockbuilder[0][i] * -1;
						b1.blockbuilder[0][i] = temp;
					}
				}
				b1.drawBlock(b1.blockColour);
			} 
			else if(code == 39){ // right arrow: moves piece right
				b1.drawBlock(0);
				if(b1.isValidMove(0,1))
					b1.blockCenter.x ++;
				b1.drawBlock(b1.blockColour);
			}
			else if(code == 40){ // down arrow: moves piece down (faster than timer)
				b1.drawBlock(0);
				if(b1.isValidMove(1,0))
					b1.blockCenter.y ++;
				else b1.lockDelay();
				b1.drawBlock(b1.blockColour);
			}	
			else if(code == 32){ // spacebar: moves piece as far down as possible
				b1.drawBlock(0);
				while(b1.isValidMove(1,0)){
					b1.blockCenter.y++;
				}
				b1.drawBlock(b1.blockColour);
				b1.lockDelay();
			}
			else if(code == 80){ //toggles pause (pauses game here and cancels all timers)
				b1.pause(true);
				b2.pause(true);
				paused = true;
			} 
			else if(code == 87){ //w key
				b2.drawBlock(0);
				for(int i = 0; i < 3; i++){
					int temp = b2.blockbuilder[0][i];
					b2.blockbuilder[0][i] = b2.blockbuilder[1][i] * -1;
					b2.blockbuilder[1][i] = temp;
				}
				if(!b2.isValidMove(0,0)){
					for(int i = 0; i < 3; i++){
						int temp = b2.blockbuilder[1][i];
						b2.blockbuilder[1][i] = b2.blockbuilder[0][i] * -1;
						b2.blockbuilder[0][i] = temp;
					}
				}
				b2.drawBlock(b2.blockColour);
			}
			else if(code == 65){//a key
				b2.drawBlock(0);
				if(b2.isValidMove(0,-1))
					b2.blockCenter.x --;
				b2.drawBlock(b2.blockColour);
			}
			else if(code == 83){//s key
				b2.drawBlock(0);
				if(b2.isValidMove(1,0))
					b2.blockCenter.y ++;
				else b2.lockDelay();
				b2.drawBlock(b2.blockColour);
			}
			else if(code == 68){//d key
				b2.drawBlock(0);
				if(b2.isValidMove(0,1))
					b2.blockCenter.x ++;
				b2.drawBlock(b2.blockColour);
			}
			else if(code == 81){//q key
				b2.drawBlock(0);
				while(b2.isValidMove(1,0)){
					b2.blockCenter.y++;
				}
				b2.drawBlock(b2.blockColour);
				b2.lockDelay();
			}
			else{
				System.out.println(code);
			}
		}

		else if(code == 80){//p key: paused is false (everything is playable) and all timers are reset
			b1.pause(false);
			b2.pause(false);
			paused = false;
		}
	}

	public void keyTyped(KeyEvent k){}

	public void mouseClicked(MouseEvent m){	}

	public void mouseEntered(MouseEvent m){}

	public void mouseExited(MouseEvent m){}

	public void mouseReleased(MouseEvent m){}

	public void mousePressed(MouseEvent m){ 
	//detects when the mouse is pressed and does actions depending on which mouse button is pressed
		if(!paused && boards.size() == 1){
			Board b = boards.get(0);
			int button = m.getButton();
			if(button == 1){ //left click detected: removes piece from its current location and checks the furthest down the board the piece can fall and draws the piece there.
				b.drawBlock(0);
				while(b.isValidMove(1,0)){
					b.blockCenter.y++;
				}
				b.lockDelay();
				b.drawBlock(b.blockColour);
			}
			else if(button == 3){ //right click detected: rotates the piece the same way the up arrow key rotates the piece.
				b.drawBlock(0);
				for(int i = 0; i < 3; i++){
					int temp = b.blockbuilder[0][i];
					b.blockbuilder[0][i] = b.blockbuilder[1][i] * -1;
					b.blockbuilder[1][i] = temp;
				}
				if(!b.isValidMove(0,0)){
					for(int i = 0; i < 3; i++){
						int temp = b.blockbuilder[1][i];
						b.blockbuilder[1][i] = b.blockbuilder[0][i] * -1;
						b.blockbuilder[0][i] = temp;
					}
				}
				b.drawBlock(b.blockColour);
			}
		}
	}

	public void mouseMoved(MouseEvent m){ 
	//moves the block left and right based on the x value of the mouse pointer. This works within the grid and outside as well.
		if(!paused && boards.size() == 1){
			Board b = boards.get(0);
			Point mouseLocation = m.getPoint();
			b.drawBlock(0);
			int colPoint = 0;
			int counter = 0;
			while(b.boardx + (counter*gridSquareSize) < mouseLocation.x)
				counter++;
			colPoint = counter;
			int shift = 0;
			if(b.blockCenter.x < colPoint)
				shift = 1;
			else shift = -1;
			b.drawBlock(0);
			while(b.isValidMove(0,shift) && b.blockCenter.x != colPoint)
				b.blockCenter.x += shift;
			b.drawBlock(b.blockColour);
			repaint();
		}
	}

	public void mouseDragged(MouseEvent m){}

}
