
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSPeriode;


/**
 * <p>Java class for HentYtelseskontraktListeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HentYtelseskontraktListeRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="personidentifikator" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Personidentifikator"/&gt;
 *         &lt;element name="periode" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Periode" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HentYtelseskontraktListeRequest", propOrder = {
    "personidentifikator",
    "periode"
})
public class WSHentYtelseskontraktListeRequest 
{

    @XmlElement(required = true)
    protected String personidentifikator;
    protected WSPeriode periode;

    /**
     * Gets the value of the personidentifikator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersonidentifikator() {
        return personidentifikator;
    }

    /**
     * Sets the value of the personidentifikator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersonidentifikator(String value) {
        this.personidentifikator = value;
    }

    /**
     * Gets the value of the periode property.
     * 
     * @return
     *     possible object is
     *     {@link WSPeriode }
     *     
     */
    public WSPeriode getPeriode() {
        return periode;
    }

    /**
     * Sets the value of the periode property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSPeriode }
     *     
     */
    public void setPeriode(WSPeriode value) {
        this.periode = value;
    }

    public WSHentYtelseskontraktListeRequest withPersonidentifikator(String value) {
        setPersonidentifikator(value);
        return this;
    }

    public WSHentYtelseskontraktListeRequest withPeriode(WSPeriode value) {
        setPeriode(value);
        return this;
    }

}
