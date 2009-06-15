/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.acoveo.tools;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.lang.StringUtils;
//import org.apache.openjpa.lib.util.J2DoPrivHelper;
//import org.apache.openjpa.lib.util.Localizer;
//import org.apache.openjpa.util.GeneralException; 
//import org.apache.openjpa.util.UserException; 

/**
 * Reflection utilities used to support and augment enhancement.  Used both
 * at enhancement time and at runtime.
 * This class has been copied from
 * https://svn.apache.org/repos/asf/openjpa/trunk/openjpa-kernel/src/main/java/org/apache/openjpa/enhance/Reflection.java
 * on 12/19/2008 and was modified to be generally usable
 *
 * @author Abe White
 * @author Florian Hackenberger
 */
public class Reflection {

    /**
     * Return the getter method matching the given property name, optionally
     * throwing an exception if none.
     * @throws RuntimeException if mustExist is true and the getter method was not found
     */
    public static Method findGetter(Class cls, String prop, boolean mustExist) {
        prop = StringUtils.capitalize(prop);
        String name = "get" + prop;
        Method m;
//        try {
            // this algorithm searches for a get<prop> or is<prop> method in
            // a breadth-first manner.
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                m = getDeclaredMethod(c, name, null);
                if (m != null) {
                    return m;
                } else {
                    m = getDeclaredMethod(c, "is" + prop, null);
                    if (m != null && (m.getReturnType() == boolean.class
                        || m.getReturnType() == Boolean.class))
                        return m;
                }
            }
//        } catch (Exception e) {
//            throw new GeneralException(e);
//        }

        if (mustExist)
            throw new RuntimeException("Could not find property" + prop + " in class " + cls.getCanonicalName());
        return null;
    }

    /**
     * Return the setter method matching the given property name, optionally
     * throwing an exception if none.  The property must also have a getter.
     * @throws RuntimeException if mustExist is true and the setter method was not found
     */
    public static Method findSetter(Class cls, String prop, boolean mustExist) {
        Method getter = findGetter(cls, prop, mustExist);
        return (getter == null) ? null 
            : findSetter(cls, prop, getter.getReturnType(), mustExist);
    }

    /**
     * Return the setter method matching the given property name, optionally
     * throwing an exception if none.
     * @throws RuntimeException if mustExist is true and the getter method was not found
     */
    public static Method findSetter(Class cls, String prop, Class param,
        boolean mustExist) {
        String name = "set" + StringUtils.capitalize(prop);
        Method m;
//        try {
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                m = getDeclaredMethod(c, name, param);
                if (m != null)
                    return m;
            }
//        } catch (Exception e) {
//            throw new GeneralException(e);
//        }

        if (mustExist)
            throw new RuntimeException("Could not find setter for property " + prop + " in class " + cls.getCanonicalName());
        return null;
    }
    
    /**
     * Return the addTo method matching the given collection property name, optionally
     * throwing an exception if none.
     * @throws RuntimeException if mustExist is true and the setter method was not found
     */
    public static Method findCollectionAdd(Class cls, String prop, Class param, boolean mustExist) {
      String name = "addTo" + StringUtils.capitalize(prop);
      Method m;
      for (Class c = cls; c != null && c != Object.class;
          c = c.getSuperclass()) {
          m = getDeclaredMethod(c, name, param);
          if (m != null)
              return m;
      }

      if (mustExist)
          throw new RuntimeException("Could not find addTo for property " + prop + " in class " + cls.getCanonicalName());
      return null;
    }

    /**
     * Return the removeFrom method matching the given collection property name, optionally
     * throwing an exception if none.
     * @throws RuntimeException if mustExist is true and the setter method was not found
     */
    public static Method findCollectionRemove(Class cls, String prop, Class param, boolean mustExist) {
      String name = "removeFrom" + StringUtils.capitalize(prop);
      Method m;
      for (Class c = cls; c != null && c != Object.class;
          c = c.getSuperclass()) {
          m = getDeclaredMethod(c, name, param);
          if (m != null)
              return m;
      }

      if (mustExist)
          throw new RuntimeException("Could not find removeFrom for property " + prop + " in class " + cls.getCanonicalName());
      return null;
    }
    
    /**
     * Return the modifyMap method matching the given collection property name, optionally
     * throwing an exception if none.
     * @throws RuntimeException if mustExist is true and the setter method was not found
     */
    public static Method findModifyMap(Class cls, String prop, Class param, boolean mustExist) {
      String name = "modifyEntry" + StringUtils.capitalize(prop);
      Method m;
      for (Class c = cls; c != null && c != Object.class;
          c = c.getSuperclass()) {
          m = getDeclaredMethod(c, name, param);
          if (m != null)
              return m;
      }

      if (mustExist)
          throw new RuntimeException("Could not find modifyMap for property " + prop + " in class " + cls.getCanonicalName());
      return null;
    }
    
    /**
     * Invokes <code>cls.getDeclaredMethods()</code>, and returns the method
     * that matches the <code>name</code> and <code>param</code> arguments.
     * Avoids the exception thrown by <code>Class.getDeclaredMethod()</code>
     * for performance reasons. <code>param</code> may be null. Additionally,
     * if there are multiple methods with different return types, this will
     * return the method defined in the least-derived class.
     *
     * @since 0.9.8
     */
    static Method getDeclaredMethod(Class cls, String name,
        Class param) {
        Method[] methods = (Method[]) AccessController.doPrivileged(
            getDeclaredMethodsAction(cls));
        Method candidate = null;
        for (int i = 0 ; i < methods.length; i++) {
            if (name.equals(methods[i].getName())) {
                Class[] methodParams = methods[i].getParameterTypes();
                if (param == null && methodParams.length == 0)
                    candidate = mostDerived(methods[i], candidate);
                else if (param != null && methodParams.length == 1
                    && param.equals(methodParams[0]))
                    candidate = mostDerived(methods[i], candidate);
            }
        }
        return candidate;
    }

    static Method mostDerived(Method meth1, Method meth2) {
        if (meth1 == null)
            return meth2;
        if (meth2 == null)
            return meth1;
        
        Class cls2 = meth2.getDeclaringClass();
        Class cls1 = meth1.getDeclaringClass();

        if (cls1.equals(cls2)) {
            Class ret1 = meth1.getReturnType();
            Class ret2 = meth2.getReturnType();
            if (ret1.isAssignableFrom(ret2))
                return meth2;
            else if (ret2.isAssignableFrom(ret1))
                return meth1;
            else
                throw new IllegalArgumentException(
                    "most-derived-unrelated-same-type" + meth1 + meth2);
        } else {
            if (cls1.isAssignableFrom(cls2))
                return meth2;
            else if (cls2.isAssignableFrom(cls1))
                return meth1;
            else
                throw new IllegalArgumentException(
                    "most-derived-unrelated" + meth1 + meth2);
        }
    }

    /**
     * Return the field with the given name, optionally throwing an exception
     * if none.
     * @throws RuntimeException if mustExist is true and the field was not found
     */
    public static Field findField(Class cls, String name, boolean mustExist) {
//        try {
            Field f;
            for (Class c = cls; c != null && c != Object.class;
                c = c.getSuperclass()) {
                f = getDeclaredField(c, name);
                if (f != null)
                    return f;
            }
//        } catch (Exception e) {
//            throw new GeneralException(e);
//        }

        if (mustExist)
            throw new RuntimeException("Bad field" + cls.getCanonicalName() + name);
        return null;
    }

    /**
     * Invokes <code>cls.getDeclaredFields()</code>, and returns the field
     * that matches the <code>name</code> argument.  Avoids the exception
     * thrown by <code>Class.getDeclaredField()</code> for performance reasons.
     *
     * @since 0.9.8
     */
    private static Field getDeclaredField(Class cls, String name) {
        Field[] fields = AccessController.doPrivileged(
            getDeclaredFieldsAction(cls));
        for (int i = 0 ; i < fields.length; i++) {
            if (name.equals(fields[i].getName()))
                return fields[i];
        }
        return null;
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static Object get(Object target, Field field) {
        if (target == null || field == null)
            return null;
        makeAccessible(field, field.getModifiers());
        try {
            return field.get(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }
    
    /**
     * Get the value of the given named field or a corresponding getter method.
     * 
     * @return null if the field does not exist and mustExist is set to false or
     * the given target is null.
     * 
     * @exception RuntimeException if mustExist is true and the field or getter 
     * method is non-existent
     */
    public static Object getValue(Object obj, String prop, boolean mustExist) {
        if (obj == null)
            return null;
        Class cls = obj.getClass();
        Field field = findField(cls, prop, false);
        if (field != null)
            return get(obj, field);
        Method getter = findGetter(cls, prop, false);
        if (getter != null)
            return get(obj, getter);
        if (mustExist)
            throw new RuntimeException("Bad field" + cls.getCanonicalName() + prop);
        return null; // should not reach
    }

    /**
     * Make the given member accessible if it isn't already.
     */
    private static void makeAccessible(AccessibleObject ao, int mods) {
        try {
            if (!Modifier.isPublic(mods) && !ao.isAccessible())
                AccessController.doPrivileged(
                    setAccessibleAction(ao, true));
        } catch (SecurityException se) {
            throw new RuntimeException("Reflection security " + ao);
        }
    }

    /**
     * Wrap the given reflection exception as a runtime exception.
     */
    private static RuntimeException wrapReflectionException(Throwable t) {
        if (t instanceof InvocationTargetException)
            t = ((InvocationTargetException) t).getTargetException();    
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        return new RuntimeException(t);
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static boolean getBoolean(Object target, Field field) {
        if (target == null || field == null)
            return false;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getBoolean(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static byte getByte(Object target, Field field) {
        if (target == null || field == null)
            return (byte) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getByte(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static char getChar(Object target, Field field) {
        if (target == null || field == null)
            return (char) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getChar(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static double getDouble(Object target, Field field) {
        if (target == null || field == null)
            return 0D;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getDouble(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static float getFloat(Object target, Field field) {
        if (target == null || field == null)
            return 0F;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getFloat(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static int getInt(Object target, Field field) {
        if (target == null || field == null)
            return 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getInt(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static long getLong(Object target, Field field) {
        if (target == null || field == null)
            return 0L;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getLong(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the value of the given field in the given object.
     */
    public static short getShort(Object target, Field field) {
        if (target == null || field == null)
            return (short) 0;
        makeAccessible(field, field.getModifiers());
        try {
            return field.getShort(target);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static Object get(Object target, Method getter) {
        if (target == null || getter == null)
            return null;
        makeAccessible(getter, getter.getModifiers());
        try {
            return getter.invoke(target, (Object[]) null);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static boolean getBoolean(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? false : ((Boolean) o).booleanValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static byte getByte(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (byte) 0 : ((Number) o).byteValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static char getChar(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (char) 0 : ((Character) o).charValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static double getDouble(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0D : ((Number) o).doubleValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static float getFloat(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0F : ((Number) o).floatValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static int getInt(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0 : ((Number) o).intValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static long getLong(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? 0L : ((Number) o).longValue();
    }

    /**
     * Return the return value of the given getter in the given object.
     */
    public static short getShort(Object target, Method getter) {
        Object o = get(target, getter);
        return (o == null) ? (short) 0 : ((Number) o).shortValue();
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, Object value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.set(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, boolean value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setBoolean(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, byte value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setByte(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, char value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setChar(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, double value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setDouble(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, float value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setFloat(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, int value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setInt(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, long value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setLong(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     */
    public static void set(Object target, Field field, short value) {
        if (target == null || field == null)
            return;
        makeAccessible(field, field.getModifiers());
        try {
            field.setShort(target, value);
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, Object value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, boolean value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, byte value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, char value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, double value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, float value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, int value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, long value, Field field) {
        set(target, field, value);
    }

    /**
     * Set the value of the given field in the given object.
     * Same behavior as above methods, but parameter ordering is rearranged
     * to simplify usage from generated bytecodes.
     *
     * @since 1.0.0
     */
    public static void set(Object target, short value, Field field) {
        set(target, field, value);
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, Object value) {
        if (target == null || setter == null)
            return;
        makeAccessible(setter, setter.getModifiers());
        try {
            setter.invoke(target, new Object[] { value });
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, boolean value) {
        set(target, setter, (value) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, byte value) {
        set(target, setter, new Byte(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, char value) {
        set(target, setter, new Character(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, double value) {
        set(target, setter, new Double(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, float value) {
        set(target, setter, new Float(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, int value) {
        set(target, setter, new Integer(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, long value) {
        set(target, setter, new Long(value));
    }

    /**
     * Invoke the given setter on the given object.
     */
    public static void set(Object target, Method setter, short value) {
        set(target, setter, new Short(value));
    }
    
    /**
     * Invoke the collection add method on the given object
     */
    public static void addToCollection(Object target, Method collectionAdder, Object added) {
        if (target == null || collectionAdder == null)
            return;
        makeAccessible(collectionAdder, collectionAdder.getModifiers());
        try {
        	collectionAdder.invoke(target, new Object[] { added });
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }
    
    /**
     * Invoke the collection remove method on the given object
     */
    public static void removeFromCollection(Object target, Method collectionRemover, Object removed) {
        if (target == null || collectionRemover == null)
            return;
        makeAccessible(collectionRemover, collectionRemover.getModifiers());
        try {
        	collectionRemover.invoke(target, new Object[] { removed });
        } catch (Throwable t) {
            throw wrapReflectionException(t);
        }
    }

    /**
     * Return a PrivilegeAction object for clazz.getDeclaredMethods().
     * 
     * This method is from:
     * https://svn.apache.org/repos/asf/openjpa/trunk/openjpa-lib/src/main/java/org/apache/openjpa/lib/util/J2DoPrivHelper.java
     * 
     * Requires security policy:
     *   'permission java.lang.RuntimePermission "accessDeclaredMembers";'
     *   
     * @return Method[]
     */
    public static final PrivilegedAction<Method []> getDeclaredMethodsAction(
        final Class<?> clazz) {
        return new PrivilegedAction<Method []>() {
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }
        };
    }
    
    /**
     * Return a PrivilegeAction object for class.getDeclaredFields().
     * 
     * This method is from:
     * https://svn.apache.org/repos/asf/openjpa/trunk/openjpa-lib/src/main/java/org/apache/openjpa/lib/util/J2DoPrivHelper.java
     * 
     * Requires security policy:
     *   'permission java.lang.RuntimePermission "accessDeclaredMembers";'
     *   
     * @return Field[]
     */
    public static final PrivilegedAction<Field []> getDeclaredFieldsAction(
        final Class<?> clazz) {
        return new PrivilegedAction<Field []>() {
            public Field[] run() {
                return clazz.getDeclaredFields();
            }
        };
    }

    /**
     * Return a PrivilegeAction object for aObj.setAccessible().
     * 
     * This method is from:
     * https://svn.apache.org/repos/asf/openjpa/trunk/openjpa-lib/src/main/java/org/apache/openjpa/lib/util/J2DoPrivHelper.java
     * 
     * Requires security policy: 'permission java.lang.reflect.ReflectPermission
     * "suppressAccessChecks";'
     */
    public static final PrivilegedAction<Object> setAccessibleAction(
        final AccessibleObject aObj, final boolean flag) {
        return new PrivilegedAction<Object>() {
            public Object run() {
                aObj.setAccessible(flag);
                return (Object) null;
            }
        };
    }
    
    /**
     * Retuns a method matching the name which takes the given parameters
     * If one of the objects in param is null, this method does not check
     * the type of this parameter.
     * 
     * @param cls The class to search
     * @param name The name of the method
     * @param params The parameter types
     * @param mustExist Whether the method should throw an exception if no matching method has been found
     * @exception RuntimeException Thrown if mustExist == true and no matching method was found
     * @return The method or null in case none was found and mustExist == false
     */
    public static Method findMethodFuzzy(Class cls, String name, Class[] params,
        boolean mustExist) {
        Method m;
        for (Class c = cls; c != null && c != Object.class;
            c = c.getSuperclass()) {
            Method[] methods = (Method[]) AccessController.doPrivileged(
                    getDeclaredMethodsAction(cls));
            Method candidate = null;
            for (int i = 0 ; i < methods.length; i++) {
                if (name.equals(methods[i].getName())) {
                    Class[] methodParams = methods[i].getParameterTypes();
                    if(methodParams.length != params.length) {
                        continue;
                    }
                    boolean paramsMatch = true;
                    int paramIndex = 0;
                    for(Class methodParam : methodParams) {
                        if(methodParam != null && methodParam != params[paramIndex]) {
                            paramsMatch = false;
                            break;
                        }
                        paramIndex++;
                    }
                    if(paramsMatch) {
                        candidate = mostDerived(methods[i], candidate);
                    }
                }
            }
            if (candidate != null)
                return candidate;
        }
        if (mustExist)
            throw new RuntimeException("Could not find method " + name + " in class " + cls.getCanonicalName());
            return null;
    }
    
    /**
     * Retuns a method matching the name which takes the given parameters
     * 
     * @param cls The class to search
     * @param name The name of the method
     * @param params The parameter types
     * @param mustExist Whether the method should throw an exception if no matching method has been found
     * @exception RuntimeException Thrown if mustExist == true and no matching method was found
     * @return The method or null in case none was found and mustExist == false
     */
    public static Method findMethod(Class cls, String name, Class[] params,
        boolean mustExist) {
        Method m;
        for (Class c = cls; c != null && c != Object.class;
            c = c.getSuperclass()) {
            Method[] methods = (Method[]) AccessController.doPrivileged(
                    getDeclaredMethodsAction(cls));
            Method candidate = null;
            for (int i = 0 ; i < methods.length; i++) {
                if (name.equals(methods[i].getName())) {
                    Class[] methodParams = methods[i].getParameterTypes();
                    if(methodParams.length != params.length) {
                        continue;
                    }
                    boolean paramsMatch = true;
                    int paramIndex = 0;
                    for(Class methodParam : methodParams) {
                        if(methodParam != params[paramIndex]) {
                            paramsMatch = false;
                            break;
                        }
                        paramIndex++;
                    }
                    if(paramsMatch) {
                        candidate = mostDerived(methods[i], candidate);
                    }
                }
            }
            if (candidate != null)
                return candidate;
        }
        if (mustExist)
            throw new RuntimeException("Could not find method " + name + " in class " + cls.getCanonicalName());
            return null;
    }
    
    /** Returns the types of a list of objects
     * 
     * @param parameters The list ob objects
     * @return An array of Class objects specifying the type of each Object in parameters.
     */
    public static Class<?>[] getParameterTypes(Object[] parameters) {
        Class<?>[] types = new Class[parameters.length];
        int index = 0;
        for(Object param : parameters) {
            if(param != null) {
                types[index] = param.getClass();
            }
            index++;
        }
        return types;
    }
}