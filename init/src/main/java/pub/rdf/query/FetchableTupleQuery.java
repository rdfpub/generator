package pub.rdf.query;

import org.eclipse.rdf4j.common.annotation.Experimental;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.explanation.Explanation;

import java.util.HashSet;
import java.util.Set;

public class FetchableTupleQuery implements TupleQuery {
    final private TupleQuery query;
    final private Set<IRI> externalResources = new HashSet<>(16);
    final private Set<Var> graphVars = new HashSet<>(16);
    private int dirtyCounter = 0;
    private boolean dirty = false;
    private final int maxDirty;

    public FetchableTupleQuery(final TupleQuery query, final int maxDirty) {
        this.query = query;
        this.maxDirty = maxDirty;
    }

    public Set<IRI> getExternalResources() {
        return null; // TODO implement this
    }

    // Delegated methods
    @Override
    public TupleQueryResult evaluate() throws QueryEvaluationException {
        return query.evaluate();
    }

    @Override
    public void evaluate(TupleQueryResultHandler tupleQueryResultHandler) throws QueryEvaluationException, TupleQueryResultHandlerException {
        query.evaluate(tupleQueryResultHandler);
    }

    @Override
    @Deprecated
    public void setMaxQueryTime(int i) {
        query.setMaxQueryTime(i);
    }

    @Override
    @Deprecated
    public int getMaxQueryTime() {
        return query.getMaxQueryTime();
    }

    @Override
    @Experimental
    public Explanation explain(Explanation.Level level) {
        return query.explain(level);
    }

    @Override
    public void setBinding(String s, Value value) {
        query.setBinding(s, value);
    }

    @Override
    public void removeBinding(String s) {
        query.removeBinding(s);
    }

    @Override
    public void clearBindings() {
        query.clearBindings();
    }

    @Override
    public BindingSet getBindings() {
        return query.getBindings();
    }

    @Override
    public void setDataset(Dataset dataset) {
        query.setDataset(dataset);
    }

    @Override
    public Dataset getDataset() {
        return query.getDataset();
    }

    @Override
    public void setIncludeInferred(boolean b) {
        query.setIncludeInferred(b);
    }

    @Override
    public boolean getIncludeInferred() {
        return query.getIncludeInferred();
    }

    @Override
    public void setMaxExecutionTime(int i) {
        query.setMaxExecutionTime(i);
    }

    @Override
    public int getMaxExecutionTime() {
        return query.getMaxExecutionTime();
    }
}
