/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.List;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static fi.evident.dalesbred.utils.Throwables.propagate;

final class ConstructorInstantiator<T> implements Instantiator<T> {

    @NotNull
    private final Constructor<T> constructor;

    @NotNull
    private final List<TypeConversion<Object,?>> conversions;

    ConstructorInstantiator(@NotNull Constructor<T> constructor, @NotNull List<TypeConversion<Object,?>> conversions) {
        this.constructor = requireNonNull(constructor);
        this.conversions = requireNonNull(conversions);
    }

    @Override
    @NotNull
    public T instantiate(@NotNull InstantiatorArguments arguments) {
        try {
            return constructor.newInstance(coerceArguments(arguments.getValues()));
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @NotNull
    private Object[] coerceArguments(@NotNull List<?> arguments) {
        Object[] result = new Object[arguments.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = conversions.get(i).convert(arguments.get(i));

        return result;
    }
}
