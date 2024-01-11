
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for Kodeverdi complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Kodeverdi">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="kodeRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="termnavn" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Kodeverdi", propOrder = {
    "value"
})
@XmlSeeAlso({
    Tema.class,
    Vedtaksstatuser.class,
    Saksstatuser.class
})
public class Kodeverdi {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "kodeRef")
    @XmlSchemaType(name = "anyURI")
    protected String kodeRef;
    @XmlAttribute(name = "termnavn")
    protected String termnavn;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the kodeRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKodeRef() {
        return kodeRef;
    }

    /**
     * Sets the value of the kodeRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKodeRef(String value) {
        this.kodeRef = value;
    }

    /**
     * Gets the value of the termnavn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermnavn() {
        return termnavn;
    }

    /**
     * Sets the value of the termnavn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermnavn(String value) {
        this.termnavn = value;
    }

}
