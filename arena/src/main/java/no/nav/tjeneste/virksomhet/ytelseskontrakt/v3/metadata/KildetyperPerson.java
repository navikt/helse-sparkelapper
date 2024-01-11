
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for KildetyperPerson.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="KildetyperPerson"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="andre"/&gt;
 *     &lt;enumeration value="bruker"/&gt;
 *     &lt;enumeration value="medarbeider"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "KildetyperPerson", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum KildetyperPerson {

    @XmlEnumValue("andre")
    ANDRE("andre"),
    @XmlEnumValue("bruker")
    BRUKER("bruker"),
    @XmlEnumValue("medarbeider")
    MEDARBEIDER("medarbeider");
    private final String value;

    KildetyperPerson(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static KildetyperPerson fromValue(String v) {
        for (KildetyperPerson c: KildetyperPerson.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
