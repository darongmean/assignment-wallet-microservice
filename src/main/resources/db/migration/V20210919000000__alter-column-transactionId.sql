alter table TBalanceTransaction
    drop constraint uq_transactionId_on_TBalanceTransaction;

create index ix_transactionId_on_TBalanceTransaction
    on TBalanceTransaction(transactionId);
