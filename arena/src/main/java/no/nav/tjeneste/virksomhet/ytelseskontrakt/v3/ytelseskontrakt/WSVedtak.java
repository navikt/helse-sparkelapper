
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.ytelseskontrakt;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;



/**
 * <p>Java class for Vedtak complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Vedtak"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Beslutning"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="periodetypeForYtelse" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}PeriodetypeForYtelse" minOccurs="0"/&gt;
 *         &lt;element name="uttaksgrad" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="vedtakBruttoBeloep" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="vedtakNettoBeloep" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="vedtaksperiode" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Periode"/&gt;
 *         &lt;element name="status" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Vedtaksstatus"/&gt;
 *         &lt;element name="vedtakstype" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Vedtakstyper"/&gt;
 *         &lt;element name="aktivitetsfase" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Aktivitetsfaser" minOccurs="0"/&gt;
 *         &lt;element name="dagsats" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Vedtak", propOrder = {
    "periodetypeForYtelse",
    "uttaksgrad",
    "vedtakBruttoBeloep",
    "vedtakNettoBeloep",
    "vedtaksperiode",
    "status",
    "vedtakstype",
    "aktivitetsfase",
    "dagsats"
})
public class WSVedtak
    extends WSBeslutning
    
{

    protected String periodetypeForYtelse;
    protected Integer uttaksgrad;
    protected Integer vedtakBruttoBeloep;
    protected Integer vedtakNettoBeloep;
    @XmlElement(required = true)
    protected WSPeriode vedtaksperiode;
    @XmlElement(required = true)
    protected String status;
    @XmlElement(required = true)
    protected String vedtakstype;
    protected String aktivitetsfase;
    protected Integer dagsats;

    /**
     * Gets the value of the periodetypeForYtelse property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPeriodetypeForYtelse() {
        return periodetypeForYtelse;
    }

    /**
     * Sets the value of the periodetypeForYtelse property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPeriodetypeForYtelse(String value) {
        this.periodetypeForYtelse = value;
    }

    /**
     * Gets the value of the uttaksgrad property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUttaksgrad() {
        return uttaksgrad;
    }

    /**
     * Sets the value of the uttaksgrad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUttaksgrad(Integer value) {
        this.uttaksgrad = value;
    }

    /**
     * Gets the value of the vedtakBruttoBeloep property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVedtakBruttoBeloep() {
        return vedtakBruttoBeloep;
    }

    /**
     * Sets the value of the vedtakBruttoBeloep property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVedtakBruttoBeloep(Integer value) {
        this.vedtakBruttoBeloep = value;
    }

    /**
     * Gets the value of the vedtakNettoBeloep property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getVedtakNettoBeloep() {
        return vedtakNettoBeloep;
    }

    /**
     * Sets the value of the vedtakNettoBeloep property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVedtakNettoBeloep(Integer value) {
        this.vedtakNettoBeloep = value;
    }

    /**
     * Gets the value of the vedtaksperiode property.
     * 
     * @return
     *     possible object is
     *     {@link WSPeriode }
     *     
     */
    public WSPeriode getVedtaksperiode() {
        return vedtaksperiode;
    }

    /**
     * Sets the value of the vedtaksperiode property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSPeriode }
     *     
     */
    public void setVedtaksperiode(WSPeriode value) {
        this.vedtaksperiode = value;
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
     * Gets the value of the vedtakstype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVedtakstype() {
        return vedtakstype;
    }

    /**
     * Sets the value of the vedtakstype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVedtakstype(String value) {
        this.vedtakstype = value;
    }

    /**
     * Gets the value of the aktivitetsfase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAktivitetsfase() {
        return aktivitetsfase;
    }

    /**
     * Sets the value of the aktivitetsfase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAktivitetsfase(String value) {
        this.aktivitetsfase = value;
    }

    /**
     * Gets the value of the dagsats property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDagsats() {
        return dagsats;
    }

    /**
     * Sets the value of the dagsats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDagsats(Integer value) {
        this.dagsats = value;
    }

    public WSVedtak withPeriodetypeForYtelse(String value) {
        setPeriodetypeForYtelse(value);
        return this;
    }

    public WSVedtak withUttaksgrad(Integer value) {
        setUttaksgrad(value);
        return this;
    }

    public WSVedtak withVedtakBruttoBeloep(Integer value) {
        setVedtakBruttoBeloep(value);
        return this;
    }

    public WSVedtak withVedtakNettoBeloep(Integer value) {
        setVedtakNettoBeloep(value);
        return this;
    }

    public WSVedtak withVedtaksperiode(WSPeriode value) {
        setVedtaksperiode(value);
        return this;
    }

    public WSVedtak withStatus(String value) {
        setStatus(value);
        return this;
    }

    public WSVedtak withVedtakstype(String value) {
        setVedtakstype(value);
        return this;
    }

    public WSVedtak withAktivitetsfase(String value) {
        setAktivitetsfase(value);
        return this;
    }

    public WSVedtak withDagsats(Integer value) {
        setDagsats(value);
        return this;
    }

    @Override
    public WSVedtak withBeslutningsdato(XMLGregorianCalendar value) {
        setBeslutningsdato(value);
        return this;
    }

}
