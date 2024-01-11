
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AktoerId complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AktoerId">
 *   &lt;complexContent>
 *     &lt;extension base="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Aktoer">
 *       &lt;sequence>
 *         &lt;element name="aktoerId" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Identifikator"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AktoerId", propOrder = {
    "aktoerId"
})
public class AktoerId
    extends Aktoer
{

    @XmlElement(required = true)
    protected String aktoerId;

    /**
     * Gets the value of the aktoerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAktoerId() {
        return aktoerId;
    }

    /**
     * Sets the value of the aktoerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAktoerId(String value) {
        this.aktoerId = value;
    }

}
