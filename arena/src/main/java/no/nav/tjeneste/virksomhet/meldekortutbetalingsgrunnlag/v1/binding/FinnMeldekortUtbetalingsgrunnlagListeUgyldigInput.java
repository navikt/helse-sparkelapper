
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil.UgyldigInput;

import jakarta.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b14002
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "finnMeldekortUtbetalingsgrunnlagListeugyldigInput", targetNamespace = "http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1")
public class FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UgyldigInput faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput(String message, UgyldigInput faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput(String message, UgyldigInput faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.feil.UgyldigInput
     */
    public UgyldigInput getFaultInfo() {
        return faultInfo;
    }

}