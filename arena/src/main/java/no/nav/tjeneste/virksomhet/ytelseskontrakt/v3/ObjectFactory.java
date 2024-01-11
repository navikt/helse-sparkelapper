
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.feil.WSSikkerhetsbegrensning;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.tjeneste.virksomhet.ytelseskontrakt.v3 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _HentYtelseskontraktListesikkerhetsbegrensning_QNAME = new QName("http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3", "hentYtelseskontraktListesikkerhetsbegrensning");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.tjeneste.virksomhet.ytelseskontrakt.v3
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HentYtelseskontraktListe }
     * 
     */
    public HentYtelseskontraktListe createHentYtelseskontraktListe() {
        return new HentYtelseskontraktListe();
    }

    /**
     * Create an instance of {@link HentYtelseskontraktListeResponse }
     * 
     */
    public HentYtelseskontraktListeResponse createHentYtelseskontraktListeResponse() {
        return new HentYtelseskontraktListeResponse();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WSSikkerhetsbegrensning }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3", name = "hentYtelseskontraktListesikkerhetsbegrensning")
    public JAXBElement<WSSikkerhetsbegrensning> createHentYtelseskontraktListesikkerhetsbegrensning(WSSikkerhetsbegrensning value) {
        return new JAXBElement<WSSikkerhetsbegrensning>(_HentYtelseskontraktListesikkerhetsbegrensning_QNAME, WSSikkerhetsbegrensning.class, null, value);
    }

}
