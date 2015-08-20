package dom;

import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;

public class AllDominatorsRunner {

	public static final boolean LOG_BINARY_RESULTS = false;
	public static final boolean LOG_TEXTUAL_RESULTS = false;
	
	public static final String DATA_SET_SINGLE_FILE_NAME = "data/single.bin";
	
	public static final String DATA_SET_SAMPLED_FILE_NAME = "data/wordpress-cfgs-as-graphs-sampled.bin";
	public static final String DATA_SET_FULL_FILE_NAME = "data/wordpress-cfgs-as-graphs.bin";
	
	public static final String CURRENT_DATA_SET_FILE_NAME = DATA_SET_FULL_FILE_NAME;

	public static final IMap CURRENT_DATA_SET;

	static {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		try {
			CURRENT_DATA_SET = (IMap) new BinaryValueReader().read(vf,
					new FileInputStream(CURRENT_DATA_SET_FILE_NAME));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.err.println("Global data initialized.");
		System.err.println();
	}

}
