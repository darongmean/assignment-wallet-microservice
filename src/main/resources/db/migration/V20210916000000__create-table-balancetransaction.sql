create table TBalanceTransaction(
    playerId varchar(255) not null,
    totalBalance decimal(13,4) default 0 not null,
    transactionAmount decimal(13,4) default 0 not null,
    transactionType varchar(10) not null, -- one of "debit" and "credit"
    transactionId varchar(255) not null,
    -- non-functional
    balanceTransactionPk identity primary key,
    createdAt timestamp default current_timestamp not null,
    traceId varchar(255),
    -- constraint
    constraint uq_transactionId_on_TBalanceTransaction unique(transactionId)
);

create index ix_playerId_on_TBalanceTransaction on TBalanceTransaction(playerId);
