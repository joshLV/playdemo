package models.sales;

import com.google.gson.*;

import javax.persistence.ManyToMany;
import java.lang.reflect.Type;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 3/12/12
 * Time: 1:09 PM
 */
public class CategorySerializer implements JsonSerializer<Category> {
    @Override
    public JsonElement serialize(Category category, Type type, JsonSerializationContext jsonSerializationContext) {
        Gson gson = new GsonBuilder().setExclusionStrategies(new LocalExclusionStrategy()).create();
        return gson.toJsonTree(category);
    }

    private class LocalExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(ManyToMany.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == Goods.class;
        }
    }
}
