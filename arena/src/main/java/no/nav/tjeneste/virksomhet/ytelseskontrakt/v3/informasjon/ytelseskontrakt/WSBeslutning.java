
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;

import jakarta.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Merk! xsd:date kan valgfritt returneres med eller uten tidssone av tilbyder. Dette må håndteres av konsumenter.
 * 
 * <p>Java class for Beslutning complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Beslutning"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="beslutningsdato" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Beslutning", propOrder = {
    "beslutningsdato"
})
@XmlSeeAlso({
    WSVedtak.class
})
public abstract class WSBeslutning
{

    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar beslutningsdato;

    /**
     * Gets the value of the beslutningsdato property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBeslutningsdato() {
        return beslutningsdato;
    }

    /**
     * Sets the value of the beslutningsdato property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBeslutningsdato(XMLGregorianCalendar value) {
        this.beslutningsdato = value;
    }

    public WSBeslutning withBeslutningsdato(XMLGregorianCalendar value) {
        setBeslutningsdato(value);
        return this;
    }

}
