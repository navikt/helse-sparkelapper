
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;


/**
 * <p>Java class for Dagpengekontrakt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Dagpengekontrakt"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/informasjon/ytelseskontrakt}Ytelseskontrakt"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="antallDagerIgjen" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="antallUkerIgjen" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="antallDagerIgjenUnderPermittering" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="antallUkerIgjenUnderPermittering" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Dagpengekontrakt2", propOrder = {
    "antallDagerIgjen",
    "antallUkerIgjen",
    "antallDagerIgjenUnderPermittering",
    "antallUkerIgjenUnderPermittering"
})
public class WSDagpengekontrakt
    extends WSYtelseskontrakt
    
{

    protected int antallDagerIgjen;
    protected int antallUkerIgjen;
    protected Integer antallDagerIgjenUnderPermittering;
    protected Integer antallUkerIgjenUnderPermittering;

    /**
     * Gets the value of the antallDagerIgjen property.
     * 
     */
    public int getAntallDagerIgjen() {
        return antallDagerIgjen;
    }

    /**
     * Sets the value of the antallDagerIgjen property.
     * 
     */
    public void setAntallDagerIgjen(int value) {
        this.antallDagerIgjen = value;
    }

    /**
     * Gets the value of the antallUkerIgjen property.
     * 
     */
    public int getAntallUkerIgjen() {
        return antallUkerIgjen;
    }

    /**
     * Sets the value of the antallUkerIgjen property.
     * 
     */
    public void setAntallUkerIgjen(int value) {
        this.antallUkerIgjen = value;
    }

    /**
     * Gets the value of the antallDagerIgjenUnderPermittering property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntallDagerIgjenUnderPermittering() {
        return antallDagerIgjenUnderPermittering;
    }

    /**
     * Sets the value of the antallDagerIgjenUnderPermittering property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntallDagerIgjenUnderPermittering(Integer value) {
        this.antallDagerIgjenUnderPermittering = value;
    }

    /**
     * Gets the value of the antallUkerIgjenUnderPermittering property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAntallUkerIgjenUnderPermittering() {
        return antallUkerIgjenUnderPermittering;
    }

    /**
     * Sets the value of the antallUkerIgjenUnderPermittering property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAntallUkerIgjenUnderPermittering(Integer value) {
        this.antallUkerIgjenUnderPermittering = value;
    }

    public WSDagpengekontrakt withAntallDagerIgjen(int value) {
        setAntallDagerIgjen(value);
        return this;
    }

    public WSDagpengekontrakt withAntallUkerIgjen(int value) {
        setAntallUkerIgjen(value);
        return this;
    }

    public WSDagpengekontrakt withAntallDagerIgjenUnderPermittering(Integer value) {
        setAntallDagerIgjenUnderPermittering(value);
        return this;
    }

    public WSDagpengekontrakt withAntallUkerIgjenUnderPermittering(Integer value) {
        setAntallUkerIgjenUnderPermittering(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withDatoKravMottatt(XMLGregorianCalendar value) {
        setDatoKravMottatt(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withFagsystemSakId(Integer value) {
        setFagsystemSakId(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withStatus(String value) {
        setStatus(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withYtelsestype(String value) {
        setYtelsestype(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withIhtVedtak(WSVedtak... values) {
        if (values!= null) {
            for (WSVedtak value: values) {
                getIhtVedtak().add(value);
            }
        }
        return this;
    }

    @Override
    public WSDagpengekontrakt withIhtVedtak(Collection<WSVedtak> values) {
        if (values!= null) {
            getIhtVedtak().addAll(values);
        }
        return this;
    }

    @Override
    public WSDagpengekontrakt withBortfallsprosentDagerIgjen(Integer value) {
        setBortfallsprosentDagerIgjen(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withBortfallsprosentUkerIgjen(Integer value) {
        setBortfallsprosentUkerIgjen(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withFomGyldighetsperiode(XMLGregorianCalendar value) {
        setFomGyldighetsperiode(value);
        return this;
    }

    @Override
    public WSDagpengekontrakt withTomGyldighetsperiode(XMLGregorianCalendar value) {
        setTomGyldighetsperiode(value);
        return this;
    }

}
