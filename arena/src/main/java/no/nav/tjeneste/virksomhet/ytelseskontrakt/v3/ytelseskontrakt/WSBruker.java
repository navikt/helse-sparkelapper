
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.ytelseskontrakt;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe;


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
@XmlType(name = "Bruker", propOrder = {
    "rettighetsgruppe"
})
public class WSBruker 
{

    protected no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe rettighetsgruppe;

    /**
     * Gets the value of the rettighetsgruppe property.
     * 
     * @return
     *     possible object is
     *     {@link no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe }
     *     
     */
    public no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe getRettighetsgruppe() {
        return rettighetsgruppe;
    }

    /**
     * Sets the value of the rettighetsgruppe property.
     * 
     * @param value
     *     allowed object is
     *     {@link no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe }
     *     
     */
    public void setRettighetsgruppe(no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe value) {
        this.rettighetsgruppe = value;
    }

    public WSBruker withRettighetsgruppe(WSRettighetsgruppe value) {
        setRettighetsgruppe(value);
        return this;
    }

}
