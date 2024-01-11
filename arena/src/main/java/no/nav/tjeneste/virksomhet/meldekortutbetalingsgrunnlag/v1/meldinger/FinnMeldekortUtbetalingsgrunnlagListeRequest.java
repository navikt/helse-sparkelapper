
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Tema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for FinnMeldekortUtbetalingsgrunnlagListeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FinnMeldekortUtbetalingsgrunnlagListeRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ident" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Aktoer"/>
 *         &lt;element name="periode" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Periode" minOccurs="0"/>
 *         &lt;element name="temaListe" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Tema" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FinnMeldekortUtbetalingsgrunnlagListeRequest", propOrder = {
    "ident",
    "periode",
    "temaListe"
})
public class FinnMeldekortUtbetalingsgrunnlagListeRequest {

    @XmlElement(required = true)
    protected Aktoer ident;
    protected Periode periode;
    @XmlElement(required = true)
    protected List<Tema> temaListe;

    /**
     * Gets the value of the ident property.
     * 
     * @return
     *     possible object is
     *     {@link Aktoer }
     *     
     */
    public Aktoer getIdent() {
        return ident;
    }

    /**
     * Sets the value of the ident property.
     * 
     * @param value
     *     allowed object is
     *     {@link Aktoer }
     *     
     */
    public void setIdent(Aktoer value) {
        this.ident = value;
    }

    /**
     * Gets the value of the periode property.
     * 
     * @return
     *     possible object is
     *     {@link Periode }
     *     
     */
    public Periode getPeriode() {
        return periode;
    }

    /**
     * Sets the value of the periode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Periode }
     *     
     */
    public void setPeriode(Periode value) {
        this.periode = value;
    }

    /**
     * Gets the value of the temaListe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the temaListe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemaListe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Tema }
     * 
     * 
     */
    public List<Tema> getTemaListe() {
        if (temaListe == null) {
            temaListe = new ArrayList<Tema>();
        }
        return this.temaListe;
    }

}
