
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Klassifiseringskoder.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Klassifiseringskoder"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ekstraHoeyt"/&gt;
 *     &lt;enumeration value="hoeyt"/&gt;
 *     &lt;enumeration value="lavt"/&gt;
 *     &lt;enumeration value="middels"/&gt;
 *     &lt;enumeration value="moderat"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Klassifiseringskoder", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum Klassifiseringskoder {

    @XmlEnumValue("ekstraHoeyt")
    EKSTRA_HOEYT("ekstraHoeyt"),
    @XmlEnumValue("hoeyt")
    HOEYT("hoeyt"),
    @XmlEnumValue("lavt")
    LAVT("lavt"),
    @XmlEnumValue("middels")
    MIDDELS("middels"),
    @XmlEnumValue("moderat")
    MODERAT("moderat");
    private final String value;

    Klassifiseringskoder(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Klassifiseringskoder fromValue(String v) {
        for (Klassifiseringskoder c: Klassifiseringskoder.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
