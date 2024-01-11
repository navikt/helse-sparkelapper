
package no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.metadata;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Dokumentasjonstype.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Dokumentasjonstype"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="arkivertBrev"/&gt;
 *     &lt;enumeration value="behandling"/&gt;
 *     &lt;enumeration value="elektronisk signatur"/&gt;
 *     &lt;enumeration value="fil"/&gt;
 *     &lt;enumeration value="telefon"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "Dokumentasjonstype", namespace = "http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/metadata")
@XmlEnum
public enum Dokumentasjonstype {

    @XmlEnumValue("arkivertBrev")
    ARKIVERT_BREV("arkivertBrev"),
    @XmlEnumValue("behandling")
    BEHANDLING("behandling"),
    @XmlEnumValue("elektronisk signatur")
    ELEKTRONISK_SIGNATUR("elektronisk signatur"),
    @XmlEnumValue("fil")
    FIL("fil"),
    @XmlEnumValue("telefon")
    TELEFON("telefon");
    private final String value;

    Dokumentasjonstype(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Dokumentasjonstype fromValue(String v) {
        for (Dokumentasjonstype c: Dokumentasjonstype.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
