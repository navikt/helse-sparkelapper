
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.feil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Sikkerhetsbegrensninger complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Sikkerhetsbegrensninger"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/feil&gt;Kodeverdi"&gt;
 *       &lt;attribute name="kodeverksRef" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://nav.no/kodeverk/Kodeverk/Sikkerhetsbegrensninger" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sikkerhetsbegrensninger")
public class WSSikkerhetsbegrensninger
    extends WSKodeverdi
    
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
        if (kodeverksRef == null) {
            return "http://nav.no/kodeverk/Kodeverk/Sikkerhetsbegrensninger";
        } else {
            return kodeverksRef;
        }
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

    public WSSikkerhetsbegrensninger withKodeverksRef(String value) {
        setKodeverksRef(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensninger withValue(String value) {
        setValue(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensninger withKodeRef(String value) {
        setKodeRef(value);
        return this;
    }

}
