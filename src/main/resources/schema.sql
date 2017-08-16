create table accounts (
id bigint auto_increment,
balance decimal(20, 2) not null,
version int not null,
primary key (id)
);

create table transfers (
id bigint auto_increment,
sourceAccountId bigint not null,
destinationAccountId bigint not null,
amount decimal(20, 2) not null,
primary key (id),
foreign key (sourceAccountId) references accounts(id),
foreign key (destinationAccountId) references accounts(id),
);