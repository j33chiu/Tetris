package V2_3;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;

public class Board
{
	public int boardx = 0;
	public int boardy = 0;
	public Point corner;
	public int[][] board;
	private Color[] colourList = new Color[]{Color.WHITE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED, Color.CYAN, Color.MAGENTA}; //list of colours for the blocks. Each number x representing a colour refers to the colour at the index x.
	private String[] blockCodes = new String[]{"2,-1,0,1,0,2,0", "2,-1,0,1,0,1,-1", "3,-1,0,-1,-1,1,0", "2,-1,0,0,-1,1,-1", "3,1,0,0,-1,-1,-1", "3,1,0,0,-1,-1,0", "2,1,0,0,-1,1,-1"}; //csv codes for building the 3 blocks in a tetris piece around a central block.
	private int[] speeds = new int[]{800, 716, 636, 550, 467, 383, 300, 217, 133, 100, 84, 84, 84, 67, 67, 67, 50, 50, 50, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 17}; //list of speed: the current game level corresponds to the index in this array.
	private Random r = new Random(); //for determining a random block and random colour: makes index numbers for the blockCodes array above and the colourList array above.
	public Point blockCenter; //x, y = col, row: refers to the central block of each block around which the three other blocks are determined with blockCodes[][].
	public int[][] blockbuilder = new int[2][3]; //row 1: x changes, row 2: y changes: determines the 3 blocks around the central block.
	public int blockColour = 0; //correspond to indexes in colour List. (colourList[0] is Color.WHITE (place holder colour as it will never be used))

	public Timer tm = new Timer(); //Timer for the block falling
	public TimerTask motion = new Fall(); //timertasks which handles the periodic falling of the pieces.
	public Timer gameTime = new Timer(); //Timer for keeping game time.
	public TimerTask time = new Time(); //timertask for determining seconds, minutes and hours of time played.
	public int totalSeconds = 0; //total seconds played (seconds, minutes, and hours (below) are determined from this)
	public int seconds, minutes, hours; //values to display for the game time.

	public int score = 0; //total score of the game
	public int level, startLevel = 0; //level: current game level (for determinine speed of blocks falling) startlevel: the initial choice of the user for the level.
	public int totalRowsBroken = 0; //total rows broken to determine the current level of the game.

	public int gridSquareSize = 25; //the default square size (maximum). 25 pixels in width and height
	private int row,col; //rows and columns of the tetris board
	private int speed = 0; //period at which blocks fall (set with the speeds array)
	public boolean paused = false; //the state of the game: the timers stop and the listeners (keyboard, mouse) don't work if this is true.
	private int screenBorder = 50; //default border of the screen (the board does not go into this border in the screen. (always 50 pixels between the top, bottom, left, right sides of the board and the window edges))

	public Board(int row, int col, int level)
	{
		board = new int[row + 6][col + 2];
		for(int i = 0; i < board.length; i++){
			if(i == 0 || i == board.length - 1){
				for(int j = 0; j < board[i].length; j++)
					board[i][j] = 1;
			}
			else{
				board[i][0] = 1;
				board[i][board[i].length - 1] = 1;
			}
		}
		speed = speeds[level];
		gameTime.scheduleAtFixedRate(time, 1000, 1000);
		tm.scheduleAtFixedRate(motion, 1000, speed);
		newBlock();
	}

	public void newBlock() //resets all important global variables for a moving playable block: determines which block to build (random) and its colour (random) and the current location of the central block on the board.
	//a check is made to determine if a new block can be made: if it cannot, the game ends (all timers stop and listeners no longer function (essentially a pause)) 
	{
		int randomBlock = r.nextInt(blockCodes.length);
		String[] code = blockCodes[randomBlock].split(",");
		int c = 1;
		for(int col = 0; col < blockbuilder[0].length; col++){
			for(int row = 0; row < blockbuilder.length; row++){
				blockbuilder[row][col] = Integer.parseInt(code[c]);
				c++;
			}
		}
		blockColour = r.nextInt(colourList.length - 1) + 1;
		int ref = Integer.parseInt(code[0]);
		blockCenter = new Point((board[0].length - 4)/2 + ref - 1, 4);
		if(!isValidMove(0,0)){
			tm.cancel();
			gameTime.cancel();
			paused = true;
		}
		else drawBlock(blockColour);
	}
	public void drawBlock(int colour) //separate variable is used instead of the same variable blockColour, so int colour can equal 0: equivalent to deleting the block
	//draws the blocks of the piece around the central piece based on blockbuilder (shift left, right, up, down values from the central block for each block)
	{
		board[blockCenter.y][blockCenter.x] = colour;
		for(int i = 0; i < blockbuilder[0].length; i++)
			board[blockCenter.y + blockbuilder[1][i]][blockCenter.x + blockbuilder[0][i]] = colour;
	}

	public boolean isValidMove(int rChange, int cChange) //rChange and cChange allow for directional checking: checks where the block WOULD go and returns true/false depending on whether block can be moved there
	//"moves" the piece to the necessary place with the same building principle as drawBlock(). Based on the location of the current blocks in the piece, the board is checked for its values in the corresponding location.
	//if the blocks in the board for each block in the piece is a zero value, the necessary space needed for the block to move is empty, and true is returned to indicate the move is possible.
	//a false return indicates the space needed for the block to move is not there and the block cannot move.
	//NOTE: drawblock(0) must be called prior to this so that the checking does not interfere with the current location of the block.
	{
		if(board[blockCenter.y + rChange][blockCenter.x + cChange] > 0)
			return false;
		for(int i = 0; i < blockbuilder[0].length; i++)
			if(board[blockCenter.y + blockbuilder[1][i] + rChange][blockCenter.x + blockbuilder[0][i] + cChange] > 0)
				return false;
		return true;
	}

	public class Fall extends TimerTask //Timertask class to drop the blocks periodically.
	//the piece is moved down 1 block (drawBlock(0) to empty the current space, isValidMove(1, 0) to check the location of the piece 1 block down, depending on the return value of this method, the current center of the piece is moved)
	//the piece is redrawn at either the same location (piece could not move) or the new location (piece could move and moves 1 block down)
	{
		public void run()
		{
			drawBlock(0);
			if(isValidMove(1, 0)){
				blockCenter.y++;
				drawBlock(blockColour);
			}
			else{
				drawBlock(blockColour);
				delete();
				newBlock();
			}
		}
	}

	public class Time extends TimerTask 
	//Timertask class to determine the game time (time played)
	{
		public void run()
		{
			totalSeconds++;
			seconds = totalSeconds%60;
			minutes = totalSeconds/60;
			hours = minutes/60;
			minutes = minutes%60;
		}
	}

	public void display(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.drawString("Score: " + Integer.toString(score), corner.x + 15, 15);
		g.drawString("Rows: " + Integer.toString(totalRowsBroken), corner.x + 15, 30);
		g.drawString("Level: " + Integer.toString(level), corner.x + 115, 15);
		String timeDisplay = "GameTime: " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
		
		g.drawString(timeDisplay, corner.x + 115, 30);

		for(int i = 5; i < board.length - 1; i++){
			for(int j = 1; j < board[0].length - 1; j++){
				if(board[i][j] > 0){
					g.setColor(colourList[board[i][j]]);
					g.fillRect(boardx + ((j - 1) * gridSquareSize), boardy + ((i - 5) * gridSquareSize), gridSquareSize, gridSquareSize);
				}
				g.setColor(Color.WHITE);
				g.drawRect(boardx + ((j - 1) * gridSquareSize), boardy + ((i - 5) * gridSquareSize), gridSquareSize, gridSquareSize);
			}
		}
		// shadow block
		int i = 0;
		drawBlock(0);
		while(isValidMove(i, 0))
			i++;
		i--;
		drawBlock(blockColour);
		g.setColor(colourList[blockColour]);
		if(boardy + ((blockCenter.y + i - 5) * gridSquareSize) >= boardy && board[blockCenter.y + i][blockCenter.x] == 0)
			g.drawRect(boardx + ((blockCenter.x - 1) * gridSquareSize), boardy + ((blockCenter.y + i - 5) * gridSquareSize), gridSquareSize, gridSquareSize);
		for(int j = 0; j < blockbuilder[0].length; j++){
			if(board[blockCenter.y + blockbuilder[1][j] + i][blockCenter.x + blockbuilder[0][j]] != 0) g.setColor(Color.WHITE);
			if(boardy + ((blockCenter.y + blockbuilder[1][j] + i - 5) * gridSquareSize) >= boardy)
				g.drawRect(boardx + ((blockCenter.x + blockbuilder[0][j] - 1) * gridSquareSize), boardy + ((blockCenter.y + blockbuilder[1][j] + i - 5) * gridSquareSize), gridSquareSize, gridSquareSize);
		}
		//end of code for shadow block
	}

	public void delete()//checks every row of the board for a completed row
	//if a row is full, each value in that row is set to 0 and each row above is moved downwards. The rows are checked top to bottom so the moving of the above blocks down does not interfere with the checking.
	//a counter keeps track of how many rows are broken in a single drop of a piece. This value is sent to score() as an argument and the current game score is computed from this.
	{
		int moveBreak = 0;
		for(int row = 5; row < board.length - 1; row++){
			boolean filled = true;
			for(int col = 1; col < board[row].length - 1; col++){
				if(board[row][col] == 0){
					filled = false;
					break;
				}
			}
			if(filled){
				board[row] = new int[board[0].length];
				board[row][0] = 1;
				board[row][board[0].length - 1] = 1;
				for(int r = row - 1; r > 4; r--){
					for(int c = 1; c < board[r].length - 1; c++){
						board[r + 1][c] += board[r][c];
						board[r][c] -= board[r][c];
					}
				}
				moveBreak++;
			}
		}
		score(moveBreak);
	}

	public void score(int rowsBroken)//computes the score and level based on the rows broken and the rows broken in a single dropped block.
	//if a level change is detected, the speed of the dropping blocks increases. The Timers are cancelled and reset with the new time interval.
	{
		if(rowsBroken == 1)
			score += 40 * (level + 1);
		else if(rowsBroken == 2)
			score += 100 * (level + 1);
		else if(rowsBroken == 3)
			score += 300 * (level + 1);
		else if(rowsBroken > 3)
			score += 1200 * (level + 1);
		totalRowsBroken += rowsBroken;
		int prevLevel = level;
		level = startLevel + totalRowsBroken/10;
		if(level != prevLevel){
			tm.cancel();
			gameTime.cancel();
			tm = new Timer();
			motion = new Fall();
			gameTime = new Timer();
			time = new Time();
			speed = speeds[Math.min(level, 29)];
			gameTime.scheduleAtFixedRate(time, 1000, 1000);
			tm.scheduleAtFixedRate(motion, 1000, speed);
		}
	}

	public void lockDelay(){
	//this method is called whenever a block is placed down. It allows the user to further move the block left and right after a piece is no longer able to move down. (there is a 200 millisecond gap) in which the user can do this.
		//The method does this by cancelling the timer for dropping blocks and restarting with a 200 millisecond delay.
		tm.cancel();
		tm = new Timer();
		motion = new Fall();
		tm.scheduleAtFixedRate(motion, 200, speed);
	}

	public void pause(boolean tf)
	{
		if(tf){
			tm.cancel();
			gameTime.cancel();
			paused = true;
		}
		else{
			paused = false;
			tm.cancel();
			gameTime.cancel();
			tm = new Timer();
			gameTime = new Timer();
			motion = new Fall();
			time = new Time();
			gameTime.scheduleAtFixedRate(time, 0, 1000);
			tm.scheduleAtFixedRate(motion, 0, speed);
		}
	}
}
