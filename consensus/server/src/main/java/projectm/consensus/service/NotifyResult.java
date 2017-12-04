package projectm.consensus.service;

import projectm.consensus.State;

public class NotifyResult {

	private boolean accept;
	private State state;

	public NotifyResult() {
		super();
	}

	public NotifyResult(boolean accept, State state) {
		super();
		this.accept = accept;
		this.state = state;
	}

	public NotifyResult(boolean accept, String state) {
		super();
		this.accept = accept;
		setState(state);
	}

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setState(String state) {
		this.state = State.parse(state);
	}

	@Override
	public String toString() {
		return "NotifyResult [accept=" + accept + ", state=" + state + "]";
	}
}
