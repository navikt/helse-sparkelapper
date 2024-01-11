
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger.WSHentYtelseskontraktListeResponse;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="response" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/meldinger}HentYtelseskontraktListeResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "response"
})
@XmlRootElement(name = "hentYtelseskontraktListeResponse")
public class HentYtelseskontraktListeResponse 
{

    protected WSHentYtelseskontraktListeResponse response;

    /**
     * Gets the value of the response property.
     * 
     * @return
     *     possible object is
     *     {@link WSHentYtelseskontraktListeResponse }
     *     
     */
    public WSHentYtelseskontraktListeResponse getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSHentYtelseskontraktListeResponse }
     *     
     */
    public void setResponse(WSHentYtelseskontraktListeResponse value) {
        this.response = value;
    }

    public HentYtelseskontraktListeResponse withResponse(WSHentYtelseskontraktListeResponse value) {
        setResponse(value);
        return this;
    }

}
