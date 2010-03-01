/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.idp.util;

import java.util.Collection;

/** Utility class for checking constraints on various parameters. */
public final class Assert {

    /** Constructor. */
    private Assert() {
    }

    /**
     * Checks that the given object is not null. If the object is null an {@link IllegalArgumentException} is thrown.
     * 
     * @param obj object to check
     */
    public static void isNotNull(Object obj) {
        if (obj != null) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given object is not null. If the object is null an {@link IllegalArgumentException} is thrown.
     * 
     * @param obj object to check
     * @param message message used in {@link IllegalArgumentException}
     */
    public static void isNotNull(Object obj, String message) {
        if (obj != null) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given object is null. If the object is not null an {@link IllegalArgumentException} is thrown.
     * 
     * @param obj object to check
     */
    public static void isNull(Object obj) {
        if (obj == null) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given object is null. If the object is not null an {@link IllegalArgumentException} is thrown.
     * 
     * @param obj object to check
     * @param message message used in {@link IllegalArgumentException}
     */
    public static void isNull(Object obj, String message) {
        if (obj == null) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given collection is empty. If the collection is not empty an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param collection collection check
     */
    public static void isEmpty(Collection<?> collection) {
        if (collection.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given collection is empty. If the collection is not empty an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param collection collection check
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isEmpty(Collection<?> collection, String message) {
        if (collection.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given collection is not empty. If the collection is empty an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param collection collection check
     */
    public static void isNotEmpty(Collection<?> collection) {
        if (!collection.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given collection is not empty. If the collection is empty an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param collection collection check
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isNotEmpty(Collection<?> collection, String message) {
        if (!collection.isEmpty()) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is greater than a given threshold. If the number is not greater than the threshold
     * an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     */
    public static void isGreaterThan(long threshold, long number) {
        if (number > threshold) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is greater than a given threshold. If the number is not greater than the threshold
     * an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isGreaterThan(long threshold, long number, String message) {
        if (number > threshold) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is greater than, or equal to, a given threshold. If the number is not greater than,
     * or equal to, the threshold an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     */
    public static void isGreaterThanOrEqual(long threshold, long number) {
        if (number >= threshold) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is greater than, or equal to, a given threshold. If the number is not greater than,
     * or equal to, the threshold an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isGreaterThanOrEqual(long threshold, long number, String message) {
        if (number >= threshold) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is less than a given threshold. If the number is not less than the threshold an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     */
    public static void isLessThan(long threshold, long number) {
        if (number < threshold) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is less than a given threshold. If the number is not less than the threshold an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isLessThan(long threshold, long number, String message) {
        if (number < threshold) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is less than, or equal to, a given threshold. If the number is not less than, or
     * equal to, the threshold an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     */
    public static void isLessThanOrEqual(long threshold, long number) {
        if (number <= threshold) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is less than, or equal to, a given threshold. If the number is not less than, or
     * equal to, the threshold an {@link IllegalArgumentException} is thrown.
     * 
     * @param threshold the threshold
     * @param number the number to be checked
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void isLessThanOrEqual(long threshold, long number, String message) {
        if (number <= threshold) {
            return;
        }
        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is in the inclusive range. If the number is not in the range an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param lowerTheshold lower bound of the range
     * @param upperThreshold upper bound of the range
     * @param number number to check
     */
    public static void numberInRangeInclusive(long lowerTheshold, long upperThreshold, long number) {
        if (number >= lowerTheshold && number <= upperThreshold) {
            return;
        }

        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is in the inclusive range. If the number is not in the range an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param lowerTheshold lower bound of the range
     * @param upperThreshold upper bound of the range
     * @param number number to check
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void numberInRangeInclusive(long lowerTheshold, long upperThreshold, long number, String message) {
        if (number >= lowerTheshold && number <= upperThreshold) {
            return;
        }

        throw new IllegalArgumentException(message);
    }

    /**
     * Checks that the given number is in the exclusive range. If the number is not in the range an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param lowerTheshold lower bound of the range
     * @param upperThreshold upper bound of the range
     * @param number number to check
     */
    public static void numberInRangeExclusive(long lowerTheshold, long upperThreshold, long number) {
        if (number > lowerTheshold && number < upperThreshold) {
            return;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks that the given number is in the exclusive range. If the number is not in the range an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param lowerTheshold lower bound of the range
     * @param upperThreshold upper bound of the range
     * @param number number to check
     * @param message message used in the {@link IllegalArgumentException}
     */
    public static void numberInRangeExclusive(long lowerTheshold, long upperThreshold, long number, String message) {
        if (number > lowerTheshold && number < upperThreshold) {
            return;
        }
        throw new IllegalArgumentException(message);
    }
}