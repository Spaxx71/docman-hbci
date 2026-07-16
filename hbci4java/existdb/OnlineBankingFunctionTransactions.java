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
public class OnlineBankingFunctionTransactions extends BasicFunction {


	public final static FunctionSignature signature = new FunctionSignature(
			new QName("getTransactions", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
			"This funtion gets the account activity",
			new SequenceType[] {
					new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE,
							"AccountID ACC123"),
					new FunctionParameterSequenceType("StartDate", Type.DATE, Cardinality.EXACTLY_ONE,
							"Start-date YYYY-MM-DD"),
					new FunctionParameterSequenceType("EndDate", Type.DATE, Cardinality.EXACTLY_ONE,
							"End-date YYYY-MM-DD")
					},
			new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "the account activity"));

	public OnlineBankingFunctionTransactions(XQueryContext context) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
		ValueSequence result = new ValueSequence(); 
		
		// is argument the empty sequence? 
		if (args == null) 
			return	Sequence.EMPTY_SEQUENCE; 

		if (false) {
			// iterate through the argument sequence and echo each item 
			for (SequenceIterator i = args[0].iterate(); i.hasNext();) { 
				String str = i.nextItem().getStringValue(); 
				result.add(new StringValue("Param: " + str));
			}
		}

		/*
			MemTreeBuilder builder = context.getDocumentBuilder();
			builder.startDocument();
			builder.startElement(new QName("hello", null, null), null);
			builder.characters(args[0].getStringValue());
			builder.endElement();
			builder.endDocument();
			return (Sequence) builder.getDocument().getDocumentElement();
		*/

		java.text.SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		Date pStartDate = null;
		Date pEndDate = null;
		String pAccountID = args[0].getStringValue();
		try {
			pStartDate = isoDateFormat.parse(args[1].getStringValue());
			pEndDate = isoDateFormat.parse(args[2].getStringValue());
		}
		catch (ParseException e) {
			System.err.println("ParseException");
		}


		String bankStatement = null;
		try {
			bankStatement = OnlineBanking.getTransactions(pAccountID, pStartDate, pEndDate);
			result.add(new StringValue(bankStatement));
		}
		catch (Exception e) {
			result.add(new StringValue("OnlineBanking.getTransactions(\""+pAccountID+"\", \""+pStartDate+"\", \""+pEndDate+"\")"));
			result.add(new StringValue("Es ist ein unerwarteter Fehler beim Aufruf der Funktion aufgetreten: " + e.toString()));
		}
		return result;
	}
}