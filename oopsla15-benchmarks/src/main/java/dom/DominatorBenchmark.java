package dom;

import java.util.ArrayList;

import org.eclipse.imp.pdb.facts.ISet;
import org.openjdk.jmh.infra.Blackhole;

public interface DominatorBenchmark {

	void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative);

	ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs);

}
