
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.feil;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for Sikkerhetsbegrensning complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Sikkerhetsbegrensning"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/feil}ForretningsmessigUnntak"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sikkerhetsbegrensning" type="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/feil}Sikkerhetsbegrensninger" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Sikkerhetsbegrensning", propOrder = {
    "sikkerhetsbegrensning"
})
public class WSSikkerhetsbegrensning
    extends WSForretningsmessigUnntak
    
{

    protected WSSikkerhetsbegrensninger sikkerhetsbegrensning;

    /**
     * Gets the value of the sikkerhetsbegrensning property.
     * 
     * @return
     *     possible object is
     *     {@link WSSikkerhetsbegrensninger }
     *     
     */
    public WSSikkerhetsbegrensninger getSikkerhetsbegrensning() {
        return sikkerhetsbegrensning;
    }

    /**
     * Sets the value of the sikkerhetsbegrensning property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSSikkerhetsbegrensninger }
     *     
     */
    public void setSikkerhetsbegrensning(WSSikkerhetsbegrensninger value) {
        this.sikkerhetsbegrensning = value;
    }

    public WSSikkerhetsbegrensning withSikkerhetsbegrensning(WSSikkerhetsbegrensninger value) {
        setSikkerhetsbegrensning(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensning withFeilkilde(String value) {
        setFeilkilde(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensning withFeilaarsak(String value) {
        setFeilaarsak(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensning withFeilmelding(String value) {
        setFeilmelding(value);
        return this;
    }

    @Override
    public WSSikkerhetsbegrensning withTidspunkt(XMLGregorianCalendar value) {
        setTidspunkt(value);
        return this;
    }

}
