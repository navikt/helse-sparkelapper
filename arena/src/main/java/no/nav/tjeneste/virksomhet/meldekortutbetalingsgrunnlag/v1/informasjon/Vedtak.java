
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * Ytelsesvedtak (livsoppholdsytelser) for en sak
 * 
 * <p>Java class for Vedtak complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Vedtak">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldekortListe" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Meldekort" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="vedtaksperiode" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Periode"/>
 *         &lt;element name="vedtaksstatus" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Vedtaksstatuser"/>
 *         &lt;element name="vedtaksdato" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="datoKravMottatt" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="dagsats" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Vedtak", propOrder = {
    "meldekortListe",
    "vedtaksperiode",
    "vedtaksstatus",
    "vedtaksdato",
    "datoKravMottatt",
    "dagsats"
})
public class Vedtak {

    protected List<Meldekort> meldekortListe;
    @XmlElement(required = true)
    protected Periode vedtaksperiode;
    @XmlElement(required = true)
    protected Vedtaksstatuser vedtaksstatus;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar vedtaksdato;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar datoKravMottatt;
    protected double dagsats;

    /**
     * Gets the value of the meldekortListe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meldekortListe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeldekortListe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Meldekort }
     * 
     * 
     */
    public List<Meldekort> getMeldekortListe() {
        if (meldekortListe == null) {
            meldekortListe = new ArrayList<Meldekort>();
        }
        return this.meldekortListe;
    }

    /**
     * Gets the value of the vedtaksperiode property.
     * 
     * @return
     *     possible object is
     *     {@link Periode }
     *     
     */
    public Periode getVedtaksperiode() {
        return vedtaksperiode;
    }

    /**
     * Sets the value of the vedtaksperiode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Periode }
     *     
     */
    public void setVedtaksperiode(Periode value) {
        this.vedtaksperiode = value;
    }

    /**
     * Gets the value of the vedtaksstatus property.
     * 
     * @return
     *     possible object is
     *     {@link Vedtaksstatuser }
     *     
     */
    public Vedtaksstatuser getVedtaksstatus() {
        return vedtaksstatus;
    }

    /**
     * Sets the value of the vedtaksstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Vedtaksstatuser }
     *     
     */
    public void setVedtaksstatus(Vedtaksstatuser value) {
        this.vedtaksstatus = value;
    }

    /**
     * Gets the value of the vedtaksdato property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getVedtaksdato() {
        return vedtaksdato;
    }

    /**
     * Sets the value of the vedtaksdato property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setVedtaksdato(XMLGregorianCalendar value) {
        this.vedtaksdato = value;
    }

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
     * Gets the value of the dagsats property.
     * 
     */
    public double getDagsats() {
        return dagsats;
    }

    /**
     * Sets the value of the dagsats property.
     * 
     */
    public void setDagsats(double value) {
        this.dagsats = value;
    }

}
