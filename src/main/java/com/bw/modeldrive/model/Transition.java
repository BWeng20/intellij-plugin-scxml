package com.bw.modeldrive.model;

import java.util.ArrayList;
import java.util.List;

public class Transition
{
	public TransitionId id;
	public int docId;

	// TODO: Possibly we need some type to express event ids
	public final List<String> events = new ArrayList<>();

	public String cond;
	public State source;
	public final java.util.List<State> target = new ArrayList<>();
	public TransitionType transitionType;
	public ExecutableContentId content;

}
