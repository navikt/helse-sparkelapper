
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Rettighetsgruppe complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Rettighetsgruppe"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="rettighetsGruppe" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Rettighetsgrupper"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rettighetsgruppe2", propOrder = {
    "rettighetsGruppe"
})
public class WSRettighetsgruppe 
{

    @XmlElement(required = true)
    protected String rettighetsGruppe;

    /**
     * Gets the value of the rettighetsGruppe property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRettighetsGruppe() {
        return rettighetsGruppe;
    }

    /**
     * Sets the value of the rettighetsGruppe property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRettighetsGruppe(String value) {
        this.rettighetsGruppe = value;
    }

    public WSRettighetsgruppe withRettighetsGruppe(String value) {
        setRettighetsGruppe(value);
        return this;
    }

}
