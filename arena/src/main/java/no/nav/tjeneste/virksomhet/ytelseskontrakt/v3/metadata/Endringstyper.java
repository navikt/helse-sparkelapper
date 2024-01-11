
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Endringstyper.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Endringstyper"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="endret"/&gt;
 *     &lt;enumeration value="ny"/&gt;
 *     &lt;enumeration value="slettet"/&gt;
 *     &lt;enumeration value="utgaatt"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Endringstyper", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum Endringstyper {

    @XmlEnumValue("endret")
    ENDRET("endret"),
    @XmlEnumValue("ny")
    NY("ny"),
    @XmlEnumValue("slettet")
    SLETTET("slettet"),
    @XmlEnumValue("utgaatt")
    UTGAATT("utgaatt");
    private final String value;

    Endringstyper(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Endringstyper fromValue(String v) {
        for (Endringstyper c: Endringstyper.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
