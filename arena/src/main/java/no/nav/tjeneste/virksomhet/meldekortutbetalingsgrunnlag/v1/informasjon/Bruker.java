
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Bruker complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Bruker">
 *   &lt;complexContent>
 *     &lt;extension base="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Aktoer">
 *       &lt;sequence>
 *         &lt;element name="ident" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Identifikator"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bruker", propOrder = {
    "ident"
})
public class Bruker
    extends Aktoer
{

    @XmlElement(required = true)
    protected String ident;

    /**
     * Gets the value of the ident property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Sets the value of the ident property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdent(String value) {
        this.ident = value;
    }

}
