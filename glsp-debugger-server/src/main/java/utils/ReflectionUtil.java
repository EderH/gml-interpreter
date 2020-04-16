package utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author pablo.barbosa (2017-08-15)
 */
public class ReflectionUtil {

    /**
     * Hiding constructor. The methods are statics
     */
    private ReflectionUtil() {
        // Hiding constructor
    }

    public static List<Field> getInheritedDeclaredFields(Class<?> fromClass, Class<?> stopWhenClass) {
        if (stopWhenClass == null) {
            stopWhenClass = Object.class;
        }
        List<Field> fields = new ArrayList<>();
        List<Class<?>> classes = new ArrayList<>();

        Class<?> cls = fromClass;
        do {
            classes.add(cls);
            cls = cls.getSuperclass();
        } while (cls != null && !cls.equals(stopWhenClass));

        for (int i = classes.size() - 1; i >= 0; i--) {
            fields.addAll(Arrays.asList(classes.get(i).getDeclaredFields()));
        }

        return fields;
    }

    public static Field getInheritedDeclaredField(Class<?> fromClass, String fieldName, Class<?> stopWhenClass) throws NoSuchFieldException {
        if (stopWhenClass == null) {
            stopWhenClass = Object.class;
        }

        Class<?> cls = fromClass;
        do {
            Field field;
            try {
                field = cls.getDeclaredField(fieldName);
                if (field != null) {
                    return field;
                }
            } catch (NoSuchFieldException | SecurityException e) {
                // Nothing. We'll try to get field from superclass
            }
            cls = cls.getSuperclass();
        } while (cls != null && !cls.equals(stopWhenClass));

        // If we got here, we'll throw an exception
        throw new NoSuchFieldException(fieldName);
    }

    public static Object getInheritedDeclaredFieldValue(Object obj, String fieldName, Class<?> stopWhenClass) throws NoSuchFieldException, IllegalAccessException {
        Field field = getInheritedDeclaredField(obj.getClass(), fieldName, stopWhenClass);
        field.setAccessible(true);
        return field.get(obj);
    }

}
