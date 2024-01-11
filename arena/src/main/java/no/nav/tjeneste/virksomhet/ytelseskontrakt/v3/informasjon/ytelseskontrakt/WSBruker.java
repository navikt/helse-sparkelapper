
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Bruker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Bruker"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="rettighetsgruppe" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Rettighetsgruppe" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bruker2", propOrder = {
    "rettighetsgruppe"
})
public class WSBruker 
{

    protected WSRettighetsgruppe rettighetsgruppe;

    /**
     * Gets the value of the rettighetsgruppe property.
     * 
     * @return
     *     possible object is
     *     {@link WSRettighetsgruppe }
     *     
     */
    public WSRettighetsgruppe getRettighetsgruppe() {
        return rettighetsgruppe;
    }

    /**
     * Sets the value of the rettighetsgruppe property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSRettighetsgruppe }
     *     
     */
    public void setRettighetsgruppe(WSRettighetsgruppe value) {
        this.rettighetsgruppe = value;
    }

    public WSBruker withRettighetsgruppe(WSRettighetsgruppe value) {
        setRettighetsgruppe(value);
        return this;
    }

}
