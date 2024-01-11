
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Sikkerhetsbegrensning }
     * 
     */
    public Sikkerhetsbegrensning createSikkerhetsbegrensning() {
        return new Sikkerhetsbegrensning();
    }

    /**
     * Create an instance of {@link AktoerIkkeFunnet }
     * 
     */
    public AktoerIkkeFunnet createAktoerIkkeFunnet() {
        return new AktoerIkkeFunnet();
    }

    /**
     * Create an instance of {@link UgyldigInput }
     * 
     */
    public UgyldigInput createUgyldigInput() {
        return new UgyldigInput();
    }

}