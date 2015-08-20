package nl.cwi.swat.jmh_dscg_benchmarks;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory;

public enum ElementProducer {

	PDB_INTEGER {
		@Override
		public IValue createFromInt(int value) {
			return ValueFactory.getInstance().integer(value);
		}
	};

	public abstract IValue createFromInt(int value);

}