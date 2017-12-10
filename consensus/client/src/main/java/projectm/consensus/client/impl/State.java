package projectm.consensus.client.impl;

import projectm.consensus.client.ConsensusException;

/** @see io.atomix.copycat.server.CopycatServer.State */
public enum State {
	/**
	 * Represents the state of an inactive server.
	 * <p>
	 * All servers start in this state and return to this state when {@link #leave()
	 * stopped}.
	 */
	INACTIVE("INACTIVE"),

	/**
	 * Represents the state of a server that is a reserve member of the cluster.
	 * <p>
	 * Reserve servers only receive notification of leader, term, and configuration
	 * changes.
	 */
	RESERVE("RESERVE"),

	// /**
	// * Represents the state of a server in the process of catching up its log.
	// * <p>
	// * Upon successfully joining an existing cluster, the server will transition
	// to
	// * the passive state and remain there until the leader determines that the
	// * server has caught up enough to be promoted to a full member.
	// */
	// PASSIVE("PASSIVE"),

	/**
	 * Represents the state of a server participating in normal log replication.
	 * <p>
	 * The follower state is a standard Raft state in which the server receives
	 * replicated log entries from the leader.
	 */
	FOLLOWER("FOLLOWER"),

	/**
	 * Represents the state of a server attempting to become the leader.
	 * <p>
	 * When a server in the follower state fails to receive communication from a
	 * valid leader for some time period, the follower will transition to the
	 * candidate state. During this period, the candidate requests votes from each
	 * of the other servers in the cluster. If the candidate wins the election by
	 * receiving votes from a majority of the cluster, it will transition to the
	 * leader state.
	 */
	CANDIDATE("CANDIDATE"),

	/**
	 * Represents the state of a server which is actively coordinating and
	 * replicating logs with other servers.
	 * <p>
	 * Leaders are responsible for handling and replicating writes from clients.
	 * Note that more than one leader can exist at any given time, but Raft
	 * guarantees that no two leaders will exist for the same
	 * {@link Cluster#term()}.
	 */
	LEADER("LEADER");

	private String val;

	private State(String val) {
		this.val = val;
	}

	public static State parse(String val) {
		for (State state : State.values()) {
			if (state.val.equals(val)) {
				return state;
			}
		}
		throw new ConsensusException("State " + val + " not found.");
	}

	public String getVal() {
		return val;
	}
}
