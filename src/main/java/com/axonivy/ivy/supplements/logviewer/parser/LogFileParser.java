package com.axonivy.ivy.supplements.logviewer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LogFileParser {
	private File file;
	private List<MainLogEntry> logEntries = new ArrayList<>();
	private String url;

	public LogFileParser(File file) {
		this.file = file;
	}
	public LogFileParser(String url) {
		this.url = url;
		createNewEntry(" ");
	}

	public List<MainLogEntry> parse() throws IOException {
		// TODO check if parsing like that is performant enough, else only read headers
		// TODO also support other encodings
		try (Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.ISO_8859_1)) {
			lines.forEachOrdered(line -> {

				if (!line.startsWith(" ")) {
					createNewEntry(line);
				} else {
					appendToLastEntry(line);
				}

				// TODO check that last entry before EOF is included
			});
		}

		return logEntries;
	}
	
	public List<MainLogEntry> parseURL() throws IOException {
		createNewEntry(" ");
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		int responseCode = connection.getResponseCode();
		InputStream inputStream;
		if (200 <= responseCode && responseCode <= 299) {
			inputStream = connection.getInputStream();
		} else {
			inputStream = connection.getErrorStream();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

		String currentLine;

		while ((currentLine = in.readLine()) != null)
			if (!currentLine.startsWith(" ")) {
				createNewEntry(currentLine);
			} else {
				appendToLastEntry(currentLine);
			}

		in.close();
		return logEntries;
	}

	private void createNewEntry(String line) {
		String[] parts = line.split(" ", 3);

		MainLogEntry entry = null;
		if (parts.length >= 2) {
			entry = new MainLogEntry(line, parts[0], LogLevel.fromValue(parts[1]));
		} else {
			if (!line.trim().equals("") || !parts[0].trim().equals("")) {
				entry = new MainLogEntry(line, parts[0], LogLevel.DEBUG /*
																		 * we set unparsable entries
																		 * to DEBUG
																		 */);
			}
		}
		if (entry != null) {
			logEntries.add(entry);
		}
	}

	private void appendToLastEntry(String line) {
		MainLogEntry entry = logEntries.get(logEntries.size() - 1);
		if (entry == null) {
			entry = new MainLogEntry("Parse ERROR", "Parse Error", LogLevel.DEBUG);
		}

		entry.addDetailLogEntry(line);
	}
}
