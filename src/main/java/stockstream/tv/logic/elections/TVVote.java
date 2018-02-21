package stockstream.tv.logic.elections;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang.builder.HashCodeBuilder;
import stockstream.logic.elections.Candidate;

@Data
@AllArgsConstructor
public class TVVote implements Candidate {

    public TVAction action;

    @Override
    public String getLabel() {
        return this.toString();
    }

    @Override
    public String toString() {
        return String.format("!tv %s", action.name()).toLowerCase();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) return true;

        if (!(otherObject instanceof TVVote)) return false;

        final TVVote otherVote = (TVVote) otherObject;

        return this.hashCode() == otherVote.hashCode();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).toHashCode();
    }
}
