package dom;

public class DominatorBenchmarkUtils {

	public static enum DominatorBenchmarkEnum {
		CHAMP {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsChamp();
			}
		},
		CHAMP_LAZY {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsChamp_LazyHashCode();
			}
		},
		CLOJURE_LAZY {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsClojure();
			}
		},		
		SCALA {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsScala_Default();
			}
		},
		SCALA_LAZY {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsScala_LazyHashCode();
			}
		};

		public abstract DominatorBenchmark getBenchmark();		
	}

}
