
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger.WSHentYtelseskontraktListeRequest;


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
 *         &lt;element name="request" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/meldinger}HentYtelseskontraktListeRequest"/&gt;
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
    "request"
})
@XmlRootElement(name = "hentYtelseskontraktListe")
public class HentYtelseskontraktListe 
{

    @XmlElement(required = true)
    protected WSHentYtelseskontraktListeRequest request;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link WSHentYtelseskontraktListeRequest }
     *     
     */
    public WSHentYtelseskontraktListeRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSHentYtelseskontraktListeRequest }
     *     
     */
    public void setRequest(WSHentYtelseskontraktListeRequest value) {
        this.request = value;
    }

    public HentYtelseskontraktListe withRequest(WSHentYtelseskontraktListeRequest value) {
        setRequest(value);
        return this;
    }

}
