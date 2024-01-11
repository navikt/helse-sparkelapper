
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1;

import jakarta.xml.bind.annotation.*;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="request" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/meldinger}FinnMeldekortUtbetalingsgrunnlagListeRequest"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "request"
})
@XmlRootElement(name = "finnMeldekortUtbetalingsgrunnlagListe")
public class FinnMeldekortUtbetalingsgrunnlagListe {

    @XmlElement(required = true)
    protected FinnMeldekortUtbetalingsgrunnlagListeRequest request;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link FinnMeldekortUtbetalingsgrunnlagListeRequest }
     *     
     */
    public FinnMeldekortUtbetalingsgrunnlagListeRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link FinnMeldekortUtbetalingsgrunnlagListeRequest }
     *     
     */
    public void setRequest(FinnMeldekortUtbetalingsgrunnlagListeRequest value) {
        this.request = value;
    }

}
