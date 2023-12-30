package com.bw.modeldrive.model;

/**
 * Functional interface to execute some code.
 */
public interface Executor
{
	/**
	 * Executes on the model.
	 *
	 * @param model The model to work on.
	 */
	void execute(Datamodel model);
}
