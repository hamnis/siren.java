package no.arktekk.siren;

import io.vavr.control.Option;
import net.hamnaberg.json.Json;
import no.arktekk.siren.Field.Type;
import org.junit.Test;


import static io.vavr.control.Option.none;
import static org.junit.Assert.assertEquals;

public class FieldTest {

    @Test
    public void construct() {
        Field.Default field = Field.of("foo").with(Json.jString("bar"));
        assertEquals(field.name, "foo");
        assertEquals(field.value, Option.of(Json.jString("bar")));
        assertEquals(field.classes, none());
        assertEquals(field.title, none());
        assertEquals(field.type, Type.TEXT);
    }
}
