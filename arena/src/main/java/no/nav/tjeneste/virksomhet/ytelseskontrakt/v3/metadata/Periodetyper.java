
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Periodetyper.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Periodetyper"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="bruksperiode"/&gt;
 *     &lt;enumeration value="gyldighetsperiode"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Periodetyper", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum Periodetyper {

    @XmlEnumValue("bruksperiode")
    BRUKSPERIODE("bruksperiode"),
    @XmlEnumValue("gyldighetsperiode")
    GYLDIGHETSPERIODE("gyldighetsperiode");
    private final String value;

    Periodetyper(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Periodetyper fromValue(String v) {
        for (Periodetyper c: Periodetyper.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
