package no.arktekk.siren;

import io.vavr.control.Option;
import net.hamnaberg.json.Json;

import java.util.function.Consumer;
import java.util.function.Function;

import static io.vavr.control.Option.none;


public abstract class Field {
    public final String name;
    public final Option<Classes> classes;
    public final Option<Json.JValue> value;
    public final Option<String> title;

    public abstract <X> X fold(Function<Default, X> defaultField, Function<Schema, X> schema, Function<Nested, X> nested);
    public abstract void consume(Consumer<Default> defaultField, Consumer<Schema> schema, Consumer<Nested> nested);

    private Field(String name, Option<Classes> classes, Option<Json.JValue> value, Option<String> title) {
        this.name = name;
        this.classes = classes;
        this.value = value;
        this.title = title;
    }

    public static final class Default extends Field {
        public final Type type;

        public Default(String name, Type type, Option<Classes> classes, Option<Json.JValue> value, Option<String> title) {
            super(name, classes, value, title);
            this.type = type;
        }

        public Default with(Classes classes) {
            return new Default(name, type, Option.of(classes), value, title);
        }

        public Default with(Json.JValue value) { // TODO: Få vekk JsonValue
            return new Default(name, type, classes, Option.of(value), title);
        }

        public Default with(String title) {
            return new Default(name, type, classes, value, Option.of(title));
        }

        @Override
        public <X> X fold(Function<Default, X> defaultField, Function<Schema, X> schema, Function<Nested, X> nested) {
            return defaultField.apply(this);
        }

        @Override
        public void consume(Consumer<Default> defaultField, Consumer<Schema> schema, Consumer<Nested> nested) {
            defaultField.accept(this);
        }

        public Type getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Default aDefault = (Default) o;

            return type == aDefault.type;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    public static final class Schema extends Field {

        public final Json.JValue schema;

        public Schema(String name, Json.JValue schema, Option<Classes> classes, Option<Json.JValue> value, Option<String> title) {
            super(name, classes, value, title);
            this.schema = schema;
        }

        @Override
        public <X> X fold(Function<Default, X> defaultField, Function<Schema, X> schema, Function<Nested, X> nested) {
            return schema.apply(this);
        }

        @Override
        public void consume(Consumer<Default> defaultField, Consumer<Schema> schema, Consumer<Nested> nested) {
            schema.accept(this);
        }

        public Json.JValue getSchema() {
            return schema;
        }

        public Schema with(Classes classes) {
            return new Schema(name, schema, Option.of(classes), value, title);
        }

        public Schema with(Json.JValue value) { // TODO: Få vekk JsonValue
            return new Schema(name, schema, classes, Option.of(value), title);
        }

        public Schema with(String title) {
            return new Schema(name, schema, classes, value, Option.of(title));
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Schema schema1 = (Schema) o;

            return schema.equals(schema1.schema);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + schema.hashCode();
            return result;
        }
    }

    public static final class Nested extends Field {

        public final Fields fields;

        public Nested(String name, Fields fields, Option<Classes> classes, Option<Json.JValue> value, Option<String> title) {
            super(name, classes, value, title);
            this.fields = fields;
        }

        @Override
        public <X> X fold(Function<Default, X> defaultField, Function<Schema, X> schema, Function<Nested, X> nested) {
            return nested.apply(this);
        }

        @Override
        public void consume(Consumer<Default> defaultField, Consumer<Schema> schema, Consumer<Nested> nested) {
            nested.accept(this);
        }

        public Fields getFields() {
            return fields;
        }

        public Nested with(Classes classes) {
            return new Nested(name, fields, Option.of(classes), value, title);
        }

        public Nested with(Json.JValue value) { // TODO: Få vekk JsonValue
            return new Nested(name, fields, classes, Option.of(value), title);
        }

        public Nested with(String title) {
            return new Nested(name, fields, classes, value, Option.of(title));
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Nested nested1 = (Nested) o;

            return fields.equals(nested1.fields);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + fields.hashCode();
            return result;
        }
    }

    public static Default of(String name) {
        return new Default(name, Type.TEXT, none(), none(), none());
    }

    public static Default of(String name, Type type) {
        return new Default(name, type, none(), none(), none());
    }

    public static Schema schema(String name, Json.JValue schema) {
        return new Schema(name, schema, none(), none(), none());
    }

    public static Nested nested(String name, Fields fields) {
        return new Nested(name, fields, none(), none(), none());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Field)) return false;

        Field field = (Field) o;

        if (!name.equals(field.name)) return false;
        if (!classes.equals(field.classes)) return false;
        if (!value.equals(field.value)) return false;
        return title.equals(field.title);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + classes.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }


    public String getName() {
        return name;
    }

    public Option<Classes> getClasses() {
        return classes;
    }

    public Option<Json.JValue> getValue() {
        return value;
    }

    public Option<String> getTitle() {
        return title;
    }

    public enum Type {
        HIDDEN("hidden"),
        TEXT("text"),
        SEARCH("search"),
        TEL("tel"),
        URL("url"),
        EMAIL("email"),
        PASSWORD("password"),
        DATETIME("datetime"),
        DATE("date"),
        MONTH("month"),
        WEEK("week"),
        TIME("time"),
        DATETIME_LOCAL("datetime-local"),
        NUMBER("number"),
        RANGE("range"),
        COLOR("color"),
        CHECKBOX("checkbox"),
        RADIO("radio"),
        FILE("file");

        public String value;

        Type(String s) {
            this.value = s;
        }

        public static Type fromString(String s) {
            for (Type v : values()) {
                if (v.value.equals(s)) {
                    return v;
                }
            }
            return Type.TEXT;
        }
    }
}
