import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class Board extends JPanel{

	private final int size = 300;
	private Dimension dim = new Dimension(size, size);
	private int x = 0, y = 0;
	private int square = 0;
	private int[] marks = new int[9];
	private int gap = 15;
	private static int turn = -1;
	private static int win = -2;
	private int computerTurn = 1;
	private TicNet net = null;
	
	public Board() {
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				x = e.getX();
				y = e.getY();
				square = x/100 + y/100*3;

				if (turn < 0) drawX(square);
				else if (turn > 0) drawO(square);
			}
		});
		
		setPreferredSize(dim);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, size, size);
		g.drawLine(size/3, 0, size/3, size);
		g.drawLine(0, size/3, size, size/3);
		g.drawLine(2*size/3, 0, 2*size/3, size);
		g.drawLine(0, 2*size/3, size, 2*size/3);
		
		g.setColor(Color.RED);
		for (int i = 0; i < 9; i++) {
			int xx = (size / 3) * (i % 3);
			int yy = (size / 3) * (i / 3);
			if (marks[i] == -1) {
				g.drawLine(xx + gap, yy + gap, xx - gap + size / 3, yy - gap + size / 3);
				g.drawLine(xx + gap, yy - gap + size / 3, xx - gap + size / 3, yy + gap);
			} else if (marks[i] == 2) {
				g.drawArc(xx + gap, yy + gap, size / 3 - 2 * gap, size / 3 - 2 * gap, 0, 360);
			}
		}
	}
	
	public void setTicNet(TicNet tic) {
		net = tic;
	}
	
	public void drawX(int square) {
		if (marks[square] == 0) {
			marks[square] = -1;
			TicTacToe.setTurnLabel(turn = (winCheck(marks) == -2) ? 1 : 0);
			
			if (net.getNNMove(marks, turn) >= 0) {
				drawO(net.getNNMove(marks, turn));
			}
			TicTacToe.setDecorativeButton(win);
		}
		repaint();
	}
	
	public void drawO(int square) {
		if (marks[square] == 0) {
			marks[square] = 2;
			TicTacToe.setTurnLabel(turn = (winCheck(marks) == -2) ? -1 : 0);
			TicTacToe.setDecorativeButton(win);
		}
		repaint();
	}
	
	public void clearBoard() {
		marks = new int[9];
		turn = -1;
		win = -2;
		TicTacToe.setTurnLabel(turn);
		TicTacToe.setDecorativeButton(-2);
		repaint();
	}
	
	public int getTurn() {
		return turn;
	}
	
	public static void setTurn(int t) {
		turn = t;
//		if (t == 1) {
//			
//		}
	}
	
	//return -1 for X win, 1 for O win, 0 for draw, -2 otherwise;
	public static int winCheck(int[] input) {
		for (int i = 0; i < 7; i += 3) {
			int prod = 1;
			for (int j = 0; j < 3; j++) {
				prod *= input[i + j];
			}
			if (prod == -1) return win = -1;
			else if (prod == 8) return win = 1;
		}
		
		for (int i = 0; i < 3; i++) {
			int prod = 1;
			for (int j = 0; j < 7; j += 3) {
				prod *= input[i + j];
			}
			if (prod == -1) return win = -1;
			else if (prod == 8) return win = 1;
		}
		
		int prod = input[0] * input[4] * input[8];
		if (prod == -1) return win = -1;
		else if (prod == 8) return win = 1;
		
		prod = input[2] * input[4] * input[6];
		if (prod == -1) return win = -1;
		else if (prod == 8) return win = 1;
		
		prod = 1;
		for (int i : input) prod *= i;
		if (prod == 0) return win = -2;
		else return win = 0;
	}
}
