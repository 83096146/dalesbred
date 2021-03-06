/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.transaction;

import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractTransactionManager implements TransactionManager {

    protected abstract @NotNull Optional<DefaultTransaction> getActiveTransaction();

    protected abstract <T> T withNewTransaction(@NotNull TransactionCallback<T> callback,
                                                @NotNull Dialect dialect,
                                                @NotNull Isolation isolation);

    protected abstract <T> T withSuspendedTransaction(@NotNull TransactionCallback<T> callback,
                                                      @NotNull Isolation isolation,
                                                      @NotNull Dialect dialect);

    @Override
    public <T> T withTransaction(@NotNull TransactionSettings settings, @NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        Propagation propagation = settings.getPropagation();
        Isolation isolation = settings.getIsolation();

        DefaultTransaction existingTransaction = getActiveTransaction().orElse(null);

        if (existingTransaction != null) {
            if (propagation == Propagation.REQUIRES_NEW)
                return withSuspendedTransaction(callback, isolation, dialect);
            else if (propagation == Propagation.NESTED)
                return existingTransaction.nested(callback, dialect);
            else
                return existingTransaction.join(callback, dialect);

        } else {
            if (propagation == Propagation.MANDATORY)
                throw new NoActiveTransactionException("Transaction propagation was MANDATORY, but there was no existing transaction.");

            return withNewTransaction(callback, dialect, isolation);
        }
    }

    @Override
    public <T> T withCurrentTransaction(@NotNull TransactionCallback<T> callback, @NotNull Dialect dialect) {
        DefaultTransaction transaction = getActiveTransaction().orElseThrow(() ->
                new NoActiveTransactionException("Tried to perform database operation without active transaction. Database accesses should be bracketed with Database.withTransaction(...) or implicit transactions should be enabled."));
        return transaction.join(callback, dialect);
    }

    @Override
    public boolean hasActiveTransaction() {
        return getActiveTransaction().isPresent();
    }
}
