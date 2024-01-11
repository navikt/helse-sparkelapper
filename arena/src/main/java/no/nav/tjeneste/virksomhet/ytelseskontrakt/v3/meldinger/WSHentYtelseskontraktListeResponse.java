
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSBruker;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSYtelseskontrakt;


/**
 * <p>Java class for HentYtelseskontraktListeResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HentYtelseskontraktListeResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="bruker" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Bruker" minOccurs="0"/&gt;
 *         &lt;element name="ytelseskontraktListe" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Ytelseskontrakt" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HentYtelseskontraktListeResponse", propOrder = {
    "bruker",
    "ytelseskontraktListe"
})
public class WSHentYtelseskontraktListeResponse 
{

    protected WSBruker bruker;
    protected List<WSYtelseskontrakt> ytelseskontraktListe;

    /**
     * Gets the value of the bruker property.
     * 
     * @return
     *     possible object is
     *     {@link WSBruker }
     *     
     */
    public WSBruker getBruker() {
        return bruker;
    }

    /**
     * Sets the value of the bruker property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSBruker }
     *     
     */
    public void setBruker(WSBruker value) {
        this.bruker = value;
    }

    /**
     * Gets the value of the ytelseskontraktListe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ytelseskontraktListe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getYtelseskontraktListe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WSYtelseskontrakt }
     * 
     * 
     */
    public List<WSYtelseskontrakt> getYtelseskontraktListe() {
        if (ytelseskontraktListe == null) {
            ytelseskontraktListe = new ArrayList<WSYtelseskontrakt>();
        }
        return this.ytelseskontraktListe;
    }

    public WSHentYtelseskontraktListeResponse withBruker(WSBruker value) {
        setBruker(value);
        return this;
    }

    public WSHentYtelseskontraktListeResponse withYtelseskontraktListe(WSYtelseskontrakt... values) {
        if (values!= null) {
            for (WSYtelseskontrakt value: values) {
                getYtelseskontraktListe().add(value);
            }
        }
        return this;
    }

    public WSHentYtelseskontraktListeResponse withYtelseskontraktListe(Collection<WSYtelseskontrakt> values) {
        if (values!= null) {
            getYtelseskontraktListe().addAll(values);
        }
        return this;
    }

}
