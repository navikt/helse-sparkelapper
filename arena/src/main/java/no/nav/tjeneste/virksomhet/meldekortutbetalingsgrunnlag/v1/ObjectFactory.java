
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil.AktoerIkkeFunnet;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil.UgyldigInput;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1 package. 
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

    private final static QName _FinnMeldekortUtbetalingsgrunnlagListeugyldigInput_QNAME = new QName("http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", "finnMeldekortUtbetalingsgrunnlagListeugyldigInput");
    private final static QName _FinnMeldekortUtbetalingsgrunnlagListesikkerhetsbegrensning_QNAME = new QName("http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", "finnMeldekortUtbetalingsgrunnlagListesikkerhetsbegrensning");
    private final static QName _FinnMeldekortUtbetalingsgrunnlagListeaktoerIkkeFunnet_QNAME = new QName("http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", "finnMeldekortUtbetalingsgrunnlagListeaktoerIkkeFunnet");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link FinnMeldekortUtbetalingsgrunnlagListe }
     * 
     */
    public FinnMeldekortUtbetalingsgrunnlagListe createFinnMeldekortUtbetalingsgrunnlagListe() {
        return new FinnMeldekortUtbetalingsgrunnlagListe();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link FinnMeldekortUtbetalingsgrunnlagListeResponse }
     * 
     */
    public FinnMeldekortUtbetalingsgrunnlagListeResponse createFinnMeldekortUtbetalingsgrunnlagListeResponse() {
        return new FinnMeldekortUtbetalingsgrunnlagListeResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UgyldigInput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", name = "finnMeldekortUtbetalingsgrunnlagListeugyldigInput")
    public JAXBElement<UgyldigInput> createFinnMeldekortUtbetalingsgrunnlagListeugyldigInput(UgyldigInput value) {
        return new JAXBElement<UgyldigInput>(_FinnMeldekortUtbetalingsgrunnlagListeugyldigInput_QNAME, UgyldigInput.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Sikkerhetsbegrensning }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", name = "finnMeldekortUtbetalingsgrunnlagListesikkerhetsbegrensning")
    public JAXBElement<Sikkerhetsbegrensning> createFinnMeldekortUtbetalingsgrunnlagListesikkerhetsbegrensning(Sikkerhetsbegrensning value) {
        return new JAXBElement<Sikkerhetsbegrensning>(_FinnMeldekortUtbetalingsgrunnlagListesikkerhetsbegrensning_QNAME, Sikkerhetsbegrensning.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AktoerIkkeFunnet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1", name = "finnMeldekortUtbetalingsgrunnlagListeaktoerIkkeFunnet")
    public JAXBElement<AktoerIkkeFunnet> createFinnMeldekortUtbetalingsgrunnlagListeaktoerIkkeFunnet(AktoerIkkeFunnet value) {
        return new JAXBElement<AktoerIkkeFunnet>(_FinnMeldekortUtbetalingsgrunnlagListeaktoerIkkeFunnet_QNAME, AktoerIkkeFunnet.class, null, value);
    }

}
