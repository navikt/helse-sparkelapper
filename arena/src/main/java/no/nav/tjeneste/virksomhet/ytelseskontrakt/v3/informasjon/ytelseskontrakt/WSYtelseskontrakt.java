
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;


import jakarta.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * <p>Java class for Ytelseskontrakt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Ytelseskontrakt"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="datoKravMottatt" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="fagsystemSakId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="status" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Fagsakstatus"/&gt;
 *         &lt;element name="ytelsestype" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Ytelsestyper"/&gt;
 *         &lt;element name="ihtVedtak" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Vedtak" maxOccurs="unbounded"/&gt;
 *         &lt;element name="bortfallsprosentDagerIgjen" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="bortfallsprosentUkerIgjen" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata}Gyldighetsperiode"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ytelseskontrakt", propOrder = {
    "datoKravMottatt",
    "fagsystemSakId",
    "status",
    "ytelsestype",
    "ihtVedtak",
    "bortfallsprosentDagerIgjen",
    "bortfallsprosentUkerIgjen"
})
@XmlSeeAlso({
    WSDagpengekontrakt.class
})
public class WSYtelseskontrakt 
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar datoKravMottatt;
    protected Integer fagsystemSakId;
    @XmlElement(required = true)
    protected String status;
    @XmlElement(required = true)
    protected String ytelsestype;
    @XmlElement(required = true)
    protected List<WSVedtak> ihtVedtak;
    protected Integer bortfallsprosentDagerIgjen;
    protected Integer bortfallsprosentUkerIgjen;
    @XmlAttribute(name = "fomGyldighetsperiode")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fomGyldighetsperiode;
    @XmlAttribute(name = "tomGyldighetsperiode")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar tomGyldighetsperiode;

    /**
     * Gets the value of the datoKravMottatt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDatoKravMottatt() {
        return datoKravMottatt;
    }

    /**
     * Sets the value of the datoKravMottatt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDatoKravMottatt(XMLGregorianCalendar value) {
        this.datoKravMottatt = value;
    }

    /**
     * Gets the value of the fagsystemSakId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFagsystemSakId() {
        return fagsystemSakId;
    }

    /**
     * Sets the value of the fagsystemSakId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFagsystemSakId(Integer value) {
        this.fagsystemSakId = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the ytelsestype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYtelsestype() {
        return ytelsestype;
    }

    /**
     * Sets the value of the ytelsestype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYtelsestype(String value) {
        this.ytelsestype = value;
    }

    /**
     * Gets the value of the ihtVedtak property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ihtVedtak property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIhtVedtak().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WSVedtak }
     * 
     * 
     */
    public List<WSVedtak> getIhtVedtak() {
        if (ihtVedtak == null) {
            ihtVedtak = new ArrayList<WSVedtak>();
        }
        return this.ihtVedtak;
    }

    /**
     * Gets the value of the bortfallsprosentDagerIgjen property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBortfallsprosentDagerIgjen() {
        return bortfallsprosentDagerIgjen;
    }

    /**
     * Sets the value of the bortfallsprosentDagerIgjen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBortfallsprosentDagerIgjen(Integer value) {
        this.bortfallsprosentDagerIgjen = value;
    }

    /**
     * Gets the value of the bortfallsprosentUkerIgjen property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBortfallsprosentUkerIgjen() {
        return bortfallsprosentUkerIgjen;
    }

    /**
     * Sets the value of the bortfallsprosentUkerIgjen property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBortfallsprosentUkerIgjen(Integer value) {
        this.bortfallsprosentUkerIgjen = value;
    }

    /**
     * Gets the value of the fomGyldighetsperiode property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFomGyldighetsperiode() {
        return fomGyldighetsperiode;
    }

    /**
     * Sets the value of the fomGyldighetsperiode property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFomGyldighetsperiode(XMLGregorianCalendar value) {
        this.fomGyldighetsperiode = value;
    }

    /**
     * Gets the value of the tomGyldighetsperiode property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTomGyldighetsperiode() {
        return tomGyldighetsperiode;
    }

    /**
     * Sets the value of the tomGyldighetsperiode property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTomGyldighetsperiode(XMLGregorianCalendar value) {
        this.tomGyldighetsperiode = value;
    }

    public WSYtelseskontrakt withDatoKravMottatt(XMLGregorianCalendar value) {
        setDatoKravMottatt(value);
        return this;
    }

    public WSYtelseskontrakt withFagsystemSakId(Integer value) {
        setFagsystemSakId(value);
        return this;
    }

    public WSYtelseskontrakt withStatus(String value) {
        setStatus(value);
        return this;
    }

    public WSYtelseskontrakt withYtelsestype(String value) {
        setYtelsestype(value);
        return this;
    }

    public WSYtelseskontrakt withIhtVedtak(WSVedtak... values) {
        if (values!= null) {
            for (WSVedtak value: values) {
                getIhtVedtak().add(value);
            }
        }
        return this;
    }

    public WSYtelseskontrakt withIhtVedtak(Collection<WSVedtak> values) {
        if (values!= null) {
            getIhtVedtak().addAll(values);
        }
        return this;
    }

    public WSYtelseskontrakt withBortfallsprosentDagerIgjen(Integer value) {
        setBortfallsprosentDagerIgjen(value);
        return this;
    }

    public WSYtelseskontrakt withBortfallsprosentUkerIgjen(Integer value) {
        setBortfallsprosentUkerIgjen(value);
        return this;
    }

    public WSYtelseskontrakt withFomGyldighetsperiode(XMLGregorianCalendar value) {
        setFomGyldighetsperiode(value);
        return this;
    }

    public WSYtelseskontrakt withTomGyldighetsperiode(XMLGregorianCalendar value) {
        setTomGyldighetsperiode(value);
        return this;
    }

}
