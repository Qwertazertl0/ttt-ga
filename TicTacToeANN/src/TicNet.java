import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import javax.swing.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.commons.math3.stat.inference.*;

public class TicNet {

	private final Population population = new Population();
	private Individual playing = null;
	private Random random = new Random();
	private final int randWBRange = 1;
	private File file = new File("Source/StartPopulation.txt");
	private File save = new File("Source/EvolvedPopulationSave.txt");
	private final int popSize = 100;
	private boolean randomPlayBool = false;
	
	public TicNet() throws IOException, ParseException {
		JFrame frame = new JFrame("TicTacToe Neural Net Operator");
		Container cont = frame.getContentPane();
		frame.setVisible(true);
		frame.setPreferredSize(new Dimension(300, 300));
		frame.setResizable(false);
		
		JLabel genLabel = new JLabel("Generation: " + population.generation);
		JButton newGen = new JButton("New Generation");
		JButton play = new JButton("Play Best NN");
		JButton tenThouGen = new JButton("10,000 Gen (~80 min)");
		JCheckBox randomPlay = new JCheckBox("Random Play Comparison");
		
		GridBagConstraints gbc = new GridBagConstraints();
		cont.setLayout(new GridBagLayout());
		gbc.ipady = 10;
		gbc.weighty = 0.3;
		gbc.gridx = 0;
		gbc.gridy = 0;
		cont.add(genLabel, gbc);
		
		randomPlay.setMnemonic(KeyEvent.VK_R);
		randomPlay.setSelected(false);
		randomPlay.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				randomPlayBool = !randomPlayBool;
			}
		});
		gbc.gridy++;
		cont.add(randomPlay, gbc);
		
		newGen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				population.newGeneration();
				if (randomPlayBool) population.randomPlayStat();
				
				genLabel.setText("Generation: " + population.generation);
			}
		});
		gbc.gridy++;
		cont.add(newGen, gbc);
		
		play.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				playing = population.indiv[popSize - 1];
			}
		});
		gbc.gridy++;
		cont.add(play, gbc);
		
		tenThouGen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				do {
					population.newGeneration();
				} while (population.generation % 10000 != 0);
				
				genLabel.setText("Generation: " + population.generation);
			}
		});
		gbc.gridy++;
		cont.add(tenThouGen, gbc);
		
		frame.pack();
		frame.setLocation(1300, 400);
		//TODO add buttons and listeners to control genetic algorithm
		
		if (!file.exists()) {
			file.createNewFile();
			FileWriter filew = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(filew);
			System.out.println("Creating a new population");
			population.fill();
			population.writeToFile(writer);
		}
		
		if (save.exists()) {
			//TODO dialogue prompt for loading save
			FileReader savefiler = new FileReader(file);
			BufferedReader saveReader = new BufferedReader(savefiler);
			population.readFromFile(saveReader, this.population);
		} else {
			FileReader filer = new FileReader(file);
			BufferedReader reader = new BufferedReader(filer);
			population.readFromFile(reader, this.population);
		}
	}
	
	public int getNNMove(int[] board, int turn) {
		if (playing == null || turn == 0) return -1;
		else return playing.chooseMove(board, turn);
	}
	
	public class Population {
		
		private int generation = 1;
		private Individual[] indiv = new Individual[popSize];

		private Population(int gen) {
			generation = gen;
		}
		
		private Population() {}
		
		private void fill() {
			for (int i = 0; i < indiv.length; i++) {
				indiv[i] = new Individual();
			}
			generation = 1;
		}
		
		private void writeToFile(BufferedWriter wr) throws IOException {
			JSONObject obj = new JSONObject();
			obj.put("Generation", generation);
			
			JSONArray fav = new JSONArray();
			for (int i = 0; i < indiv.length; i++) {
				fav.add(indiv[i].favorable);
			}
			obj.put("Favorability", fav);
			
			JSONArray pop = new JSONArray();
			for (int i = 0; i < indiv.length; i++) {
				for (int j = 0; j < 100; j++) pop.add(indiv[i].weightBias[j]);
			}
			obj.put("Population", pop);
			wr.write(obj.toJSONString());
			wr.flush();
		}
		
		void readFromFile(BufferedReader br, Population pop) throws ParseException, IOException {
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(br.readLine());
			
			pop.fill();
			pop.generation = (int) (long) obj.get("Generation");
			
			JSONArray arr = (JSONArray) obj.get("Population");
			Iterator<Double> weightIter = arr.iterator();
			int x = 0;
			while (weightIter.hasNext()) {
				int y = x / 100;
				pop.indiv[y].weightBias[x % 100] = weightIter.next();
				x++;
			}
			
			arr = (JSONArray) obj.get("Favorability");
			Iterator<Long> favIter = arr.iterator();
			x = 0;
			while (favIter.hasNext()) {
				pop.indiv[x].favorable = (int) (long) favIter.next();
				x++;
			}
		}
		
		private void sortIndividuals() {
			Arrays.sort(indiv, new Comparator<Individual>() {

				@Override
				public int compare(Individual o1, Individual o2) {
					if (o1.favorable > o2.favorable) return 1;
					else if (o1.favorable < o2.favorable) return -1;
					else return 0;
				}
			});
		}
	
		private void pairAndPlay(Individual x, Individual o) {
			int[] virtBoard = new int[9];
			int win = -2;
			int turn = -1;
			int counter = -4;
			while (win == -2) {
				int maxIndex = (turn > 0) ? o.chooseMove(virtBoard, turn) : x.chooseMove(virtBoard, turn);
				
				virtBoard[maxIndex] = (turn > 0) ? 2 : -1;
				counter++;
				turn = ((win = Board.winCheck(virtBoard)) == -2) ? turn *= -1 : 0;
			}
			
			if (win < 0) {
				x.favorable += (counter == 1) ? 2 : (-1 * counter + 5) / 2;
				o.favorable -= (counter == 1) ? 2 : 1;
			} else if (win > 0) {
				x.favorable -= (counter == 2) ? 2 : 1;
				o.favorable += (counter == 2) ? 2 : 1;
			} else {
				x.favorable += 1;
				o.favorable += 1;
			}
			
//			x.favorable -= win;
//			o.favorable += win;
		}
		
		private int playRandom(Individual net, int t) {
			int[] virtBoard = new int[9];
			int win = -2;
			int turn = t;
			while (win == -2) {
				int maxIndex = (turn > 0) ? net.chooseMove(virtBoard, turn) : net.chooseRandomMove(virtBoard);
				
				virtBoard[maxIndex] = (turn > 0) ? 2 : -1;
				turn = ((win = Board.winCheck(virtBoard)) == -2) ? turn *= -1 : 0;
			}

			return win;
		}
		
		private void randomPlayStat() {
			int w = 0, l = 0, d = 0;
			for (int i = 0; i < 100; i++) {
				int t = (int) Math.pow(-1, i % 2);
				switch (playRandom(population.indiv[popSize-1], t)) {
				case 0:
					d++; break;
				case 1:
					w++; break;
				case -1: 
					l++; break;
				default: System.out.println("Unexpected value: playRandom()");
				}
			}
			//double chi2 = Math.pow(w - 2750/63, 2)*(63.0/2750) + Math.pow(d - 800/63, 2)*(63.0/800) + Math.pow(l - 2750/63, 2)*(63.0/2750);
			double p = 0;
			ChiSquareTest chiTest = new ChiSquareTest();
			long[] observed = {w, d, l};
			double[] expected = {43.65, 12.7, 43.65};
			p = chiTest.chiSquareTest(expected, observed);
			//System.out.println("\tChi-square: " + String.format("%.5f", chi2));
			
			String color = "", reset = "\u001B[0m";
			if (p < 0.05) if (l >= 43) color = "\u001B[91m"; else color = "\u001B[92m";
			System.out.println("\tRandomPlay (W/D/L): " + color + w + "/" + d + "/" + l + reset);
			if (p > 0.05) color = "\u001B[91m"; else color = "\u001B[92m";
			System.out.println("\tH_0 p-value: " + color + String.format("%.5f", p) + reset);
		}
	
		private void newGeneration() {
			for (int i = 0; i < popSize; i++) {
				for (int a = 0; a < popSize; a++) {
					if (i != a) {
						pairAndPlay(population.indiv[i], population.indiv[a]);
					}
				}
			}
			sortIndividuals();
			
			System.out.print("Generation " + (population.generation+1) + ": \n\t" + population.indiv[popSize-1].favorable + " to " + population.indiv[0].favorable + ": ");
			System.out.println(population.indiv[popSize-1].favorable - population.indiv[0].favorable);
			
			
			generation++;
			//TODO set startup dialogue with frequency of generation files
			if (generation % 1000 == 0) {
				try {
					writeToFile(new BufferedWriter(new FileWriter(new File("Source/Generation " + generation + ".txt"))));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			//Cross and mutate code, questionable
			for (int i = 0; i < popSize; i++) {
				if (random.nextDouble() < 0.01*(popSize - 1.25*i) || population.indiv[i].favorable < 0) {
 				
					int bound = 780;
					int r1 = random.nextInt(bound), r2 = random.nextInt(bound), sum = 40, count = 1;
					while (sum < r1) {
						sum += (40 - count);
						count += 1;
					} int parent1 = popSize - count;
					sum = 40; count = 1;
					while (sum < r2) {
						sum += (40 - count);
						count++;
					} int parent2 = popSize - count;
					
					population.indiv[i] = cross(population.indiv[parent1], population.indiv[parent2]);
					if (i % 10 == 0) population.indiv[i] = new Individual();
				}
				
				population.indiv[i].favorable = 0;
			}
		}
		
		private Individual cross(Individual p1, Individual p2) {
			if (random.nextDouble() < 0.85) {
				Individual child = new Individual();
//				int crosspoint = random.nextInt(100);
//				for (int i = 0; i < crosspoint; i++) child.weightBias[i] = p1.weightBias[i];
//				for (int i = crosspoint; i < 100; i++) child.weightBias[i] = p2.weightBias[i];
				
//					child.weightBias[i] = (random.nextDouble() < 0.5) ? p1.weightBias[i] : p2.weightBias[i];
				double ratio = 0.5;
				if (p1.favorable > 0 && p2.favorable > 0) {
					ratio = (double) p1.favorable / (p1.favorable + p2.favorable);
					for (int i = 0; i < 100; i++) {
						child.weightBias[i] = ratio * p1.weightBias[i] + (1 - ratio) * p2.weightBias[i];
					}
				} else return (p1.favorable > p2.favorable) ? p1.mutate() : p2.mutate();
				
				return child.mutate();
			} else if (random.nextDouble() < 0.5) return p1.mutate();
			else return p2.mutate();
		}
	}
	
	private class Individual {
		
		private double[] weightBias = new double[100];
		private int favorable = 0;
		
		private Individual() {
			int a = randWBRange;
			for (int i = 0; i < weightBias.length; i++) {
				weightBias[i] = a * (random.nextDouble()*2 - 1);
			}
			favorable = 0;
		}
		
		private double moveScore(int[] boardIn) {
			double[] hiddenLayer = new double[9];
			//TODO verify the change of value from 2 to 1
			int[] board = new int[9];
			for (int i = 0; i < 9; i++) {
				if (boardIn[i] == 2) board[i] = 1;
				else board[i] = boardIn[i];
			}
			for (int i = 0; i < 9; i++) {
				hiddenLayer[i] = weightBias[9 + 10 * i];
				for (int j = 0; j < 9; j++) {
					hiddenLayer[i] += board[j] * weightBias[10 * i + j];
				}
				
				hiddenLayer[i] = Math.tanh(hiddenLayer[i]);
			}
			
			double score = weightBias[99];
			for (int i = 0; i < 9; i++) {
				score += board[i] * weightBias[90 + i];
			}
			
			return score;
		}
		
		private int chooseMove(int[] virtBoard, int turn) {
			double[] scoreSpace = new double[9];
			for (int i = 0; i < 9; i++) {
				if (virtBoard[i] == 0) {
					virtBoard[i] = (turn > 0) ? 2 : -1;
					scoreSpace[i] = Math.pow(moveScore(virtBoard), 4);
					virtBoard[i] = 0;
				}
			}
			
//			double scoreSum = 0;
//			for (double d : scoreSpace) scoreSum += d;
//			double choice = random.nextDouble() * scoreSum;
//			double choiceSum = 0;
//			int choiceIndex = 0;
//			while (choiceSum < choice) {
//				choiceSum += scoreSpace[choiceIndex];
//				choiceIndex++;
//			}
//			
//			return choiceIndex - 1;

			int maxIndex = 0;
			double max = scoreSpace[0];
			for (int i = 1; i < 9; i++) {
				if (scoreSpace[i] > max) {
					max = scoreSpace[i];
					maxIndex = i;
				}
			}

			return maxIndex;
			
//			System.out.println(Arrays.toString(scoreSpace));
		}
		
		private int chooseRandomMove(int[] virtBoard) {
			int[] scoreSpace = new int[9];
			int scoreSum = 0;
			for (int i = 0; i < 9; i++) {
				if (virtBoard[i] == 0) {
					scoreSpace[i] = 1;
					scoreSum++;
				}
			}
			
			double choice = random.nextDouble() * scoreSum;
			double choiceSum = 0;
			int choiceIndex = 0;
			while (choiceSum < choice) {
				choiceSum += scoreSpace[choiceIndex];
				choiceIndex++;
			}
			
			return choiceIndex - 1;
		}
		
		private Individual mutate() {
			Individual child = new Individual();
			for (int i = 0; i < child.weightBias.length; i++) {
				child.weightBias[i] = this.weightBias[i];
				int sign = (int) Math.pow(-1, i % 2);
				if (random.nextDouble() < 0.01) child.weightBias[i] += sign * 0.1;
			}
			
			return child;
		}
		
		private void printWeightBias() {
			//TODO print weights and biases in a UI-friendly way
		}
	}
}
