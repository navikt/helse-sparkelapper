
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link WSVedtak }
     * 
     */
    public WSVedtak createWSVedtak() {
        return new WSVedtak();
    }

    /**
     * Create an instance of {@link WSRettighetsgruppe }
     * 
     */
    public WSRettighetsgruppe createWSRettighetsgruppe() {
        return new WSRettighetsgruppe();
    }

    /**
     * Create an instance of {@link WSPeriode }
     * 
     */
    public WSPeriode createWSPeriode() {
        return new WSPeriode();
    }

    /**
     * Create an instance of {@link WSDagpengekontrakt }
     * 
     */
    public WSDagpengekontrakt createWSDagpengekontrakt() {
        return new WSDagpengekontrakt();
    }

    /**
     * Create an instance of {@link WSBruker }
     * 
     */
    public WSBruker createWSBruker() {
        return new WSBruker();
    }

    /**
     * Create an instance of {@link WSYtelseskontrakt }
     * 
     */
    public WSYtelseskontrakt createWSYtelseskontrakt() {
        return new WSYtelseskontrakt();
    }

}
