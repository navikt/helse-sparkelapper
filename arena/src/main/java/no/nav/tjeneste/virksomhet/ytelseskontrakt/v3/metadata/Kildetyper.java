
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Kildetyper.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Kildetyper"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="intern"/&gt;
 *     &lt;enumeration value="samhandler"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Kildetyper", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum Kildetyper {

    @XmlEnumValue("intern")
    INTERN("intern"),
    @XmlEnumValue("samhandler")
    SAMHANDLER("samhandler");
    private final String value;

    Kildetyper(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Kildetyper fromValue(String v) {
        for (Kildetyper c: Kildetyper.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
