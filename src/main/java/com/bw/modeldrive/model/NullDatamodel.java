package com.bw.modeldrive.model;

/**
 * The Null Data Model.
 */
public class NullDatamodel implements Datamodel
{
	private GlobalData globalData = new GlobalData();

	/**
	 * Creates a new Null model.
	 */
	public NullDatamodel()
	{

	}

	@Override
	public GlobalData global()
	{
		return globalData;
	}

	@Override
	public String getName()
	{
		return "Null";
	}

	@Override
	public void initializeDataModel(FiniteStateMachine fsm, State state)
	{
		// Nothing
	}

	@Override
	public void set(String name, Data data)
	{
		// Nothing
	}

	@Override
	public Data get(String name)
	{
		return null;
	}

	@Override
	public void clear()
	{
		// Nothing
	}

	@Override
	public void log(String label, String msg)
	{
		// Nothing
	}

	@Override
	public String execute(FiniteStateMachine fsm, String script)
	{
		return null;
	}

	@Override
	public void executeForEach(FiniteStateMachine fsm, String arrayExpression, String item, String index, Executor executeBody)
	{

	}

	@Override
	public boolean executeCondition(FiniteStateMachine fsm, String script)
	{
		//  @TODO: support "In(id)"
		return false;
	}

	@Override
	public void executeContent(FiniteStateMachine fsm, ExecutableContent content)
	{
		// Nothing
	}
}
