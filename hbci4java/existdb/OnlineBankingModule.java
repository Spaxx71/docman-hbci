package hbci4java.existdb;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionDef;
import org.exist.xquery.XPathException; 
import org.exist.xquery.value.FunctionReturnSequenceType;

import java.util.*;
import java.util.Arrays;
import java.util.List;

/*
exist-db extension module
https://exist-db.org/exist/apps/doc/extensions.xml
*/
public class OnlineBankingModule extends AbstractInternalModule {

	public final static String NAMESPACE_URI = "http://exist-db.org/library/hbci4java";
		
	public final static String PREFIX = "hbci4java";
		
	private final static FunctionDef[] functions = {
		new FunctionDef(OnlineBankingFunctionInitialize.signature, OnlineBankingFunctionInitialize.class),
		new FunctionDef(OnlineBankingFunctionTransactions.signature, OnlineBankingFunctionTransactions.class),
		new FunctionDef(OnlineBankingFunctionBankTransfer.signature, OnlineBankingFunctionBankTransfer.class),
		new FunctionDef(OnlineBankingFunctionClose.signature, OnlineBankingFunctionClose.class)
	};
		
	public OnlineBankingModule(Map<String, List<? extends Object>> parameters) throws XPathException {
		super(functions, parameters, false);
	}

	public String getNamespaceURI() {
		return NAMESPACE_URI;
	}

	public String getDefaultPrefix() {
		return PREFIX;
	}

	public String getDescription() {
		return "Functions for hbci4java Online Banking (HBCIplus and FintTS)";
	}

	public String getReleaseVersion() {
		return "0.5.5";
	}

}