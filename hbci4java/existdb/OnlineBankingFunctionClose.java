package hbci4java.existdb;

import java.util.Date;
import java.text.ParseException;

import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;

import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xquery.value.StringValue;

import org.xmldb.api.base.CompiledExpression;

/*
exist-db extension function
https://exist-db.org/exist/apps/doc/extensions.xml
*/
public class OnlineBankingFunctionClose extends BasicFunction {

	public final static FunctionSignature signature = new FunctionSignature(
			new QName("close", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
			"This funtion closes the HBCI/FinTS channel.",
			new SequenceType[] {
					new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE,
							"AccountID ACC123")
					},
			new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "the closing notice"));

	public OnlineBankingFunctionClose(XQueryContext context) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		ValueSequence result = new ValueSequence(); 
		
		// is argument the empty sequence? 
		if (args == null) 
			return	Sequence.EMPTY_SEQUENCE;

		String pAccountID = args[0].getStringValue();

		String bankStatement = null;
		try {
			bankStatement = OnlineBanking.close(pAccountID);
			result.add(new StringValue(bankStatement));
		}
		catch (Exception e) {
			result.add(new StringValue("OnlineBanking.close(\""+pAccountID+"\")"));
			result.add(new StringValue("Es ist ein unerwarteter Fehler beim Aufruf der Funktion aufgetreten: " + e.toString()));
		}
		return result;
	}
}