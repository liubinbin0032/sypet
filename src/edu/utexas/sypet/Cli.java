/*
 * Copyright (C) 2017 The SyPet Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.utexas.sypet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Cli {
	private static final Logger log = Logger.getLogger(Cli.class.getName());
	private String[] args = null;
	private Options options = new Options();

	// Options
	private String filename;
	private boolean verbose;
	private int timeout;
	int roundRobinIterationsLimit;
	int roundRobinRange;
	boolean roundRobinFlag;
	int solverLimit;
	boolean pattern;
	boolean constraint;
	int pattern_num;
	boolean inheri;

	public Cli(String[] args) {

		this.args = args;

		options.addOption("h", "help", false, "Shows help.");
		options.addOption("f", "file", true, "Json filename.");
		options.addOption("v", "verb", false, "Verbose.");
		options.addOption("t", "time", true, "Timeout in seconds.");
		options.addOption("rlimit", "robinlimit", true, "Round robin iterations.");
		options.addOption("rrange", "robinRange", true, "Round robin range.");
		options.addOption("r", "robin", false, "Round robin.");
		options.addOption("slimit","solverlimit",true,"Maximum number of optimization iterations.");
		options.addOption("ptn", "pattern", false, "Handling Pattern.");
		options.addOption("cons", "constraint", false, "Including Constraint.");
		options.addOption("pn", "pattern_num", true, "Patterns Number.");
		options.addOption("inheri", "inheri", false, "Handling Inheritage.");

		verbose = false;
		timeout = 1800000;
		roundRobinIterationsLimit = 100;
		roundRobinRange = 2;
		roundRobinFlag = false;
		solverLimit = 5;
		pattern = false;
		constraint = false;
		pattern_num = 40;
		inheri = true;

	}
	
	public int getSolverLimit(){
		return solverLimit;
	}	

	public String getFilename() {
		return filename;
	}

	public boolean getVerbose() {
		return verbose;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getRobinLimit() {
		return roundRobinIterationsLimit;
	}

	public int getRobinRange() {
		return roundRobinRange;
	}

	public boolean getRoundRobin() {
		return roundRobinFlag;
	}
	
	public boolean getPattern() {
		return pattern;
	}
	
	public boolean getConstraint() {
		return constraint;
	}
	
	public int getPatternNum() {
		return pattern_num;
	}
	
	public boolean getInheri() {
		return inheri;
	}
	
	public void printOptions() {
		System.out.println("----------Options");
		System.out.println("Verbose: " + verbose);
		System.out.println("Timeout: " + timeout);
		System.out.println("Round Robin: " + roundRobinFlag);
		System.out.println("Round Robin Iterations: " + roundRobinIterationsLimit);
		System.out.println("Round Robin Range: " + roundRobinRange);
		System.out.println("Solver limit: " + solverLimit);
		System.out.println("Including patterns: " + pattern);
		System.out.println("Including constraint: " + constraint);
		System.out.println("Patterns Number: " + pattern_num);
		System.out.println("Handling with inheritage: " + inheri);
	}

	public void parse() {
		CommandLineParser parser = new BasicParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h"))
				help();
			
			if(cmd.hasOption("slimit")){
				solverLimit = Integer.parseInt(cmd.getOptionValue("slimit"));
			}

			if (cmd.hasOption("file")) {
				filename = cmd.getOptionValue("f");
			} else {
				log.log(Level.SEVERE, "Please provide the location of Json file.");
				help();
			}

			verbose = cmd.hasOption("v");

			if (cmd.hasOption("time")) {
				timeout = Integer.parseInt(cmd.getOptionValue("t")) * 1000;
			}

			if (cmd.hasOption("rlimit")) {
				roundRobinIterationsLimit = Integer.parseInt(cmd.getOptionValue("rlimit"));
			}
			
			if (cmd.hasOption("rrange")) {
				roundRobinRange = Integer.parseInt(cmd.getOptionValue("rrange"));
			}
			
			roundRobinFlag = cmd.hasOption("r");
			
			pattern = cmd.hasOption("ptn");
			
			constraint = cmd.hasOption("cons");
			
//			inheri = cmd.hasOption("inheri");
			
			if(cmd.hasOption("pn")) {
				pattern_num = Integer.valueOf(cmd.getOptionValue("pn"));
			}

		} catch (ParseException e) {
			log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			help();
		}
	}

	private void help() {
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();

		formater.printHelp("Main", options);
		System.exit(0);
	}
}