package hbci4java.existdb;

import java.io.File;
import java.io.Console;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.GV_Result.GVRSaldoReq;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIVersion;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Calendar;
import java.nio.charset.StandardCharsets;

/*
Liste aller GV https://github.com/willuhn/hbci4java/tree/master/src/org/kapott/hbci/GV 
*/


/**
 * Demo zum Abruf von Umsaetzen per PIN/TAN-Verfahren.
 * 
 * Die folgende Demo zeigt mit dem minimal noetigen Code, wie eine
 * Umsatz-Abfrage fuer ein Konto durchgefuehrt werden kann. Hierzu wird der
 * Einfachheit halber das Verfahren PIN/TAN verwendet, da es von den meisten
 * Banken unterstuetzt wird.
 * 
 * Trage vor dem Ausfuehren des Programms die Zugangsdaten zu deinem Konto ein.
 */
public class OnlineBankingAccount {

    /**
     * Dies ist der Message Handler.
     */
    private MessageHandler messageHandler = null;

    /**
     * Die zu verwendende HBCI-Version.
     */
    private final static HBCIVersion VERSION = HBCIVersion.HBCI_300;

    // Das Handle ist die eigentliche HBCI-Verbindung zum Server
    HBCIHandler handle = null;
    HBCIPassport passport = null;
    Konto konto = null;

    String Country; 
    String Blz;
    String Bic;
    String Iban;
    Integer Port;
    String BankAccount;
    String BankUserID;
    String CustomerID;
    String Secret;
    Integer AuthType; 
    String Pin; 
    String PassportURL;
    String MessageUrl; 
    String ResponseUrl; 
    String AuthHeaderValue;
    Boolean Debug;

    /*
Aufbau des 
Geschäftsvorfall-Codes:
1. Stelle : Geschäftssparte
    0 = Inlandszahlungsverkehr
    1 = Inlandszahlungsverkehr
    2 = Auslandsgeschäft
    3 = Wertpapiergeschäft
    4 = Devisengeschäft
    5 = MAOBE
    6 = Reserve
    7 = Reserve
    8 = Sonstige
    9 = Unstrukturierte Belegung
2. Stelle: Geschäftsvorfallart gemäß nachfolgender Tabelle
3. Stelle: Geschäftsvorfallart gemäß nachfolgender Tabelle
*/
    private static final HashMap<String, String> gvcMap;
    static {
        gvcMap = new HashMap<>();
        gvcMap.put("008", "Dauerauftrag Belastung");
        gvcMap.put("082", "Einzahlungen");
        gvcMap.put("083", "Auszahlungen");
        gvcMap.put("104", "Einzelbuchung Soll, SEPA B2B");
        gvcMap.put("105", "Einzelbuchung Soll, SEPA Core");
        gvcMap.put("108", "Rückbelastung Soll, SEPA B2B");
        gvcMap.put("109", "Rückbelastung Soll, SEPA Core");
        gvcMap.put("116", "Einzelbuchung Soll");
        gvcMap.put("118", "Einzelbuchung Soll");
        gvcMap.put("119", "Einzelbuchung Spende (SEPA Purpose Code: CHAR)");
        gvcMap.put("152", "Einzelbuchung Dauerauftrag");
        gvcMap.put("153", "Einzelbuchung Haben, Lohn, Gehalts, Rentengutschrift");
        gvcMap.put("154", "Einzelbuchung Haben, Vermögenswirksame Leistungen");
        gvcMap.put("156", "Einzelbuchung Haben, Überweisung öffentlicher Kassen");
        gvcMap.put("159", "Retoure Haben für unanbringliche SEPA Überweisung");
        gvcMap.put("160", "Rücküberweisung (wegen Rückruf)");
        gvcMap.put("166", "Einzelbuchung Haben");
        gvcMap.put("168", "Einzelbuchung Haben");
        gvcMap.put("169", "Einzelbuchung Spende(SEPA Purpose Code: CHAR)");
        gvcMap.put("171", "Einzelbuchung Haben, SEPA Core");
        gvcMap.put("174", "Einzelbuchung Haben, SEPA B2B");
        gvcMap.put("177", "Online Einzelbuchung Soll");
        gvcMap.put("181", "Wiedergutschrift Haben, SEPA Core");
        gvcMap.put("184", "Wiedergutschrift Haben, SEPA B2B");
        gvcMap.put("188", "Sammler Soll [Reserviert]");
        gvcMap.put("189", "Sammler Haben");
        gvcMap.put("191", "Sammler Soll");
        gvcMap.put("192", "Sammler Haben Core");
        gvcMap.put("193", "Soll, SEPA Reversal");
        gvcMap.put("194", "Sammler Haben");
        gvcMap.put("195", "Sammler Soll, SEPA Core");
        gvcMap.put("196", "Sammler Haben, SEPA B2B");
        gvcMap.put("197", "Sammler Soll, SEPA B2B");
        gvcMap.put("801", "Scheckkarte");
        gvcMap.put("802", "Scheckheft");
        gvcMap.put("803", "Depotverwahrung");
        gvcMap.put("804", "Dauerauftragsgebühren");
        gvcMap.put("805", "Abschluß");
        gvcMap.put("806", "Porto/Zustellgebühren");
        gvcMap.put("807", "Preise/Spesen");
        gvcMap.put("808", "Gebühren");
        gvcMap.put("809", "Provisionen");
        gvcMap.put("810", "Mahngebühren");
        gvcMap.put("811", "Kreditkosten");
        gvcMap.put("812", "Stundungszinsen");
        gvcMap.put("813", "Disagio");
        gvcMap.put("814", "Zinsen");
        gvcMap.put("815", "Kapitalisierte Zinsen");
        gvcMap.put("816", "Zinssatzänderung");
        gvcMap.put("817", "Zinsberichtigung");
        gvcMap.put("818", "Abbuchung");
        gvcMap.put("819", "Bezüge");
        gvcMap.put("820", "Übertrag");
        gvcMap.put("821", "Telefon");
        gvcMap.put("822", "Auszahlplan");
        gvcMap.put("823", "Festgeld");
        gvcMap.put("824", "Leihgeld");
        gvcMap.put("825", "Universaldarlehn");
        gvcMap.put("826", "Dynamisches Sparen");
        gvcMap.put("827", "Überschußsparen");
        gvcMap.put("828", "Sparbrief");
        gvcMap.put("829", "Sparplan");
        gvcMap.put("830", "Bonus");
        gvcMap.put("831", "Alte Rechnung");
        gvcMap.put("832", "Hypothek");
        gvcMap.put("833", "Cash Concentrating-Buchung Hauptkonten");
        gvcMap.put("834", "Cash Concentrating-Avisenformation für Nebenkosten");
        gvcMap.put("835", "sonstige nicht definierte GV-Arten");
        gvcMap.put("836", "Reklamationsbuchung");
        gvcMap.put("899", "Storno");
        gvcMap.put("999", "Sonstiges");
    };

    private static final HashMap<String, String> bucMap;
    static {
        bucMap = new HashMap<>();
        // -- List of HBCI UCs --
        bucMap.put("DKBKD", "Detailansicht Bausparkonto (Sparkasse)");
        bucMap.put("DKBKU", "Kontenübersicht Bausparkonten");
        bucMap.put("DKBUM", "Umsätze Bausparvertrag anfordern");
        bucMap.put("DKDIH", "Liste Handelspartner (Makert Maker) anfordern");
        bucMap.put("DKFAF", "Deka-Depot-Fondsaufstellung anfordern");
        bucMap.put("DKKKS", "Abruf Kreditkartensalden");
        bucMap.put("DKKKA", "Abruf von elektronischen Kontoauszügen");
        bucMap.put("DKKAU", "Abruf von elektronischen Kontoauszügen");
        bucMap.put("DKKKU", "Abruf Kreditkartenumsätze");
        bucMap.put("DKPSP", "PIN sperren");
        bucMap.put("DKPAE", "PIN online ändern");
        bucMap.put("DKQUT", "Neue Quote anfordern");
        bucMap.put("DKQUO", "Quote annehmen");
        bucMap.put("DKWGV", "GuV-Analyse");
        bucMap.put("DKWLV", "Liquiditätsvorschau");
        bucMap.put("DKWOP", "Wertpapierorder");
        bucMap.put("DKBAZ", "Bestand der ZV Dateien abrufen");
        bucMap.put("DKZDF", "Freigabe ZV-Dateien");
        bucMap.put("DKZDL", "Löschung ZV-Dateien");
        bucMap.put("GKVPD", "FinTS Verbund-Finanzstatus Detailanzeige");
        bucMap.put("GKVPU", "FinTS Verbund-Finanzstatus Übersicht");
        bucMap.put("HIISA", "Übermittlung eines öffentlichen Schlüssels");
        bucMap.put("HIKIM", "Kreditinstitutsmeldung");
        bucMap.put("HIPRO", "Statusprotokoll rückmelden");
        bucMap.put("HIPROS", "Statusprotokoll Parameter");
        bucMap.put("HISYN", "Synchronisierungsantwort");
        bucMap.put("HKANA", "Alias (Anmeldename) anlegen / ändern");
        bucMap.put("HKANL", "Alias (Anmeldename) löschen");
        bucMap.put("HKAOM", "EU-Überweisung oder Meldeteil");
        bucMap.put("HKAUB",
                "Zahlungsauftrag im Außenwirtschaftsverkehr");
        bucMap.put("HKAZK", "Kartenanzeige");
        bucMap.put("HKBBS", "Bestand terminierter SEPA-Firmeneinzellastschriften anfordern");
        bucMap.put("HKBMB", "Bestand terminierter SEPA-Firmensammellastschriften anfordern");
        bucMap.put("HKCAN", "Kontoumsätze/neue Umsätze (camt)");
        bucMap.put("HKCAZ", "Kontoumsätze im Format camt anfordern / Zeitraum");
        bucMap.put("HKCCM", "SEPA-Sammelüberweisung");
        bucMap.put("HKCCS", "SEPA-Einzelüberweisung");
        bucMap.put("HKCDA", "SEPA-Dauerauftragsänderungsvormerkungen");
        bucMap.put("HKCDN", "FinTS SEPA Dauerüberweisungen ändern");
        bucMap.put("HKCUB", "Bestand der zugelassenen Empfängerkonten für SEPA-Umbuchungen anfordern");
        bucMap.put("HKCUM", "SEPA-Umbuchung auf Referenzkonto");
        bucMap.put("HKCSA", "term. SEPA-Überweisungen ändern");
        bucMap.put("HKCSB", "term. SEPA-Überweisungen abrufen");
        bucMap.put("HKCMB", "term. SEPA-Überweisung Bestand abrufen");
        bucMap.put("HKCME", "term. SEPA-Sammelüberweisung");
        bucMap.put("HKCML", "term. SEPA-Überweisung Bestand löschen");
        bucMap.put("HKCSL", "term. SEPA-Überweisungen löschen");
        bucMap.put("HKCSU", "SEPA-Eilüberweisung");
        bucMap.put("HKCVB", "Bestand vorbereiteter SEPA-Überweisungen abfragen");
        bucMap.put("HKDAB", "Dauerauftragsbestand holen");
        bucMap.put("HKDAE", "Dauerauftrag einrichten");
        bucMap.put("HKDAL", "Dauerauftrag löschen");
        bucMap.put("HKDAN", "Dauerauftrag ändern");
        bucMap.put("HKDBM", "Bestand terminierter SEPA-Sammellastschriften anfordern");
        bucMap.put("HKDBS", "Bestand terminierter SEPA Einzellastschriften anfordern");
        bucMap.put("HKDSB", "FinTS SEPA Bestand rückgabefähiger Lastschriften");
        bucMap.put("HKDSC", "Terminierte SEPA-COR1-Einzellastschrift einreichen");
        bucMap.put("HKDSW", "SEPA-Lastschriftwiderspruch einreichen");
        bucMap.put("HKDMC", "Terminierte SEPA-COR1-Sammellastschrift einreichen");
        bucMap.put("HKDDE", "SEPA-Dauereinzellastschrift einrichten");
        bucMap.put("HKDDN", "SEPA-Dauereinzellastschrift ändern");
        bucMap.put("HKDDU", "SEPA-Dauereinzellastschrift aussetzen");
        bucMap.put("HKDDA", "SEPA-Dauereinzellastschriftänderungsvormerkungen abrufen");
        bucMap.put("HKDDB", "SEPA-Dauereinzellastschriftbestand andordern");
        bucMap.put("HKDDL", "SEPA-Dauereinzellastschriftbestand löschen");
        bucMap.put("HKDWP", "Depotaufstellung anfordern");
        bucMap.put("HKDWU", "Depotumsätze anfordern");
        bucMap.put("HKECA", "Elektronischer Kontoauszug (camt), z. B. für Sammlerauflösung");
        bucMap.put("HKEKA", "elektronischen Kontoauszug abholen");
        bucMap.put("HKEKB", "Elektronischen Kontoauszug beantragen");
        bucMap.put("HKEKP", "elektronischen Kontoauszug abholen PDF");
        bucMap.put("HKEND", "Dialogende");
        bucMap.put("HKFDA", "Finanzdatenformat anfordern");
        bucMap.put("HKFDB", "Bearbeitungsstatus Finanzdatenformat anfordern");
        bucMap.put("HKFDL", "Finanzdatenformatliste anfordern");
        bucMap.put("HKFDS", "Finanzdatenformat senden");
        bucMap.put("HKFGA", "Festgeldanlage ändern");
        bucMap.put("HKFGB", "Festgeldbestand anfordern");
        bucMap.put("HKFGK", "Festgeldkonditionen anfordern");
        bucMap.put("HKFGN", "Festgeldneuanlage");
        bucMap.put("HKFGP", "Festgeldanlage prolongieren");
        bucMap.put("HKFGW", "Festgeldneuanlage widerrufen");
        bucMap.put("HKFPO", "Festpreisorder (Eigenhandel)");
        bucMap.put("HKFRD", "Freistellungsdaten abfragen");
        bucMap.put("HKIDN", "Identifikation");
        bucMap.put("HKIPE", "Terminierte SEPA-Instant Payment Sammelzahlung");
        bucMap.put("HKIPM", "SEPA-Instant Payment Sammelzahlung");
        bucMap.put("HKIPZ", "Instant Payment Zahlung (Echtzeitüberweisung)");
        bucMap.put("HKISA", "Anforderung eines öffentlichen Schlüssels");
        bucMap.put("HKISS", "SEPA-Instant Payment Sammelzahlung Status");
        bucMap.put("HKKAN", "Neue Kontoumsätze holen");
        bucMap.put("HKKAU", "Übersicht Kontoauszüge holen");
        bucMap.put("HKKAZ", "Kontoumsätze holen");
        bucMap.put("HKKDM", "Kundenmeldung");
        bucMap.put("HKKIF", "Kontoinformation abrufen");
        bucMap.put("HKLAS", "Lastschrift");
        bucMap.put("HKLSW", "Lastschriftwiderspruch");
        bucMap.put("HKNEZ", "Neuemission zeichnen (Neuemissionen)");
        bucMap.put("HKOAN", "Orderanzeige anfordern");
        bucMap.put("HKPAE", "PIN online ändern");
        bucMap.put("HKPPD", "Prepaid-Karte (Handy) aufladen");
        bucMap.put("HKPRO", "Statusprotokoll holen");
        bucMap.put("HKQTG",
                "elektronische Quittung für den erfolgreichen Abruf von elektronischen Kontoauszügen senden");
        bucMap.put("HKSAL", "Saldo holen");
        bucMap.put("HKSLA", "Sammellastschrift");
        bucMap.put("HKSLB", "Terminierte Sammellastschrift Bestand");
        bucMap.put("HKSLE", "Terminierte Sammellastschrift einreichen");
        bucMap.put("HKSLL", "Terminierte Sammellastschrift löschen");
        bucMap.put("HKSPA", "SEPA-Kontoverbindung anfordern");
        bucMap.put("HKSSP", "Schlüsselsperre");
        bucMap.put("HKSTP", "Euro-Überweisung");
        bucMap.put("HKSUB", "Sammelüberweisung");
        bucMap.put("HKSYN", "Synchronisierungsnachricht");
        bucMap.put("HKTAB", "TAN-Medien-Bestand anzeigen");
        bucMap.put("HKTAU", "TAN-Generator an-/ummelden");
        bucMap.put("HKTAZ",
                "TAN-Verbrauchsinformationen anfordern / Liste der bereits verwendeten TAN-Nummern anzeigen");
        bucMap.put("HKTML", "TAN-Medium deaktivieren/löschen");
        bucMap.put("HKTSB", "Terminierte Sammelüberweisung Bestand");
        bucMap.put("HKTSE", "Terminierte Sammelüberweisung einreichen");
        bucMap.put("HKTSL", "Terminierte Sammelüberweisung löschen");
        bucMap.put("HKTUA", "Terminüberweisung ändern");
        bucMap.put("HKTUB", "Bestand an Terminüberweisungen holen");
        bucMap.put("HKTUE", "Terminüberweisung einrichten");
        bucMap.put("HKTUL", "Terminüberweisung löschen");
        bucMap.put("HKUEB", "Einzelüberweisung");
        bucMap.put("HKUMB", "Umbuchung");
        bucMap.put("HKUTA", "Übersicht institutsverwalteter Aufträge");
        bucMap.put("HKVVB", "Verarbeitungsvorbereitung");
        bucMap.put("HKWEK", "Willenserklärung des Kunden");
        bucMap.put("HKWFO", "Fondsorder (Fonds)");
        bucMap.put("HKWPD", "Wertpapierdepotaufstellung holen");
        bucMap.put("HKWPU", "Depotumsätze holen");
        bucMap.put("HKWPK", "Wertpapierkursabfrage");
        bucMap.put("HKWPO", "Wertpapierorder (Aktien, Renten, Optionsscheine)");
        bucMap.put("HKWPR", "Wertpapierreferenznummern holen");
        bucMap.put("HKWPS", "Orderstreichung");
        bucMap.put("HKWSD", "Wertpapierstammdaten anfordern");
        bucMap.put("HKWSO", "Orderstatus anfordern");
        // -- List of SEPA UCs --
        bucMap.put("HKSPA", "SEPA Kontoverbindungen anfordern");
        bucMap.put("HKCCS", "SEPA-Einzelüberweisung einreichen");
        bucMap.put("HKCSE", "Terminierte SEPA-Überweisung");
        bucMap.put("HKCSA", "Terminierte SEPA-Überweisung ändern");
        bucMap.put("HKCSB", "Bestand terminierter SEPA-Überweisungen anfordern");
        bucMap.put("HKCSL", "Löschung terminierte SEPA-Überweisungen");
        bucMap.put("HKCDE", "SEPA-Dauerauftrag einrichten");
        bucMap.put("HKCDN", "SEPA-Dauerauftragsänderung");
        bucMap.put("HKCDU", "SEPA-Dauerauftragsaussetzung");
        bucMap.put("HKCDB", "SEPA-Dauerauftragsbestand anfordern");
        bucMap.put("HKCDA", "SEPA-Dauerauftragsänderungsvormerkungen anfordern");
        bucMap.put("HKCDL", "SEPA-Dauerauftragslöschung");
        bucMap.put("HKCVE", "Vorbereitete SEPA-Überweisung anlegen");
        bucMap.put("HKCVA", "Vorbereitete SEPA-Überweisung ändern");
        bucMap.put("HKCVB", "Bestand vorbereitete SEPA-Überweisung anzeigen");
        bucMap.put("HKCVL", "Vorbereitete SEPA-Überweisung löschen");
        bucMap.put("HKEUE", "EURO-Expresszahlungen online übertragen");
        bucMap.put("HKIPZ", "SEPA-Instant Payment Zahlung");
        bucMap.put("HKIPS", "SEPA-Instant Payment Status anfordern");
        bucMap.put("HKCCM", "SEPA-Sammelüberweisung einreichen");
        bucMap.put("HKCME", "Einreichung terminierter SEPA-Sammelüberweisung");
        bucMap.put("HKCMB", "Bestand terminierter SEPA-Sammelüberweisungen abrufen");
        bucMap.put("HKCML", "Löschung terminierter SEPA-Sammelüberweisungen");
        bucMap.put("HKCDS", "SEPA Einzellastschrift einreichen");
        bucMap.put("HKDSW", "SEPA-Lastschriftwiderspruch");
        bucMap.put("HKDSB", "Bestand rückgabefähiger SEPA-Lastschriften");
        bucMap.put("HKDSE", "Einreichung terminierter SEPA-Einzellastschriften");
        bucMap.put("HKDSA", "Änderung terminierter SEPA-Einzellastschriften");
        bucMap.put("HKDBS", "Bestand terminierter SEPA-Einzellastschriften abrufen");
        bucMap.put("HKDSL", "Terminierte SEPA-Einzellastschriften löschen");
        bucMap.put("HKBSE", "Einreichung terminierter SEPA-Firmeneinzellastschriften");
        bucMap.put("HKBSA", "Änderung terminierter SEPA-Firmeneinzellastschriften");
        bucMap.put("HKBBS", "Bestand terminierter SEPA-Firmeneinzellastschriften abrufen");
        bucMap.put("HKBSL", "Terminierte SEPA-Firmeneinzellastschriften löschen");
        bucMap.put("HKDME", "Terminierte SEPA Sammellastschrift einreichen");
        bucMap.put("HKBME", "Terminierte SEPA Firmensammellastschrift einreichen");
        bucMap.put("HKDMB", "Bestand terminierter SEPA-Sammellastschriften abrufen");
        bucMap.put("HKDML", "Löschung terminierter SEPA-Sammellastschriften");
        bucMap.put("HKBMB", "Bestand terminierter SEPA Firmensammellastschriften anfordern");
        bucMap.put("HKBML", "Terminierte SEPA Firmensammellastschrift löschen");
        bucMap.put("HKCDM", "SEPA Sammellastschrift einreichen");
        bucMap.put("HKPOF", "Postfach Nachrichtenliste anfordern");
        bucMap.put("HKKAA", "Postfach Nachrichten anfordern");
        // -- sonstiges --
        bucMap.put("HFRD", "Abfrage Freistellungsdaten");
        bucMap.put("HKTAN", "Zwei-Schritt-TAN-Einrichtung");
        bucMap.put("HKPSP", "PIN-Sperre");
        bucMap.put("HKPSA", "PIN-Sperre aufheben");
    }

    private static final java.text.SimpleDateFormat isoDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

    private static final java.text.SimpleDateFormat isoDateTimeFormat = new java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'hh:mm:ss");

    public OnlineBankingAccount (String Country, Integer Port, String Blz, String Bic, String Iban, String BankAccount, String BankUserID, String CustomerID,
        String Secret, Integer AuthType, String Pin, String PassportURL, 
        String MessageUrl, String ResponseUrl, String AuthHeaderValue, Boolean Debug){

        this.Country = Country;
        this.Port = Port;
        this.Blz = Blz;
        this.Bic = Bic;
        this.Iban = Iban;
        this.BankAccount = BankAccount;
        this.BankUserID = BankUserID;
        this.CustomerID = CustomerID;
        this.Secret = Secret;
        this.Pin = Pin;
        this.AuthType = AuthType;
        this.PassportURL = PassportURL;
        this.MessageUrl = MessageUrl;
        this.ResponseUrl = ResponseUrl;
        this.AuthHeaderValue = AuthHeaderValue;
        this.Debug = Debug;

        if (MessageUrl == null || ResponseUrl == null)
            messageHandler = new ConsoleMessageHandler();
        else
            messageHandler = new HttpMessageHandler(MessageUrl, ResponseUrl, AuthHeaderValue);
    } 


    /**
     * initialize
     * 
     * @param args
     * @throws Exception
     */
    // https://groups.google.com/g/hbci4java/c/Ug4Hxu7npRY
    public String initialize() throws Exception {
        StringBuilder sb = new StringBuilder();

        // HBCI4Java initialisieren
        // In "props" koennen optional Kernel-Parameter abgelegt werden, die in der
        // Klasse
        // org.kapott.hbci.manager.HBCIUtils (oben im Javadoc) beschrieben sind.
        //Properties props = new Properties();
        //HBCIUtils.init(props, new MyHBCICallback());
        HBCIUtils.init(null,null,new MyHBCICallback());

        /*
  private final static Map<Integer,String> settings = new HashMap<Integer,String>()
  {{
    // Demo-Konto bei der ApoBank
    put(HBCICallback.NEED_COUNTRY,         "DE");
    put(HBCICallback.NEED_BLZ,             "30060601");
    put(HBCICallback.NEED_CUSTOMERID,      "0001956434");
    put(HBCICallback.NEED_FILTER,          "Base64");
    put(HBCICallback.NEED_HOST,            "hbcibanking.apobank.de/fints_pintan/receiver");
    put(HBCICallback.NEED_PASSPHRASE_LOAD, "test");
    put(HBCICallback.NEED_PASSPHRASE_SAVE, "test");
    put(HBCICallback.NEED_PORT,    &nbs
  }}
    */

        /** HBCI Initialisierung per Code:
HBCIUtils.init(null,null,new MyCallback());
HBCIUtils.setParam("client.connection.localPort",null);
HBCIUtils.setParam("log.loglevel.default","2");
HBCIUtils.setParam("client.passport.default","PinTan");
HBCIUtils.setParam("client.passport.PinTan.filename","my_passport_pintan.dat");
HBCIUtils.setParam("client.passport.PinTan.checkcert","1");
HBCIUtils.setParam("client.passport.PinTan.certfile",null);
HBCIUtils.setParam("client.passport.PinTan.init","1");
HBCIUtils.setParam("client.passport.hbciversion.default","plus");
passport=AbstractHBCIPassport.getInstance();
passport.clearBPD(); // test
passport.clearUPD(); // test
hbciHandle = new HBCIHandler(HBCIUtils.getParam("client.passport.hbciversion.default"),passport); 
        */

        // In der Passport-Datei speichert HBCI4Java die Daten des Bankzugangs
        // (Bankparameterdaten, Benutzer-Parameter, etc.).
        // Die Datei kann problemlos geloescht werden. Sie wird beim naechsten mal
        // automatisch neu erzeugt,
        // wenn der Parameter "client.passport.PinTan.init" den Wert "1" hat (siehe
        // unten).
        // Wir speichern die Datei der Einfachheit halber im aktuellen Verzeichnis.
        final File passportFile = new File(PassportURL);
        System.out.println("### passportURL '" + PassportURL + "'");

        // Wir setzen die Kernel-Parameter zur Laufzeit. Wir koennten sie alternativ
        // auch oben in "props" setzen.
        HBCIUtils.setParam("client.passport.default", "PinTan"); // Legt als Verfahren PIN/TAN fest.
        HBCIUtils.setParam("client.passport.PinTan.init", "1"); // Stellt sicher, dass der Passport initialisiert wird


        // Erzeugen des Passport-Objektes.
        // HBCIPassportDDV (für Chipkarte), HBCIPassportPinTan (für Zugang über das PIN/TAN-Verfahren), HBCIPassportRDHNew für Zugang über RDH mit Datei
        passport = AbstractHBCIPassport.getInstance(passportFile);

        if (passport.getAccounts() == null || passport.getAccounts().length == 0)
            System.out.println("### passport is empty.");
        else 
            System.out.println("### passport for accounts '" + passport.getAccounts()[1] + "' opened.");

        // Konfigurieren des Passport-Objektes.
        // Das kann alternativ auch alles ueber den Callback unten geschehen

        // Das Land.
        passport.setCountry(Country);

        // Server-Adresse angeben. Koennen wir entweder manuell eintragen oder direkt
        // von HBCI4Java ermitteln lassen
        BankInfo info = HBCIUtils.getBankInfo(Blz.toString());
        passport.setHost(info.getPinTanAddress());
        // passport.setHost("fints.deutsche-bank.de");

        // TCP-Port des Servers. Bei PIN/TAN immer 443, da das ja ueber HTTPS laeuft.
        passport.setPort(Integer.parseInt(Port.toString()));

        // Art der Nachrichten-Codierung. Bei Chipkarte/Schluesseldatei wird
        // "None" verwendet. Bei PIN/TAN kommt "Base64" zum Einsatz.
        passport.setFilterType("Base64");

        try {
            // Verbindung zum Server aufbauen
            try {
                handle = new HBCIHandler(VERSION.getId(), passport);
            }
            catch (Exception e) {
                messageHandler.sendMessage("Server HBCI-connect failed: " + e.toString() + ". Passport: " + passport);
                return error(passport, handle, "Server HBCI-connect failed. ");
            }

            messageHandler.sendMessage("HBCI-Server passport: " + passport);

            // Wir verwenden einfach das erste Konto, welches wir zur Benutzerkennung finden
            Konto[] konten = passport.getAccounts();
            if (konten == null || konten.length == 0) {
                return error(passport, handle, "No accouts found. ");
            } else {
                messageHandler.sendMessage("Anzahl Konten: " + konten.length);

                for (int i = 0; i < konten.length; i++) {
                    if (BankAccount.equals(new StringBuilder(konten[i].number).toString()))
                        konto = konten[i];
                    else if (BankAccount.equals(new StringBuilder(konten[i].number).append(konten[i].subnumber).toString()))
                        konto = konten[i];
                }
            }

            if (konto == null) {
                String errorMsg = "No account '" + stringValue(BankAccount) + "' given. Possible accounts are:";
                for (int i = 0; i < konten.length; i++) {
                    errorMsg += " Konto #" + i + ":" + stringValue(konten[i].number);
                    errorMsg += " bzw. #" + stringValue(konten[i].number) + stringValue(konten[i].subnumber);
                }
                return error(passport, handle, errorMsg);
            } else {
                sb.append("Mit Konto '" + konto + "' verbunden.");
            } /* konto given */
        } catch (Exception e) {
            messageHandler.sendMessage("Exception: " + e.toString());
            sb.append(e.toString());
        
            // Sicherstellen, dass sowohl Passport als auch Handle nach Beendigung
            // geschlossen werden.
            close();
        } finally {

        }

        return sb.toString();
    }


    /**
     * close
     * 
     * @param args
     * @throws Exception
     */
    public void close() {

        HBCIUtils.done();
        
        konto = null;

        if (handle != null)
            handle.close();

        if (passport != null)
            passport.close();

        handle = null;
        passport = null;
    }

    /**
     * getHBCIActivity
     * 
     * @param args
     * @throws Exception
     */
    public String getTransactions(Date StartDate, Date EndDate) throws Exception {
        StringBuilder sb = new StringBuilder();

        try {
            if (konto != null) {
System.err.println("Konto: " + konto);
                // 1. Auftrag fuer das Abrufen des Saldos erzeugen
                HBCIJob saldoJob = handle.newJob("SaldoReq");
                saldoJob.setParam("my", konto); // festlegen, welches Konto abgefragt werden soll.
                saldoJob.addToQueue(); // Zur Liste der auszufuehrenden Auftraege hinzufuegen

                // 2. Auftrag fuer das Abrufen der Umsaetze erzeugen
                HBCIJob umsatzJob = handle.newJob("KUmsAll"); /* "KUmsAll", */
                umsatzJob.setParam("my", konto); // festlegen, welches Konto abgefragt werden soll.
                // umsatzJob.setParam(("maxentries",1000);
                umsatzJob.setParam("startdate", StartDate); // festlegen welches Konto
                                                                                    // abgefragt werden
                                                                                    // soll.
                umsatzJob.setParam("enddate", EndDate); // festlegen, welches Konto abgefragt
                                                                                // werden
                                                                                // soll.

                umsatzJob.addToQueue(); // Zur Liste der auszufuehrenden Auftraege hinzufuegen

                // Hier koennen jetzt noch weitere Auftraege fuer diesen Bankzugang hinzugefuegt
                // werden. Z.Bsp. Ueberweisungen.

                // Alle Auftraege aus der Liste ausfuehren.
                HBCIExecStatus status = handle.execute();

                // Pruefen, ob die Kommunikation mit der Bank grundsaetzlich geklappt hat
                if (!status.isOK())
                    return error(passport, handle, status.toString());

                // Auswertung des Saldo-Abrufs.
                GVRSaldoReq saldoResult = (GVRSaldoReq) saldoJob.getJobResult();
                if (!saldoResult.isOK())
                    return error(passport, handle, saldoResult.toString());

                Value saldo = saldoResult.getEntries()[0].ready.value;

                // Das Ergebnis des Jobs koennen wir auf "GVRKUms" casten. Jobs des Typs
                // "KUmsAll"
                // liefern immer diesen Typ.
                GVRKUms result = (GVRKUms) umsatzJob.getJobResult();

                // Pruefen, ob der Abruf der Umsaetze geklappt hat
                if (result.isOK())
                    sb = createUmsatzReport(passport, konto, saldo, result);
                else
                    return error(passport, handle, result.toString());

            } /* konto given */
        } catch (Exception e) {
            //System.err.println("ERROR: Could not receice transaction data for account. " + e.toString());
            sb.append(e.toString());

            // Sicherstellen, dass sowohl Passport als auch Handle nach Beendigung
            // geschlossen werden.
            close();
        } finally {

        }

        return sb.toString();
    }

  private StringBuilder createUmsatzReport (HBCIPassport passport, Konto konto, Value saldo, GVRKUms result) {
    StringBuilder sb = new StringBuilder();

    sb.append("<BankAccount");
    sb.append(" limit=\"" + stringValue(konto.limit) + "\"");
    sb.append(" accountType=\"" + stringValue(konto.acctype) + "\"");
    if (saldo != null)
        sb.append(" saldoAmount=\"" + stringValue(saldo).substring(1,saldo.toString().indexOf(" EUR")) + "\"");
    sb.append(">");
 
    sb.append("<BankAccountDetail>");
    sb.append("<BankName>" + passport.getInstName() + "</BankName>");
    sb.append("<AccountName/>");
    sb.append("<AccountNick/>");
    sb.append("<KTNR>" + stringValue(konto.number) + stringValue(konto.subnumber) + "</KTNR>");
    sb.append("<BLZ>" + stringValue(konto.blz) + "</BLZ>"); 
    sb.append("<IBAN>" + stringValue(konto.iban) + "</IBAN>");
    sb.append("<BIC>" + stringValue(konto.bic) + "</BIC>");
    sb.append("<CustomerID>" + stringValue(konto.customerid) + "</CustomerID>");
    sb.append("<CustomerName>" + stringValue(konto.name) + stringValue(konto.name2) + "</CustomerName>");
    sb.append("<Type>" + stringValue(konto.type) + "</Type>");
    sb.append("<CurrencyUnit>" + stringValue(konto.curr) + "</CurrencyUnit>");
    sb.append("</BankAccountDetail>");

    sb.append("<BusinessUCs>");
    for (String gv : (List<String>) konto.allowedGVs) {
      sb.append("<UC key=\"" + stringValue(gv) + "\"");
      if (bucMap.get(gv) != null)
        sb.append(" desc=\"" + stringValue(bucMap.get(gv)) + "\"");
      sb.append("/>");
    }
    sb.append("</BusinessUCs>");

    //sb.append("<DEBUG>" + result.getDataPerDay() + "</DEBUG>");


    // Alle Umsatzbuchungen ausgeben
    List<UmsLine> buchungen = result.getFlatData();

    sb.append("<Accountings");
    if (buchungen != null && buchungen.get(0) != null && buchungen.get(0).saldo != null && buchungen.get(0).saldo.value != null && buchungen.get(0).value != null) {
        String amount = String.valueOf(buchungen.get(0).saldo.value.getLongValue() - buchungen.get(0).value.getLongValue());
        if (amount != null)
            sb.append(" saldoInitAmount=\"" + amount.substring(0,amount.length()-2) + "." + amount.substring(amount.length()-2) + "\"");
    }
    sb.append(">");

    for (UmsLine buchung : buchungen) {

        /**
         * Liste von Strings mit den Verwendungszweckzeilen. Nur wenn
         * <code>gvcode!=999</code>! (siehe auch <code>additional</code> und
         * <code>gvcode</code>)
         */
        List<String> verwendungszweck = buchung.usage;

        if (false) {
            sb.append("<DEBUG_USAGE>" + buchung.toString());
            List<String> zweck = buchung.usage;
            if (zweck != null && zweck.size() > 0) {
              for (int i = 0; i < zweck.size(); i++) {
                  sb.append(zweck);
              }
            }    
            sb.append("</DEBUG_USAGE>");
        }

        sb.append("<Accounting origin=\"FinTS\"");

        if (buchung.id != null) {
            sb.append(" oid=\"" + stringValue(buchung.id) + "\"");
        }

        if (buchung.saldo != null) {
            sb.append(" saldoTimestamp=\"" + isoDateTimeFormat.format(buchung.saldo.timestamp) + "\"");
            sb.append(" saldoAmount=\"" + monetaryValue(buchung.saldo.value) + "\"");
        }
        sb.append(" isStorno=\"" + buchung.isStorno + "\"");
        sb.append(" isSepa=\"" + buchung.isSepa + "\"");
        sb.append(" isCamt=\"" + buchung.isCamt + "\"");
        sb.append(">");
        
//System.out.println("DEBUG '" + stringValue + "'");

        /* Art der Buchung */ 
        sb.append("<SalesType key=\"" + stringValue(buchung.gvcode) + "\">"+ stringValue(gvcMap.get(buchung.gvcode)) + "</SalesType>");

        /* Purpose-Code der Buchung */ 
        if (buchung.purposecode != null)
            sb.append("<PurposeCode>" + isoDateFormat.format(buchung.purposecode) + "</PurposeCode>");

        /* Buchungsdatum */
        sb.append("<BookingDate>" + isoDateFormat.format(buchung.bdate) + "</BookingDate>");

        /* Datum der Wertstellung */
        sb.append("<ValueDate>" + isoDateFormat.format(buchung.valuta) + "</ValueDate>");


        /*  SEPA Verwendungszweck: 
        IBAN+ SEPA IBAN Auftraggeber
        BIC+ SEPA BIC Auftraggeber
        EREF+ SEPA End to End-Referenz      (für Überweisung)
        KREF+ oder CREF + Kundenreferenz Customer Reference
        MREF+ SEPA Mandatsreferenz          (für Lastschrift)
        CRED+ SEPA Creditor Identifier      (für Lastschrift)
        DEBT+ Originator Identifier
        ABWA+ Abweichender SEPA Auftraggeber
        ABWE+ Abweichender SEPA Empfänger
        BREF+ Bankreferenz, Instruction ID
        RREF+ Retourenreferenz
        SVWZ+ SEPA Verwendungszweck
        COAM+ Zinskompensationsbetrag
        OAMT+ Ursprünglicher Umsatzbetrag
        */

        /* EndToEndReferenz (EREF+) - nur bei SEPA Überweisung */
        if (buchung.endToEndId != null && buchung.endToEndId != "") 
            sb.append("<EndToEndRef>" + stringValue(buchung.endToEndId) + "</EndToEndRef>");
        else  {
            String valueEREF = sepaUsageValue(verwendungszweck, "EREF");
            if (valueEREF != null)
                sb.append("<EndToEndRef type=\"EREF\">" + valueEREF + "</EndToEndRef>");
        }

        /* Kundenreferenz (CREF+) */
        if (buchung.customerref != null && buchung.customerref != "") {
            sb.append("<CustomerRef>" + stringValue(buchung.customerref) + "</CustomerRef>");
            // workaround for hbci4java bug: Remove "KREF+NOTPROVIDED"
            if (verwendungszweck != null && verwendungszweck.size() > 0) {
                for (int i = 0; i < verwendungszweck.size(); i++) {
                    if ("KREF+NOTPROVIDED".equals(stringValue(verwendungszweck.get(i)))) {
                        verwendungszweck.remove(i);
                    }
                }
            }
        }
        else  {
            String valueKREF = sepaUsageValue(verwendungszweck, "KREF");
            if (valueKREF != null) {
                sb.append("<CustomerRef type=\"KREF\">" + valueKREF + "</CustomerRef>");
            }
            else {
                String valueCREF = sepaUsageValue(verwendungszweck, "CREF");
                if (valueCREF != null)
                    sb.append("<CustomerRef type=\"CREF\">" + valueCREF + "</CustomerRef>");
            }
        }
        
        /* Mandatsreferenznummer (MREF+) - nur bei Bankeinzug */
        if (buchung.instref != null && buchung.instref != "") 
            sb.append("<MandantRef>" + stringValue(buchung.instref) + "</MandantRef>");
        else {
            String valueMREF = sepaUsageValue(verwendungszweck, "MREF");
            if (valueMREF != null)
                sb.append("<MandantRef type=\"MREF\">" + valueMREF + "</MandantRef>");
        }

        /* CreditorID (CRED+) - nur bei Bankeinzug */
        String valueCRED = sepaUsageValue(verwendungszweck, "CRED");
        if (valueCRED != null)
            sb.append("<CreditorID type=\"CRED\">" + valueCRED + "</CreditorID>");      

        /** Ursprünglicher Betrag (bei ausländischen Buchungen; optional) (OAMT) */
        if (buchung.orig_value != null)
            sb.append("<OriginalAmount>" + stringValue(buchung.orig_value) + "</OriginalAmount>");
        else {
            String valueOAMT = sepaUsageValue(verwendungszweck, "OAMT");
            if (valueOAMT != null)
                sb.append("<OriginalAmount type=\"OAMT\">" + valueOAMT + "</OriginalAmount>");
        }

        /** Betrag für Gebühren des Geldverkehrs (optional) */
        if (buchung.charge_value != null)
            sb.append("<ExternalCosts>" + stringValue(buchung.charge_value) + "</ExternalCosts>");


        /* Gebuchter Betrag */
        if (buchung.value != null) {
            if ((buchung.value.getLongValue() <= 0) || (buchung.value.getLongValue() > 0 && buchung.isStorno)) {
                sb.append("<Amount type=\"DEBIT\">" + monetaryValue(buchung.value) + "</Amount>");
                sb.append("<CurrencyUnit>" + stringValue(buchung.value.getCurr()) + "</CurrencyUnit>");
            }
            else {
                sb.append("<Amount type=\"CREDIT\">" + monetaryValue(buchung.value) + "</Amount>");
                sb.append("<CurrencyUnit>" + stringValue(buchung.value.getCurr()) + "</CurrencyUnit>");
            }
        }


        /* DivergentRecepient / Abweichender SEPA Auftraggeber*/
        String valueABWA = sepaUsageValue(verwendungszweck, "ABWA");
        if (valueABWA != null)
            sb.append("<DivergentRecepient type=\"ABWA\">" + valueABWA + "</DivergentRecepient>"); 

        /*
        * Art der Buchung (bankinterner Code). Nur wenn hier ein Wert ungleich
        * <code>999</code> drinsteht, enthalten die Attribute <code>text</code>,
        * <code>primanota</code>, <code>usage</code>, <code>other</code> und
        * <code>addkey</code> sinnvolle Werte. Andernfalls sind all diese Informationen
        * möglicherweise im Feld <code>additional</code> enthalten, allerdings in einem
        * nicht definierten Format (siehe auch <code>additional</code>).
        */      
        if (buchung.gvcode != "999") {
                 /**
             * <p>
             * Wenn die Zusatzinformationen aber ausgewertet werden können (und
             * <code>gvcode!=999</code>), sind die Felder <code>text</code>, 
             * <code>primanota</code>, <code>usage</code>, <code>other</code> 
             * und <code>addkey</code> gefüllt und enthalten die entsprechenden Werte
             * </p>
             */

        
            /**
             * Gegenkonto der Buchung (optional). Nur wenn <code>gvcode!=999</code>! (siehe
             * auch <code>additional</code> und <code>gvcode</code>)
             */
            if (buchung.other != null) {
                sb.append("<Recipient>" + stringValue(buchung.other.name) + stringValue(buchung.other.name2) + "</Recipient>");
                sb.append("<IBAN>");
                    if (buchung.other.iban != null)
                        sb.append(stringValue(buchung.other.iban));
                    else if (buchung.other.number != null) {
                        sb.append(stringValue(buchung.other.number));
                        if (buchung.other.subnumber != null)
                            sb.append(stringValue(buchung.other.subnumber));
                    }
                sb.append("</IBAN>");
                sb.append("<BIC>");
                    if (buchung.other.bic != null)
                        sb.append(stringValue(buchung.other.bic));
                    else if (buchung.other.blz != null)
                        sb.append(stringValue(buchung.other.blz));
                sb.append("</BIC>");
            } 
           

            /* Verwendungszweck */
            String valueSVWZ = sepaUsageValue(verwendungszweck, "SVWZ");
            if (valueSVWZ != null) {
                sb.append("<PaymentDetail type=\"SVWZ\">" + valueSVWZ + "</PaymentDetail>");
            } else {
                sb.append("<PaymentDetail/>");
            }

            /**
             * Liste von Strings mit den verbliebenen Verwendungszweckzeilen. Nur wenn
             * <code>gvcode!=999</code>! (siehe auch <code>additional</code> und
             * <code>gvcode</code>)
             */

            if (verwendungszweck != null && verwendungszweck.size() > 0) {
                sb.append("<Usage>");
                for (int i = 0; i < verwendungszweck.size(); i++) {
                    // alle verbliebenen Zeilen mit Verwendungszweck ausgeben (siehe u. a. CreditorID)) 
                    sb.append(stringValue(verwendungszweck.get(i)));
                    // Leerzeichen einfügen??
                }
                sb.append("</Usage>");
            }
            else {
                sb.append("<Usage/>");
            }

            /**
             * Beschreibung der Art der Buchung (optional). Nur wenn
             * <code>gvcode!=999</code>! (siehe auch <code>additional</code> und
             * <code>gvcode</code>)
             */
            if (buchung.text != null)
                sb.append("<Text>" + stringValue(buchung.text) + "</Text>");

            /**
             * Primanotakennzeichen (optional). Nur wenn <code>gvcode!=999</code>! (siehe
             * auch <code>additional</code> und <code>gvcode</code>)
             */
            if (buchung.primanota != null)
                sb.append("<Primanota>" + stringValue(buchung.primanota) + "</Primanota>");

            /**
             * Erweiterte Informationen zur Art der Buchung (bankintern, optional). Nur wenn
             * <code>gvcode!=999</code>! (siehe auch <code>additional</code> und
             * <code>gvcode</code>)
             */
            if (buchung.addkey != null)
                sb.append("<AddKey>" + stringValue(buchung.addkey) + "</AddKey>");
   
        } /* BVC != 999 */

        
        /**
         * <p>
         * Zusatzinformationen im Rohformat. Wenn Zusatzinformationen zu dieser
         * Transaktion in einem unbekannten Format vorliegen, dann enthält dieser String
         * diese Daten (u.U. ist dieser String leer, aber nicht <code>null</code>). Das
         * ist genau dann der Fall, wenn der Wert von <code>gvcode</code> gleich
         * <code>999</code> ist.
         *
         * Wenn die Zusatzinformationen aber ausgewertet werden können (und
         * <code>gvcode!=999</code>), so ist dieser String <code>null</code>.
         * </p>
         */ 
        if (buchung.additional != null && buchung.additional != "") {
            sb.append("<Additional> " + stringValue(buchung.additional) + "</Additional>");
        }

        /* cleanup to free memory */
        //buchungen.remove(buchung);
        //buchung = null;

        sb.append("</Accounting>");
    }

    // Ausgeben der Umsatz-Zeile
    sb.append("</Accountings>");

    // Alle vorgemerkten Buchungen
    // List<UmsLine> buchungen = result.getFlatDataUnbooked();
    // for (UmsLine buchung : buchungen) {
    // buchung.valuta
    // }

    sb.append("</BankAccount>");

    return sb;
  }


    /*
    *
    *  SEPA Banküberweisung
    *  RecipientBlz, RecipientKtnr, RecipientName, Date, CurrencyAmount, CurrencyUnit, VerwendungszweckZeile1, VerwendungszweckZeile2
    *  https://github.com/willuhn/hbci4java/blob/master/src/org/kapott/hbci/GV/GVUebSEPA.java
    *  https://github.com/willuhn/hbci4java/blob/master/test/hbci4java/sepa/TestPainGen.java
    *
    */
    public String arangeBankTransfer(String RecipientCountry, String RecipientBic, String RecipientIban, String RecipientName, Date Date, double CurrencyAmount, String CurrencyUnit, String UsageText) {

        HBCIJob job = handle.newJob("UebSEPA"); //Ueb

        Konto kontoSrc = new Konto();

        kontoSrc.name = this.CustomerID;
        kontoSrc.iban = this.Iban;
        kontoSrc.bic = this.Bic;
        kontoSrc.country = this.Country;

        job.setParam("src", kontoSrc);
        //job.setParam("src.bic", stringValue(kontoSrc.bic));
        //job.setParam("src.iban", stringValue(kontoSrc.iban));
        //job.setParam("src.name", stringValue(kontoSrc.name));

        Konto kontoDst = new Konto();
        kontoDst.name = RecipientName;
        kontoDst.iban = RecipientIban;
        kontoDst.blz = RecipientBic;
        kontoDst.country = RecipientCountry;
        //kontoDst.bic = RecipientBic;
        //kontoDst.bic = payment.getReceiverBic();
        //job.setParam("dst", i, dst);

        job.setParam("dst", kontoDst);
        //job.setParam("dst.bic", "");
        //job.setParam("dst.iban", RecipientIban);
        //job.setParam("dst.name", RecipientName);

        //job.setParam("btg", new Value(100, "EUR"));
        job.setParam("btg", new Value(CurrencyAmount, CurrencyUnit));
        //job.setParam("btg.value", ""+CurrencyAmount);
        //job.setParam("btg.curr", CurrencyUnit);


        job.setParam("usage", UsageText);
        //job.setParam("usage.usage", Verwendungszweck);
        //job.setParam("usage.usage_2", "Verwendungszweck2");
        //job.setParam("key", job.getJobRestrictions().getProperty("key"));

        //Date
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
		//dtf.format(Date)

        //job.setParam("sepaid", "abcde");  //getPainMessageId()
        //job.setParam("pmtinfid", "fghij");  //getPainMessageId()
        //job.setParam("endtoendid", "fghij");  //ENDTOEND_ID_NOTPROVIDED
        
        //System.out.println("DEBUG: btg.value: " + job.getBigDecimalValue("btg.value"));

        System.out.println("DEBUG: getJobParameterNames: " + job.getJobParameterNames());
        System.out.println("DEBUG: getLowlevelParams: " + job.getLowlevelParams());
        System.out.println("DEBUG: getJobResultNames: " + job.getJobResultNames() );
        System.out.println("DEBUG: getJobRestrictions: " + job. getJobRestrictions());

        job.addToQueue();

System.out.println("DEBUG Job:" + job);

        // Alle Auftraege aus der Liste ausfuehren.
        HBCIExecStatus status = handle.execute();

        // Pruefen, ob die Kommunikation mit der Bank grundsaetzlich geklappt hat
        if (!status.isOK())
            return error(passport, handle, status.toString());

        // Status aller mit execute() ausgeführten Dialoge (können mehrere
        // sein, wenn z.B. verschiedene Kunden-IDs für einzelne Jobs verwendet
        // werden sollen)
System.out.println("DEBUG Status:" + status);

        // Status eines konkreten Jobs
System.out.println("DEBUG: Jobstatus:" + job.getJobResult().getJobStatus());

        return "" + job.getJobResult().getJobStatus();
    }


    /*
    *
    *  SEPA Bank Multi-Lastschrift
    */
    public void sepaBankMultiLast() {
        /*
        HBCIJob job = handle.newJob("MultiLastSEPA");

        job.setParam("src", konto);
        job.setParam("sepaid", sepaid);

        for (int ix = 0; ix < transactions.size(); ix++) {
            Transaction tr = transactions.get(ix);

            job.setParam("dst.bic"       , ix, tr.getBankCode());
            job.setParam("dst.iban"      , ix, tr.getAccountNumber());
            job.setParam("dst.name"      , ix, tr.getAccountOwner());
            job.setParam("btg.value"     , ix, tr.getValue());
            job.setParam("btg.curr"      , ix, "EUR");
            job.setParam("usage"         , ix, tr.getUsage());
            job.setParam("mandateid"     , ix, tr.getMandateId());
            job.setParam("manddateofsig" , ix, tr.getMandateDate());
            job.setParam("amendmandindic", ix, tr.getMandateAmend());
            job.setParam("endtoendid"    , ix, tr.getEndToEndId());
            job.setParam("creditorid"    , ix, tr.getCreditorId());
        }

        HBCIExecStatus status = handle.execute();
        */
    }

    /*
    *
    *   Ermittelt den Text zu einem SEPA Code
    */
    private String sepaUsageValue(List<String> verwendungszweck, String usageCode) {
        String value = null;

        int zeile = 0;
        if (verwendungszweck != null && verwendungszweck.size() > 0) {
            for (int i = 0; i < verwendungszweck.size(); i++) {
                String VzZeile = stringValue(verwendungszweck.get(i));
                if (VzZeile != null) {
                    // ERSTE Zeile des Verwenugszweck
                    if (VzZeile.matches("^"+usageCode+"\\+.*")) {
                        zeile = 1;
                        value = stringValue(VzZeile.substring(5));
                        // diese Zeile aus der Liste Verwendungszweck löschen
                        verwendungszweck.remove(i);
                        i--; // da die aktuelle Zeile gelöscht wurde...
                    }
                    // ALLE Zeilen NACH ^SVWZ+ gehören auch zum Verwendungszweck
                    else if (zeile != 0) {
                        if (VzZeile.matches("^[A-Z]{4}\\+[^\\u0020].*")) { // ... außer es handelt sich um einen neuen Steuercode
                            zeile = 0;
                            return value;
                        }
                        else {
                            zeile += 1;
                            value += stringValue(VzZeile);
                            // diese Zeile aus der Liste Verwendungszweck löschen
                            verwendungszweck.remove(i);
                            i--; // da die aktuelle Zeile gelöscht wurde...
                        }
                    }
                }
            }
        }
        return value;
    }


    /**
     * Erstellen eines eindeutigen Schlüssel für jeden Buchungssatz (NOT USED)
     * @param accounting
     * @return md5 hash für den Buchungssatz (Kalkulation kann erst auf dem XML erfolgen)
     */
    private static String createAccountingID (String accounting) {

        String stringHashbase = "";
        // BankAccount.IBAN 
        // Geschaeftsvorgang Code SalesType/@key
        // ValueDate
        // Amount
        // @saldoAmount
        // CurrencyUnit
        // CustomerRef[text()!='NONREF']
        // EndToEndID[text()!='NOTPROVIDED']
        // MandantRef
        // CreditorI
        // Recepient
        // IBAN
        // BIC
        // normalize-space(PaymentDetails/PaymentDetail)
        // dayCounter (wie viele Buchungen gab es bereits an diesem Tag)
        
        MessageDigest digest = null;
		try {
                digest = MessageDigest.getInstance("MD5");
                digest.update(stringHashbase.getBytes());	
            } 
            catch (NoSuchAlgorithmException e1) 
            {}

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
			
        return "PAY" + bigInt.toString(16);
    } 


    /** 
     * Gibt den reinen Wert eines Value Feldes aua
     */
    private static String monetaryValue (Value value) {
        if (value != null) {
            String amount = String.valueOf(value.getLongValue());

            if (amount == null)
                return "";
            else if (amount.length() == 0)
                return "";
            else if (amount.length() == 1)
               return "0.0" + amount;
            else if (amount.length() == 2 && value.getLongValue() >= 0)
               return "0." + amount;
            else if (amount.length() == 2 && value.getLongValue() < 0)
                return "-0.0" + amount.substring(1);
                else if (amount.length() == 3 && value.getLongValue() < 0)
                return "-0." + amount.substring(1);
            else 
                return amount.substring(0,amount.length()-2) + "." + amount.substring(amount.length()-2);
        }

        return "";
    }


    /** 
     * Gibt den String eines Wertes aus (ohne NULL)
     * alle XML Zeichnen werden escaped
     */
    private static String stringValue(Object value) {
        if (value == null)
            return "";

        String stringValue = value.toString();
        if (stringValue == null)
            return "";

        if (stringValue.equals("null"))
            return "";

        return stringValue.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;")
                .replace("\'", "&apos;");
    }


    /**
     * Beendet das Programm mit der angegebenen Fehler-Meldung.
     * 
     * @param msg die Meldung.
     */
    private static String error(HBCIPassport passport, HBCIHandler handle, String msg) {
        // Sicherstellen, dass sowohl Passport als auch Handle auch bei einem Fehler
        // immer geschlossen werden
        if (handle != null)
            handle.close();

        if (passport != null)
            passport.close();

        System.err.println(msg);
        return "<Error>" + msg + "</Error>";
    }

    /**
     * Ueber diesen Callback kommuniziert HBCI4Java mit dem Benutzer und fragt die
     * benoetigten Informationen wie Benutzerkennung, PIN usw. ab.
     */
    private class MyHBCICallback extends AbstractHBCICallback {
        /**
         * @see org.kapott.hbci.callback.HBCICallback#log(java.lang.String, int,
         *      java.util.Date, java.lang.StackTraceElement)
         */
        @Override
        public void log(String msg, int level, Date date, StackTraceElement trace) {
            // Ausgabe von Log-Meldungen bei Bedarf
            // System.out.println(msg);
        }

        /**
         * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport,
         *      int, java.lang.String, int, java.lang.StringBuffer)
         */
        @Override
        public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {
            java.io.Console console = System.console();

            // Diese Funktion ist wichtig. Ueber die fragt HBCI4Java die benoetigten Daten
            // von uns ab.
            switch (reason) {
                // Mit dem Passwort verschluesselt HBCI4Java die Passport-Datei.
                // Wir nehmen hier der Einfachheit halber direkt die PIN. In der Praxis
                // sollte hier aber ein staerkeres Passwort genutzt werden.
                // Die Ergebnis-Daten muessen in dem StringBuffer "retData" platziert werden.
                case NEED_PASSPHRASE_LOAD:
                    // when keyfile is been loaded
                    messageHandler.sendMessage("NEED_PASSPHRASE_LOAD");
                    if (Secret == null)
                        Secret = messageHandler.readMessage();
                    messageHandler.sendMessage("PASSPHRASE_LOAD = " + "****"); // + Secret
                    retData.replace(0, retData.length(), Secret);
                    break;

                case NEED_PASSPHRASE_SAVE:
                    // when keyfile is been created
                    messageHandler.sendMessage("NEED_PASSPHRASE_SAVE");
                    if (Secret == null)
                        Secret = messageHandler.readMessage(); 
                    messageHandler.sendMessage("PASSPHRASE_SAVE = " + "****"); // + Secret
                    retData.replace(0, retData.length(), Secret);
                    break;

                // PIN wird benoetigt
                case NEED_PT_PIN:
                    messageHandler.sendMessage("NEED_PT_PIN");
                    // retData.replace(0,retData.length(),PIN);
                    if (Pin == null)
                        Pin = messageHandler.readMessage();
                    messageHandler.sendMessage("PT_PIN = " + "****"); // + Pin
                    retData.replace(0, retData.length(), Pin);
                    break;

                // BLZ wird benoetigt
                case NEED_BLZ:
                    messageHandler.sendMessage("NEED_BLZ");
                    if (Blz == null)
                        Blz = messageHandler.readMessage(); 
                    messageHandler.sendMessage("BLZ = " + Blz);
                    retData.replace(0, retData.length(), Blz);
                    break;

                // Die Benutzerkennung
                case NEED_USERID:
                    messageHandler.sendMessage("NEED_USERID");
                    System.out.println("###USERID = " + BankUserID);
                    if (BankUserID == null)
                        BankUserID = messageHandler.readMessage();
                    messageHandler.sendMessage("USERID = " + BankUserID);
                    retData.replace(0, retData.length(), BankUserID);
                    break;

                // Die Kundenkennung. Meist identisch mit der Benutzerkennung.
                // Bei manchen Banken kann man die auch leer lassen
                case NEED_CUSTOMERID:
                    messageHandler.sendMessage("NEED_CUSTOMER_ID");
                    if (CustomerID == null)
                        CustomerID = messageHandler.readMessage();
                    messageHandler.sendMessage("CUSTOMER_ID = " + CustomerID);
                    retData.replace(0, retData.length(), CustomerID);
                    break;

                ////////////////////////////////////////////////////////////////////////
                // Die folgenden Callbacks sind nur fuer die Ausfuehrung TAN-pflichtiger
                // Geschaeftsvorfaelle bei der Verwendung des PIN/TAN-Verfahrens noetig.
                // Z.Bsp. beim Versand einer Ueberweisung
                // "NEED_PT_SECMECH" kann jedoch auch bereits vorher auftreten.

                // HBCI4Java benoetigt die TAN per PhotoTAN-Verfahren
                // Liefert die anzuzeigende PhotoTAN-Grafik, die mit der entsprechenden
                // Smartphone-App der Bank fotografiert werden muss, um die TAN
                // zu generieren. Eine Implementierung muss diese Grafik anzeigen
                // sowie ein Eingabefeld fuer die TAN. Der Callback muss dann die vom
                // User eingegebene TAN zurueckliefern (nachdem dieser die Grafik
                // fotografiert und die App ihm die TAN angezeigt hat)
                case NEED_PT_PHOTOTAN:
                    messageHandler.sendMessage("NEED_PT_PHOTOTAN - Phototan wird benoetigt: " + reason);

                    // Die Klasse "MatrixCode" kann zum Parsen der Daten verwendet werden
                    try {
                        // MatrixCode code = new MatrixCode(retData.toString());

                        // Liefert den Mime-Type der grafik (i.d.R. "image/png").
                        // String type = code.getMimetype();

                        // Der Stream enthaelt jetzt die Binaer-Daten des Bildes
                        // InputStream stream = new ByteArrayInputStream(code.getImage());

                        // .... Hier Dialog mit der Grafik anzeigen und User-Eingabe der TAN
                        // Die Variable "msg" aus der Methoden-Signatur enthaelt uebrigens
                        // den bankspezifischen Text mit den Instruktionen fuer den User.
                        // Der Text aus "msg" sollte daher im Dialog dem User angezeigt
                        // werden.
                        String tan = null;
                        if (tan == null)
                            tan = messageHandler.readMessage();
                        messageHandler.sendMessage("PT_PHOTOTAN = " + tan);
                        retData.replace(0, retData.length(), tan);
                    } catch (Exception e) {
                        messageHandler.sendMessage("HBCI-Exception: " + e.toString());
                        throw new HBCI_Exception(e);
                    }
                    break;

                case NEED_PT_QRTAN:
                    messageHandler.sendMessage("NEED_PT_QRTAN - QR-Tan wird benoetigt: " + msg + " " + retData.toString());

                    // Die Klasse "QRCode" kann zum Parsen der Daten verwendet werden
                    try {
                        // QRCode code = new QRCode(retData.toString(),msg);

                        // Der Stream enthaelt jetzt die Binaer-Daten des Bildes
                        // InputStream stream = new ByteArrayInputStream(code.getImage());

                        // .... Hier Dialog mit der Grafik anzeigen und User-Eingabe der TAN
                        // Die Variable "msg" aus der Methoden-Signatur enthaelt uebrigens
                        // den bankspezifischen Text mit den Instruktionen fuer den User.
                        // Der Text aus "msg" sollte daher im Dialog dem User angezeigt
                        // werden. Da Sparkassen den eigentlichen Bild u.U. auch in msg verpacken,
                        // sollte zur Anzeige nicht der originale Text verwendet werden sondern
                        // der von QRCode - dort ist dann die ggf. enthaltene Base64-codierte QR-Grafik
                        // entfernt
                        // msg = code.getMessage();
                        String tan = null;
                        if (tan == null)
                            tan = messageHandler.readMessage();
                        messageHandler.sendMessage("PT_QRTAN = " + tan);
                        retData.replace(0, retData.length(), tan);
                    } catch (Exception e) {
                        messageHandler.sendMessage("HBCI-Exception: " + e.toString());
                        throw new HBCI_Exception(e);
                    }
                    break;

                // HBCI4Java benoetigt den Code des verwendenden TAN-Verfahren (smsTAN,
                // chipTAN optisch, photoTAN,...)
                // I.d.R. ist das eine dreistellige mit "9" beginnende Ziffer
                case NEED_PT_SECMECH:
                    messageHandler.sendMessage("NEED_PT_SECMECH - Code für TAN-Verfahren wird benoetigt: " + msg + " "
                            + retData.toString());

                    // Als Parameter werden die verfuegbaren TAN-Verfahren uebergeben.
                    // Der Aufbau des String ist wie folgt:
                    // <code1>:<name1>|<code2>:<name2>|...
                    // Bsp:
                    // 901:smsTAN|920:chipTAN optisch|955:photoTAN
                    // String options = retData.toString();

                    // Der Callback muss den Code des zu verwendenden TAN-Verfahrens
                    // zurueckliefern
                    // In "code" muss der 3-stellige Code des vom User gemaess obigen
                    // Optionen ausgewaehlte Verfahren eingetragen werden
                    //String code = "901";
                    //String code = "910";
                    String code = AuthType.toString();
                    if (code == null)
                        code = messageHandler.readMessage(); 
                    // String code = console.readLine("Bitte 9xx Code eingeben:");
                    messageHandler.sendMessage("PT_SECMECH = " + code);
                    retData.replace(0, retData.length(), code);
                    break;

                // HBCI4Java benoetigt die TAN per smsTAN/chipTAN/weiteren TAN-Verfahren
                case NEED_PT_TAN:
                    messageHandler.sendMessage(
                            "NEED_PT_TAN - TAN für smsTAN/chipTAN wird benoetigt: " + msg + " " + retData.toString());

                    // Wenn per "retData" Daten uebergeben wurden, dann enthalten diese
                    // den fuer chipTAN optisch zu verwendenden Flickercode.
                    // Falls nicht, ist es eine TAN-Abfrage, fuer die keine weiteren
                    // Parameter benoetigt werden (z.Bsp. beim smsTAN-Verfahren)

                    // Die Variable "msg" aus der Methoden-Signatur enthaelt uebrigens
                    // den bankspezifischen Text mit den Instruktionen fuer den User.
                    // Der Text aus "msg" sollte daher im Dialog dem User angezeigt
                    // werden.

                    String flicker = retData.toString();
                    if (flicker != null && flicker.length() > 0) {
                        // Ist chipTAN optisch. Es muss ein animierter Barcode angezeigt
                        // werden. Hierfuer kann die Hilfsklasse "FlickerRenderer" verwendet
                        // werden. Diese enthalt bereits das Parsen. Es muss lediglich die
                        // Methode "paint" ueberschrieben werden.
                        // FlickerRenderer renderer = new FlickerRenderer(flicker);

                        // Hier TAN-Abfrage mit dem animierten Barcode anzeigen sowie
                        // Eingabefeld fuer die TAN
                        // String tan = "";
                        messageHandler.sendMessage("Bitte chipTAN eingeben:");
                        String tan = messageHandler.readMessage(); 
                        messageHandler.sendMessage("PT_TAN = " + tan);
                        retData.replace(0, retData.length(), tan);
                    } else {
                        // Ist smsTAN, iTAN, o.ae.
                        // Dialog zur TAN-Eingabe anzeigen mit dem Text aus "msg".
                        // String tan = null;
                        messageHandler.sendMessage("Bitte mTAN / iTAN eingeben:");
                        String tan = messageHandler.readMessage();
                        messageHandler.sendMessage("PT_TAN = " + tan);
                        retData.replace(0, retData.length(), tan);
                    }

                    break;

                // Beim Verfahren smsTAN ist es moeglich, mehrere Handynummern mit
                // Aliasnamen bei der Bank zu hinterlegen. Auch wenn nur eine Handy-
                // Nummer bei der Bank hinterlegt ist, kann es durchaus passieren,
                // dass die Bank dennoch die Aufforderung zur Auswahl des TAN-Mediums
                // sendet.
                case NEED_PT_TANMEDIA:
                    messageHandler.sendMessage(
                            "NEED_PT_TANMEDIA- Fuer smsTAN wird Alias wird benoetigt: " + msg + " id=" + reason);

                    // Als Parameter werden die verfuegbaren TAN-Medien uebergeben.
                    // Der Aufbau des String ist wie folgt:
                    // <name1>|<name2>|...
                    // Bsp:
                    // Privathandy|Firmenhandy
                    // String options = retData.toString();

                    // Der Callback muss den vom User ausgewaehlten Aliasnamen
                    // zurueckliefern. Falls "options" kein "|" enthaelt, ist davon
                    // auszugehen, dass nur eine moegliche Option existiert. In dem
                    // Fall ist keine Auswahl noetig und "retData" kann unveraendert
                    // bleiben
                    // String alias = null;
                    messageHandler.sendMessage("Bitte Alias eingeben:");
                    String alias = messageHandler.readMessage();
                    messageHandler.sendMessage("PT_TANMEDIA = " + alias);
                    retData.replace(0, retData.length(), alias);

                    break;
                //
                ////////////////////////////////////////////////////////////////////////

                // Manche Fehlermeldungen werden hier ausgegeben
                case HAVE_ERROR:
                    messageHandler.sendMessage("Es ist ein Fehler aufgetreten: reason=" + reason + " msg=" + msg + " datatype="
                            + datatype + " ret=" + retData);

                    messageHandler.sendMessage(msg);
                    break;

                default:
                    messageHandler.sendMessage("Default Callback: reason=" + reason + " msg=" + msg + " datatype="
                            + datatype + " ret=" + retData);
                    // Wir brauchen nicht alle der Callbacks
                    break;

            }
        }

        /**
         * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport,
         *      int, java.lang.Object[])
         */
        @Override
        public void status(HBCIPassport passport, int statusTag, Object[] o) {
            // So aehnlich wie log(String,int,Date,StackTraceElement) jedoch fuer
            // Status-Meldungen.
        }

    }

}
