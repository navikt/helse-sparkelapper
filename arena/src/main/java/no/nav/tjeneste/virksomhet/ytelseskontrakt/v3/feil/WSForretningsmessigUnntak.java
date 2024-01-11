
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.feil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ForretningsmessigUnntak complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ForretningsmessigUnntak"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="feilkilde" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="feilaarsak" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="feilmelding" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tidspunkt" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ForretningsmessigUnntak", propOrder = {
    "feilkilde",
    "feilaarsak",
    "feilmelding",
    "tidspunkt"
})
@XmlSeeAlso({
    WSSikkerhetsbegrensning.class
})
public abstract class WSForretningsmessigUnntak 
{

    @XmlElement(required = true)
    protected String feilkilde;
    @XmlElement(required = true)
    protected String feilaarsak;
    @XmlElement(required = true)
    protected String feilmelding;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar tidspunkt;

    /**
     * Gets the value of the feilkilde property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeilkilde() {
        return feilkilde;
    }

    /**
     * Sets the value of the feilkilde property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeilkilde(String value) {
        this.feilkilde = value;
    }

    /**
     * Gets the value of the feilaarsak property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeilaarsak() {
        return feilaarsak;
    }

    /**
     * Sets the value of the feilaarsak property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeilaarsak(String value) {
        this.feilaarsak = value;
    }

    /**
     * Gets the value of the feilmelding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeilmelding() {
        return feilmelding;
    }

    /**
     * Sets the value of the feilmelding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeilmelding(String value) {
        this.feilmelding = value;
    }

    /**
     * Gets the value of the tidspunkt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTidspunkt() {
        return tidspunkt;
    }

    /**
     * Sets the value of the tidspunkt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTidspunkt(XMLGregorianCalendar value) {
        this.tidspunkt = value;
    }

    public WSForretningsmessigUnntak withFeilkilde(String value) {
        setFeilkilde(value);
        return this;
    }

    public WSForretningsmessigUnntak withFeilaarsak(String value) {
        setFeilaarsak(value);
        return this;
    }

    public WSForretningsmessigUnntak withFeilmelding(String value) {
        setFeilmelding(value);
        return this;
    }

    public WSForretningsmessigUnntak withTidspunkt(XMLGregorianCalendar value) {
        setTidspunkt(value);
        return this;
    }

}
