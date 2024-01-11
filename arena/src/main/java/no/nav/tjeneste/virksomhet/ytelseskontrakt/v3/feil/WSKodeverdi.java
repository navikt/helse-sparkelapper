
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.feil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for Kodeverdi complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Kodeverdi"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *       &lt;attribute name="kodeRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Kodeverdi", propOrder = {
    "value"
})
@XmlSeeAlso({
    WSSikkerhetsbegrensninger.class
})
public class WSKodeverdi 
{

    @XmlValue
    protected String value;
    @XmlAttribute(name = "kodeRef")
    @XmlSchemaType(name = "anyURI")
    protected String kodeRef;

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

    public WSKodeverdi withValue(String value) {
        setValue(value);
        return this;
    }

    public WSKodeverdi withKodeRef(String value) {
        setKodeRef(value);
        return this;
    }

}
