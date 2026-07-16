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
public class OnlineBankingFunctionBankTransfer extends BasicFunction {


	public final static FunctionSignature signature = new FunctionSignature(
			new QName("arangeBankTransfer", OnlineBankingModule.NAMESPACE_URI, OnlineBankingModule.PREFIX),
			"This funtion arange a bank transfer",
			new SequenceType[] {
					new FunctionParameterSequenceType("AccountID", Type.STRING, Cardinality.EXACTLY_ONE,
							"AccountID ACC123"),
					new FunctionParameterSequenceType("RecipientCountry", Type.STRING, Cardinality.EXACTLY_ONE,
							"RecipientCountry DE"),
					new FunctionParameterSequenceType("RecipientBic", Type.STRING, Cardinality.EXACTLY_ONE,
							"RecipientBic DEDBDEUBA"),
					new FunctionParameterSequenceType("RecipientIban", Type.STRING, Cardinality.EXACTLY_ONE,
							"RecipientIban DE99123456712345678"),
					new FunctionParameterSequenceType("RecipientName", Type.STRING, Cardinality.EXACTLY_ONE,
							"RecipientName Mustermann"),
					new FunctionParameterSequenceType("Date", Type.DATE, Cardinality.EXACTLY_ONE,
							"Date YYYY-MM-DD"),
					new FunctionParameterSequenceType("CurrencyAmount", Type.DOUBLE, Cardinality.EXACTLY_ONE,
							"CurrencyAmount 123.45"),
					new FunctionParameterSequenceType("CurrencyUnit", Type.STRING, Cardinality.EXACTLY_ONE,
							"CurrencyUnit EUR"),
					new FunctionParameterSequenceType("UsageText", Type.STRING, Cardinality.EXACTLY_ONE,
							"UsageText JustForFun")
					},
			new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, "the bank transfer"));

	public OnlineBankingFunctionBankTransfer(XQueryContext context) {
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

		java.text.SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		String AccountID = null;
		String RecipientCountry = null;
		String RecipientBic = null;
		String RecipientIban = null;
		String RecipientName = null;
		Date Date = null;
		Double CurrencyAmount = 0.0;
		String CurrencyUnit = null;
		String UsageText = null;

		try {
			AccountID = args[0].getStringValue();
			RecipientCountry = args[1].getStringValue();
			RecipientBic = args[2].getStringValue();
			RecipientIban = args[3].getStringValue();
			RecipientName = args[4].getStringValue();
			Date = isoDateFormat.parse(args[5].getStringValue());
			CurrencyAmount = Double.parseDouble(args[6].getStringValue());
			CurrencyUnit = args[7].getStringValue();
			UsageText = args[8].getStringValue();
		}
		catch (ParseException e) {
			System.err.println("ParseException");
		}


		String arangeBankTransfer = null;
		try {
			arangeBankTransfer = OnlineBanking.arangeBankTransfer(AccountID, RecipientCountry, RecipientBic, RecipientIban, RecipientName, Date, CurrencyAmount.doubleValue(), CurrencyUnit, UsageText);
			result.add(new StringValue(arangeBankTransfer));
		}
		catch (Exception e) {
			result.add(new StringValue("OnlineBanking.arangeBankTransfer(\""+AccountID+"\", \""+RecipientCountry+"\", \""+RecipientBic+"\", \""+RecipientIban+"\", \""+RecipientName+"\", \""+Date+"\", \""+CurrencyAmount+"\", \""+CurrencyUnit+"\", \""+UsageText+"\")"));
			result.add(new StringValue("Es ist ein unerwarteter Fehler beim Aufruf der Funktion aufgetreten: " + e.toString()));
		}
		return result;
	}
}