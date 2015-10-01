package no.arktekk.siren;

import no.arktekk.siren.SubEntity.EmbeddedLink;
import no.arktekk.siren.SubEntity.EmbeddedRepresentation;
import no.arktekk.siren.util.StreamUtils;
import org.glassfish.json.JsonFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface JsonSerializer<T> {
    T serialize(EmbeddedRepresentation embeddedRepresentation);

    T serialize(EmbeddedLink embeddedLink);

    T serialize(Entity entity);

    final class JavaxJsonSerializer implements JsonSerializer<JsonValue> {

        private static Function<Iterable<String>, JsonArray> FromIterableString =
                strings -> JsonFactory.arrayOf(StreamUtils.stream(strings).<JsonValue>map(JsonFactory::jsonString).collect(Collectors.toList()));

        private JsonObjectBuilder sirenBuilder(Entity entity) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            entity.classes.ifPresent(cs -> builder.add("class", FromIterableString.apply(cs)));
            entity.properties.ifPresent(ps -> builder.add("properties", ps));
            entity.entities.ifPresent(es -> builder.add("entities", JsonFactory.arrayOf(es.stream().map(e -> e.toJson(this)).collect(Collectors.toList()))));
            entity.links.ifPresent(ls -> builder.add("links", JsonFactory.arrayOf(ls.stream().map(l -> {
                JsonObjectBuilder link = Json.createObjectBuilder();
                link.add("rel", FromIterableString.apply(l.rel));
                link.add("href", l.href.toString()); // TODO: denne skal sikker encodes
                return link.build();
            }).collect(Collectors.toList()))));
            entity.actions.ifPresent(as -> builder.add("actions", JsonFactory.arrayOf(as.stream().map(a -> {
                JsonObjectBuilder action = Json.createObjectBuilder();
                action.add("name", a.name);
                a.classes.ifPresent(cs -> action.add("class", FromIterableString.apply(cs)));
                a.method.ifPresent(m -> action.add("method", m.name()));
                action.add("href", a.href.toString());  // TODO: denne skal sikker encodes
                a.title.ifPresent(t -> action.add("title", t));
                a.type.ifPresent(t -> action.add("type", t.format()));
                a.fields.ifPresent(fs ->
                        action.add("fields", JsonFactory.arrayOf(fs.stream().map(f -> {
                            JsonObjectBuilder field = Json.createObjectBuilder();
                            field.add("name", f.name);
                            f.classes.ifPresent(cs -> field.add("class", FromIterableString.apply(cs)));
                            field.add("type", f.type.value);
                            f.value.ifPresent(v -> field.add("value", v));
                            f.title.ifPresent(t -> field.add("title", t));
                            return field.build();
                        }).collect(Collectors.toList()))));
                return action.build();
            }).collect(Collectors.toList()))));
            entity.title.ifPresent(t -> builder.add("title", t));
            return builder;
        }

        public JsonValue serialize(Entity entity) {
            return sirenBuilder(entity).build();
        }

        public JsonValue serialize(EmbeddedRepresentation embeddedRepresentation) {
            return sirenBuilder(embeddedRepresentation.entity).add("rel", FromIterableString.apply(embeddedRepresentation.rel)).build();
        }

        public JsonValue serialize(EmbeddedLink embeddedLink) {
            JsonObjectBuilder object = Json.createObjectBuilder();
            embeddedLink.classes.ifPresent(cs -> object.add("class", FromIterableString.apply(cs)));
            object.add("rel", FromIterableString.apply(embeddedLink.rel));
            object.add("href", embeddedLink.href.toString()); // TODO: denne skal sikkert encodes
            embeddedLink.title.ifPresent(t -> object.add("title", t));
            embeddedLink.type.ifPresent(t -> object.add("type", t.format()));
            return object.build();
        }
    }
}
