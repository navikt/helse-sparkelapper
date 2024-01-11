
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for Vedtaksstatuser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Vedtaksstatuser">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon>Kodeverdi">
 *       &lt;attribute name="kodeverksRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Vedtaksstatuser")
public class Vedtaksstatuser
    extends Kodeverdi
{

    @XmlAttribute(name = "kodeverksRef")
    @XmlSchemaType(name = "anyURI")
    protected String kodeverksRef;

    /**
     * Gets the value of the kodeverksRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKodeverksRef() {
        return kodeverksRef;
    }

    /**
     * Sets the value of the kodeverksRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKodeverksRef(String value) {
        this.kodeverksRef = value;
    }

}
