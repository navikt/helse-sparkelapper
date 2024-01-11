
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.ytelseskontrakt;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Merk! xsd:date kan valgfritt returneres med eller uten tidssone av tilbyder. Dette må håndteres av konsumenter.
 * 
 * <p>Java class for Periode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Periode"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fom" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="tom" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Periode", propOrder = {
    "fom",
    "tom"
})
public class WSPeriode 
{

    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar fom;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar tom;

    /**
     * Gets the value of the fom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFom() {
        return fom;
    }

    /**
     * Sets the value of the fom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFom(XMLGregorianCalendar value) {
        this.fom = value;
    }

    /**
     * Gets the value of the tom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTom() {
        return tom;
    }

    /**
     * Sets the value of the tom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTom(XMLGregorianCalendar value) {
        this.tom = value;
    }

    public WSPeriode withFom(XMLGregorianCalendar value) {
        setFom(value);
        return this;
    }

    public WSPeriode withTom(XMLGregorianCalendar value) {
        setTom(value);
        return this;
    }

}
