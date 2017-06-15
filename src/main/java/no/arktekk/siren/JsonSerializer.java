package no.arktekk.siren;

import io.vavr.collection.List;
import net.hamnaberg.json.Json;
import no.arktekk.siren.SubEntity.EmbeddedLink;
import no.arktekk.siren.SubEntity.EmbeddedRepresentation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface JsonSerializer<T> {
    T serialize(EmbeddedRepresentation embeddedRepresentation);

    T serialize(EmbeddedLink embeddedLink);

    T serialize(Entity entity);

    enum ImmutableJsonSerializer implements JsonSerializer<Json.JValue> {
        INSTANCE;

        private static Function<Iterable<String>, Json.JArray> FromIterableString =
                strings -> Json.jArray(List.ofAll(strings).map(Json::jString));

        private Json.JObject sirenBuilder(Entity entity) {
            Map<String, Json.JValue> map = new LinkedHashMap<>();
            entity.classes.forEach(cs -> map.put("class", FromIterableString.apply(cs)));
            entity.properties.forEach(ps -> map.put("properties", ps));
            entity.entities.forEach(es -> map.put("entities", Json.jArray(es.map(e -> e.toJson(this)))));
            entity.links.forEach(ls -> map.put("links", Json.jArray(ls.map(this::toLink))));
            entity.actions.forEach(as -> map.put("actions", Json.jArray(as.stream().map(this::toAction).collect(Collectors.toList()))));
            entity.title.forEach(t -> map.put("title", Json.jString(t)));
            return Json.jObject(map);
        }

        private Json.JObject toAction(Action a) {
            Map<String, Json.JValue> action = new LinkedHashMap<>();
            action.put("name", Json.jString(a.name));
            a.classes.forEach(cs -> action.put("class", FromIterableString.apply(cs)));
            a.method.forEach(m -> action.put("method", Json.jString(m.name())));
            action.put("href", Json.jString(a.href.toString()));
            a.title.forEach(t -> action.put("title", Json.jString(t)));
            a.type.forEach(t -> action.put("type", Json.jString(t.format())));
            a.fields.forEach(fs ->
                    action.put("fields", fromFields(fs)));
            return Json.jObject(action);
        }

        private Json.JObject toLink(Link link) {
            Map<String, Json.JValue> map = new LinkedHashMap<>();
            map.put("rel", FromIterableString.apply(link.rel));
            map.put("href", Json.jString(link.href.toString()));
            link.classes.forEach(cs -> map.put("class", FromIterableString.apply(cs)));
            link.type.forEach(type -> map.put("type", Json.jString(type.format())));
            link.title.forEach(title -> map.put("title", Json.jString(title)));
            return Json.jObject(map);
        }

        private Json.JArray fromFields(Fields fs) {
            return Json.jArray(fs.map(f -> {
                Map<String, Json.JValue> field = new LinkedHashMap<>();
                field.put("name", Json.jString(f.name));
                f.classes.forEach(cs -> field.put("class", FromIterableString.apply(cs)));
                f.consume(def -> {
                    field.put("type", Json.jString(def.type.value));
                }, schema -> {
                    field.put("type", Json.jString("json-schema"));
                    field.put("schema", schema.schema);
                }, nested -> {
                    field.put("type", Json.jString("fields"));
                    field.put("fields", fromFields(nested.fields));
                });
                f.value.forEach(v -> field.put("value", v));
                f.title.forEach(t -> field.put("title", Json.jString(t)));
                return Json.jObject(field);
            }));
        }

        public Json.JValue serialize(Entity entity) {
            return sirenBuilder(entity);
        }

        public Json.JValue serialize(EmbeddedRepresentation embeddedRepresentation) {
            Json.JObject object = sirenBuilder(embeddedRepresentation.entity);
            return object.put("rel", FromIterableString.apply(embeddedRepresentation.rel));
        }

        public Json.JValue serialize(EmbeddedLink embeddedLink) {
            return toLink(embeddedLink.toLink());
        }
    }
}
