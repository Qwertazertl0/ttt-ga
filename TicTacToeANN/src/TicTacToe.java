
import javax.swing.*;

import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.*;
import java.util.*;
import java.io.*;

public class TicTacToe{
	
	private final static int width = 500;
	
	//Three main JPanels
	private Board board = new Board();
	private TicNet net = new TicNet();
	private JPanel options = new JPanel();
	private JPanel text = new JPanel();
	
	private JButton b1 = new JButton("New Game");
	private static JButton b2 = new JButton("Decorative Button");
	private static JLabel f1 = new JLabel("Human's Turn");
	private JLabel f2 = new JLabel("Player X: Human");
	private JLabel f3 = new JLabel("Player O: Neural Net");
	
	public TicTacToe() throws IOException, ParseException {
		JFrame gui = new JFrame("Jong - TicTacToe Neural Net");
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setResizable(false);
		Container cont = gui.getContentPane();
		cont.setBackground(Color.WHITE);
		
		//Introductory text set up
		text.setBackground(Color.WHITE);
		BufferedReader reader = new BufferedReader(new FileReader("Source/IntroText.txt"));
		JTextArea textF = new JTextArea(reader.readLine());
		textF.setSize(width, 100);
		textF.setLineWrap(true);
		textF.setWrapStyleWord(true);
		textF.setEditable(false);
		textF.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		text.add(textF);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 15;
		gbc.ipady = 15;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		cont.setLayout(new GridBagLayout());
		cont.add(text, gbc);
		
		//Options set-up
		options.setBackground(Color.WHITE);
		gbc.ipadx = 5;
		gbc.ipady = 5;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		cont.add(options, gbc);
		
		GridBagConstraints opgbc = new GridBagConstraints();
		options.setLayout(new GridBagLayout());
		b1.setBackground(Color.CYAN);
		b1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				board.clearBoard();
			}
		});
		opgbc.gridx = 0;
		opgbc.gridy = 0;
		opgbc.weighty = 0.4;
		options.add(b1, opgbc);
		
		b2.setBackground(Color.LIGHT_GRAY);
		opgbc.gridy = 1;
		options.add(b2, opgbc);
		
		opgbc.weighty = 0.2;
		opgbc.gridy = 2;
		options.add(f1, opgbc);
		
		opgbc.gridy = 3;
		options.add(f2, opgbc);
		
		opgbc.gridy = 4;
		options.add(f3, opgbc);
		
		//Board set-up
		board.setTicNet(net);
		gbc.weightx = 0;
		gbc.gridx = 1;
		cont.add(board, gbc);
		
		gui.pack();
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
		
		reader.close();
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		TicTacToe game = new TicTacToe();
	}

	public static JLabel getTurnLabel() {
		return f1;
	}
	
	public static void setTurnLabel(int turn) {
		switch (turn) {
		case -1:
			f1.setText("Human's Turn");
			break;
		case 0:
			f1.setText("Game Ended!");
			break;
		case 1:
			f1.setText("Neural Net's Turn");
		default:
		}
	}
	
	public static void setDecorativeButton(int win) {
		switch (win) {
		case -1:
			b2.setText("Human Wins!");
			break;
		case 0:
			b2.setText("It's a Tie!");
			break;
		case 1:
			b2.setText("Neural Net Wins!");
			break;
		default:
			b2.setText("Miao");
		}
	}
}
