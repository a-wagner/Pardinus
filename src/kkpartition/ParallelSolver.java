package kkpartition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.instance.Bounds;

public class ParallelSolver {

	// the solver used in the parallelization
	final private Solver solver;

	// the number of parallel processes
	private int ps = 4;
	private boolean it = true;
	
	private PProblemManager manager;
	
	public ParallelSolver(Solver solver) {
		this.solver = solver;
		if (!solver.options().solver().incremental())
			throw new IllegalArgumentException("An incremental solver is required to iterate the configurations.");
	}

	/**
	 * Solves a partitioned problem in parallel.
	 * @param b1 partition 1 bounds
	 * @param b2 partition 2 bounds
	 * @param f1 partition 1 formula
	 * @param f2 partition 2 formula
	 * @return every calculated solution until SAT
	 */
	List<PProblem> solve(Bounds b1, Bounds b2, Formula f1, Formula f2) {
		List<PProblem> sols = runConfigurations(f1,f2,b1,b2);
		return sols;
	}

	/**
	 * Calls the parallel problem manager, and iterates until a SAT solution is returned or
	 * there are no more configurations to explore.
	 * @param configs
	 * @param formula
	 * @param b1
	 * @param b2
	 * @return
	 */
	private List<PProblem> runConfigurations(Formula f1, Formula f2, Bounds b1, Bounds b2) {
		manager = new PProblemManager(f1,f2,b1,b2,solver,ps,it);
		manager.start();
		List<PProblem> problems = new ArrayList<PProblem>();
		boolean done = false;
		// iterates until every configuration as been run (DONE) or a SAT solution is returned
		while (!done) {
			PProblem sol = manager.waitUntil();
			done = sol.equals(PProblem.DONE) || sol.sat();
			if (!sol.equals(PProblem.DONE)) problems.add(sol);
		}
		if(problems.isEmpty())
			problems.add(PProblem.DONE);
		manager.terminate();
		return problems;
	}

	/**
	 * Sets the number of threads that will be launched in parallel.
	 * @param ps
	 */
	public void setPs(int ps) {
		this.ps = ps;
	}

	public void setIt(boolean it) {
		this.it = it;
	}

	public PProblemManager manager() {
		return manager;
	}

}