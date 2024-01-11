
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Tema }
     * 
     */
    public Tema createTema() {
        return new Tema();
    }

    /**
     * Create an instance of {@link Kodeverdi }
     * 
     */
    public Kodeverdi createKodeverdi() {
        return new Kodeverdi();
    }

    /**
     * Create an instance of {@link Vedtak }
     * 
     */
    public Vedtak createVedtak() {
        return new Vedtak();
    }

    /**
     * Create an instance of {@link Periode }
     * 
     */
    public Periode createPeriode() {
        return new Periode();
    }

    /**
     * Create an instance of {@link Meldekort }
     * 
     */
    public Meldekort createMeldekort() {
        return new Meldekort();
    }

    /**
     * Create an instance of {@link AktoerId }
     * 
     */
    public AktoerId createAktoerId() {
        return new AktoerId();
    }

    /**
     * Create an instance of {@link Vedtaksstatuser }
     * 
     */
    public Vedtaksstatuser createVedtaksstatuser() {
        return new Vedtaksstatuser();
    }

    /**
     * Create an instance of {@link Sak }
     * 
     */
    public Sak createSak() {
        return new Sak();
    }

    /**
     * Create an instance of {@link Bruker }
     * 
     */
    public Bruker createBruker() {
        return new Bruker();
    }

    /**
     * Create an instance of {@link Saksstatuser }
     * 
     */
    public Saksstatuser createSaksstatuser() {
        return new Saksstatuser();
    }

}
