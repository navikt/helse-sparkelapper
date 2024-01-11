
package no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.informasjon.Sak;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for FinnMeldekortUtbetalingsgrunnlagListeResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FinnMeldekortUtbetalingsgrunnlagListeResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldekortUtbetalingsgrunnlagListe" type="{http://nav.no/tjeneste/virksomhet/meldekortUtbetalingsgrunnlag/v1/informasjon}Sak" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FinnMeldekortUtbetalingsgrunnlagListeResponse", propOrder = {
    "meldekortUtbetalingsgrunnlagListe"
})
public class FinnMeldekortUtbetalingsgrunnlagListeResponse {

    protected List<Sak> meldekortUtbetalingsgrunnlagListe;

    /**
     * Gets the value of the meldekortUtbetalingsgrunnlagListe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meldekortUtbetalingsgrunnlagListe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeldekortUtbetalingsgrunnlagListe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sak }
     * 
     * 
     */
    public List<Sak> getMeldekortUtbetalingsgrunnlagListe() {
        if (meldekortUtbetalingsgrunnlagListe == null) {
            meldekortUtbetalingsgrunnlagListe = new ArrayList<Sak>();
        }
        return this.meldekortUtbetalingsgrunnlagListe;
    }

}
