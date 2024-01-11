
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Meldekort som gjelder for en gitt vedtaksperiode
 * 
 * <p>Java class for Meldekort complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Meldekort">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldekortperiode" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Periode"/>
 *         &lt;element name="dagsats" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="beloep" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="utbetalingsgrad" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Meldekort", propOrder = {
    "meldekortperiode",
    "dagsats",
    "beloep",
    "utbetalingsgrad"
})
public class Meldekort {

    @XmlElement(required = true)
    protected Periode meldekortperiode;
    protected double dagsats;
    protected double beloep;
    protected double utbetalingsgrad;

    /**
     * Gets the value of the meldekortperiode property.
     * 
     * @return
     *     possible object is
     *     {@link Periode }
     *     
     */
    public Periode getMeldekortperiode() {
        return meldekortperiode;
    }

    /**
     * Sets the value of the meldekortperiode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Periode }
     *     
     */
    public void setMeldekortperiode(Periode value) {
        this.meldekortperiode = value;
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

    /**
     * Gets the value of the beloep property.
     * 
     */
    public double getBeloep() {
        return beloep;
    }

    /**
     * Sets the value of the beloep property.
     * 
     */
    public void setBeloep(double value) {
        this.beloep = value;
    }

    /**
     * Gets the value of the utbetalingsgrad property.
     * 
     */
    public double getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    /**
     * Sets the value of the utbetalingsgrad property.
     * 
     */
    public void setUtbetalingsgrad(double value) {
        this.utbetalingsgrad = value;
    }

}
