package projectm.consensus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projectm.consensus.ApplicationConfig;
import projectm.consensus.ConsensusServer;
import projectm.consensus.State;

@Service
public class DefaultConsensusServer implements ConsensusServer {

	@Autowired
	private ApplicationConfig appConfig;
	private State state;

	@Override
	public void startUp() {
		// TODO Auto-generated method stub
		// RESERVE->FOLLOWER->CANDIDATE->LEADER
		System.out.println("startUp");
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub
		System.out.println("tearDown");
	}

	@Override
	public State transition(State state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State getState() {
		return state;
	}
}
