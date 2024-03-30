-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';

-- DROP SEQUENCE public.transaction_sequence_generator;

CREATE SEQUENCE public.transaction_sequence_generator
	INCREMENT BY 50
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.transaction_sequence_generator OWNER TO postgres;
GRANT ALL ON SEQUENCE public.transaction_sequence_generator TO postgres;

-- DROP SEQUENCE public.user_sequence;

CREATE SEQUENCE public.user_sequence
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

-- Permissions

ALTER SEQUENCE public.user_sequence OWNER TO postgres;
GRANT ALL ON SEQUENCE public.user_sequence TO postgres;
-- public.user_table definition

-- Drop table

-- DROP TABLE public.user_table;

CREATE TABLE public.user_table (
	userid int8 NOT NULL,
	balance float8 NOT NULL,
	email varchar(255) NULL,
	"password" varchar(255) NULL,
	socialmediaacc varchar(255) NULL,
	bankaccountnumber varchar(255) NULL,
	bankname varchar(255) NULL,
	bankroutingnumber varchar(255) NULL,
	bank_account_number varchar(255) NULL,
	bank_name varchar(255) NULL,
	bank_routing_number varchar(255) NULL,
	currency varchar(3) NOT NULL,
	CONSTRAINT user_table_pkey PRIMARY KEY (userid),
	CONSTRAINT user_table_socialmediaacc_check CHECK (((socialmediaacc)::text = ANY ((ARRAY['Twitter'::character varying, 'Facebook'::character varying])::text[])))
);

-- Permissions

ALTER TABLE public.user_table OWNER TO postgres;
GRANT ALL ON TABLE public.user_table TO postgres;


-- public.transaction_table definition

-- Drop table

-- DROP TABLE public.transaction_table;

CREATE TABLE public.transaction_table (
	transactionid int8 NOT NULL,
	amount float8 NOT NULL,
	"timestamp" timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	description text NULL,
	fee float8 NOT NULL,
	sender_userid int8 NOT NULL,
	receiver_userid int8 NULL,
	currency varchar(3) NOT NULL,
	CONSTRAINT transaction_table_pkey PRIMARY KEY (transactionid),
	CONSTRAINT transaction_table_receiver_userid_fkey FOREIGN KEY (receiver_userid) REFERENCES public.user_table(userid),
	CONSTRAINT transaction_table_sender_userid_fkey FOREIGN KEY (sender_userid) REFERENCES public.user_table(userid)
);
CREATE INDEX idx_receiver_userid ON public.transaction_table USING btree (receiver_userid);
CREATE INDEX idx_sender_userid ON public.transaction_table USING btree (sender_userid);

-- Permissions

ALTER TABLE public.transaction_table OWNER TO postgres;
GRANT ALL ON TABLE public.transaction_table TO postgres;


-- public.user_connections definition

-- Drop table

-- DROP TABLE public.user_connections;

CREATE TABLE public.user_connections (
	user_id int8 NOT NULL,
	connected_user_id int8 NOT NULL,
	CONSTRAINT fk8bows9ocdx32uyl3g3i3he1tt FOREIGN KEY (connected_user_id) REFERENCES public.user_table(userid),
	CONSTRAINT fkgmq4kpm6rjyetb786rr2ux2ru FOREIGN KEY (user_id) REFERENCES public.user_table(userid)
);

-- Permissions

ALTER TABLE public.user_connections OWNER TO postgres;
GRANT ALL ON TABLE public.user_connections TO postgres;




-- Permissions

GRANT ALL ON SCHEMA public TO pg_database_owner;
GRANT USAGE ON SCHEMA public TO public;