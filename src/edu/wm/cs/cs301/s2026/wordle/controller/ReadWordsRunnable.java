package edu.wm.cs.cs301.s2026.wordle.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.wm.cs.cs301.s2026.wordle.model.Model;

public class ReadWordsRunnable implements Runnable {

	private static final Logger LOGGER =
			Logger.getLogger(ReadWordsRunnable.class.getName());

	private final Model model;

	public ReadWordsRunnable(Model model) {
		LOGGER.setLevel(Level.INFO);

		try {
			FileHandler fileTxt = new FileHandler("./logging.txt");
			LOGGER.addHandler(fileTxt);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.model = model;
	}

	@Override
	public void run() {
		List<String> wordlist;

		try {
			wordlist = createWordList();
			LOGGER.info("Created word list of " + wordlist.size() + " words.");
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			e.printStackTrace();
			wordlist = new ArrayList<>();
		}

		model.setWordList(wordlist);
		model.initialize();
	}

	
	private InputStream deliverInputStream() {
		String text = "/resources/usa.txt";
		// Original code
		/*
		ClassLoader loader = this.getClass().getClassLoader();
		InputStream stream = loader.getResourceAsStream(text);
		*/
		// https://stackoverflow.com/questions/68314700/why-java-cannot-read-same-resource-file-when-module-info-is-added
		
		InputStream stream = Wordle.class.getResourceAsStream(text);
		
		if (null == stream) {
			System.out.println("Failed to open stream with " + text);
			System.exit(0);
		}
		else 
			System.out.println("Successfully opened inputstream for " + text);
		
		return stream;
	}
	
	private List<String> createWordList() throws IOException {
		int minimum = model.getColumnCount();

		List<String> wordlist = new ArrayList<>();

		
		InputStream stream = deliverInputStream();

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line = reader.readLine();
		while (line != null) {
			line = line.trim();
			if (line.length() == minimum) {
				wordlist.add(line);
			}
			line = reader.readLine();
		}
		reader.close();

		return wordlist;
	}
	
}
