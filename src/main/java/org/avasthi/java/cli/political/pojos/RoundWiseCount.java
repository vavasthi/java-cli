package org.avasthi.java.cli.political.pojos;

import java.util.Date;
import java.util.UUID;

public record RoundWiseCount(UUID id,
                             ElectionType electionType,
                             int constituencyNumber,
                             String constituencyName,
                             int round,
                             int totalRounds,
                             String candidate,
                             String party,
                             long votesFromPreviousRounds,
                             long votesCurrentRound,
                             long totalVotes) {
}