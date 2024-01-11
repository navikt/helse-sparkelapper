
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * Sak relatert til en bruker
 * 
 * <p>Java class for Sak complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Sak">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vedtakListe" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Vedtak" maxOccurs="unbounded"/>
 *         &lt;element name="fagsystemSakId" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Identifikator"/>
 *         &lt;element name="saksstatus" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Saksstatuser"/>
 *         &lt;element name="tema" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Tema"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sak", propOrder = {
    "vedtakListe",
    "fagsystemSakId",
    "saksstatus",
    "tema"
})
public class Sak {

    @XmlElement(required = true)
    protected List<Vedtak> vedtakListe;
    @XmlElement(required = true)
    protected String fagsystemSakId;
    @XmlElement(required = true)
    protected Saksstatuser saksstatus;
    @XmlElement(required = true)
    protected Tema tema;

    /**
     * Gets the value of the vedtakListe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vedtakListe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVedtakListe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Vedtak }
     * 
     * 
     */
    public List<Vedtak> getVedtakListe() {
        if (vedtakListe == null) {
            vedtakListe = new ArrayList<Vedtak>();
        }
        return this.vedtakListe;
    }

    /**
     * Gets the value of the fagsystemSakId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFagsystemSakId() {
        return fagsystemSakId;
    }

    /**
     * Sets the value of the fagsystemSakId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFagsystemSakId(String value) {
        this.fagsystemSakId = value;
    }

    /**
     * Gets the value of the saksstatus property.
     * 
     * @return
     *     possible object is
     *     {@link Saksstatuser }
     *     
     */
    public Saksstatuser getSaksstatus() {
        return saksstatus;
    }

    /**
     * Sets the value of the saksstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Saksstatuser }
     *     
     */
    public void setSaksstatus(Saksstatuser value) {
        this.saksstatus = value;
    }

    /**
     * Gets the value of the tema property.
     * 
     * @return
     *     possible object is
     *     {@link Tema }
     *     
     */
    public Tema getTema() {
        return tema;
    }

    /**
     * Sets the value of the tema property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tema }
     *     
     */
    public void setTema(Tema value) {
        this.tema = value;
    }

}
