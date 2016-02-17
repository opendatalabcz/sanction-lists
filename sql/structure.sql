--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

ALTER TABLE ONLY public.entry_companies DROP CONSTRAINT referenced_id_entry;
ALTER TABLE ONLY public.entry_companies DROP CONSTRAINT referenced_id;
ALTER TABLE ONLY public.entry_names DROP CONSTRAINT referenced_id;
ALTER TABLE ONLY public.entry_nationalities DROP CONSTRAINT nationality_id_reference;
ALTER TABLE ONLY public.entry_nationalities DROP CONSTRAINT entry_id_reference;
ALTER TABLE ONLY public.entry_places_of_birth DROP CONSTRAINT entry_id_reference;
ALTER TABLE ONLY public.entry_addresses DROP CONSTRAINT entry_id_reference;
ALTER TABLE ONLY public.entry_places_of_birth DROP CONSTRAINT address_id_reference;
ALTER TABLE ONLY public.entry_addresses DROP CONSTRAINT address_id_reference;
ALTER TABLE ONLY public.nationalities DROP CONSTRAINT unique_nationality;
ALTER TABLE ONLY public.entry_names DROP CONSTRAINT unique_name;
ALTER TABLE ONLY public.entries DROP CONSTRAINT unique_id;
ALTER TABLE ONLY public.addresses DROP CONSTRAINT unique_address;
ALTER TABLE ONLY public.nationalities DROP CONSTRAINT nationalities_pkey;
ALTER TABLE ONLY public.entry_places_of_birth DROP CONSTRAINT entry_places_of_birth_pkey;
ALTER TABLE ONLY public.entry_nationalities DROP CONSTRAINT entry_nationalities_pkey;
ALTER TABLE ONLY public.entry_names DROP CONSTRAINT entry_names_pkey;
ALTER TABLE ONLY public.entry_addresses DROP CONSTRAINT entry_addresses_pkey;
ALTER TABLE ONLY public.entries DROP CONSTRAINT entries_pkey;
ALTER TABLE ONLY public.addresses DROP CONSTRAINT addresses_pkey;
ALTER TABLE public.nationalities ALTER COLUMN nationality_id DROP DEFAULT;
ALTER TABLE public.entries ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.addresses ALTER COLUMN address_id DROP DEFAULT;
DROP SEQUENCE public.nationalities_nationality_id_seq;
DROP TABLE public.nationalities;
DROP TABLE public.entry_places_of_birth;
DROP TABLE public.entry_nationalities;
DROP TABLE public.entry_names;
DROP TABLE public.entry_companies;
DROP TABLE public.entry_addresses;
DROP SEQUENCE public.entries_id_seq;
DROP TABLE public.entries;
DROP SEQUENCE public.addresses_address_id_seq;
DROP TABLE public.addresses;
DROP EXTENSION plpgsql;
DROP SCHEMA public;
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: addresses; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE addresses (
    address_id integer NOT NULL,
    address character varying(255) NOT NULL
);


--
-- Name: addresses_address_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE addresses_address_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: addresses_address_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE addresses_address_id_seq OWNED BY addresses.address_id;


--
-- Name: entries; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entries (
    id integer NOT NULL,
    type character varying(7) NOT NULL,
    date_of_birth_start timestamp without time zone,
    date_of_birth_end timestamp without time zone
);


--
-- Name: entries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE entries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE entries_id_seq OWNED BY entries.id;


--
-- Name: entry_addresses; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entry_addresses (
    entry_id integer NOT NULL,
    address_id integer NOT NULL
);


--
-- Name: entry_companies; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entry_companies (
    entry_id integer NOT NULL,
    company_name character varying(255) NOT NULL,
    company_address character varying(255),
    referenced_id integer
);


--
-- Name: entry_names; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entry_names (
    entry_id integer NOT NULL,
    name character varying(255) NOT NULL
);


--
-- Name: entry_nationalities; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entry_nationalities (
    entry_id integer NOT NULL,
    nationality_id integer NOT NULL
);


--
-- Name: entry_places_of_birth; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entry_places_of_birth (
    entry_id integer NOT NULL,
    address_id integer NOT NULL
);


--
-- Name: nationalities; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE nationalities (
    nationality_id integer NOT NULL,
    nationality character varying(255) NOT NULL
);


--
-- Name: COLUMN nationalities.nationality; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN nationalities.nationality IS 'Name of nationality';


--
-- Name: nationalities_nationality_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE nationalities_nationality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: nationalities_nationality_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE nationalities_nationality_id_seq OWNED BY nationalities.nationality_id;


--
-- Name: address_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY addresses ALTER COLUMN address_id SET DEFAULT nextval('addresses_address_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY entries ALTER COLUMN id SET DEFAULT nextval('entries_id_seq'::regclass);


--
-- Name: nationality_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY nationalities ALTER COLUMN nationality_id SET DEFAULT nextval('nationalities_nationality_id_seq'::regclass);


--
-- Name: addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (address_id);


--
-- Name: entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entries
    ADD CONSTRAINT entries_pkey PRIMARY KEY (id);


--
-- Name: entry_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entry_addresses
    ADD CONSTRAINT entry_addresses_pkey PRIMARY KEY (entry_id, address_id);


--
-- Name: entry_names_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entry_names
    ADD CONSTRAINT entry_names_pkey PRIMARY KEY (entry_id, name);


--
-- Name: entry_nationalities_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entry_nationalities
    ADD CONSTRAINT entry_nationalities_pkey PRIMARY KEY (entry_id, nationality_id);


--
-- Name: entry_places_of_birth_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entry_places_of_birth
    ADD CONSTRAINT entry_places_of_birth_pkey PRIMARY KEY (entry_id, address_id);


--
-- Name: nationalities_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nationalities
    ADD CONSTRAINT nationalities_pkey PRIMARY KEY (nationality_id);


--
-- Name: unique_address; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT unique_address UNIQUE (address);


--
-- Name: unique_id; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entries
    ADD CONSTRAINT unique_id UNIQUE (id);


--
-- Name: unique_name; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entry_names
    ADD CONSTRAINT unique_name UNIQUE (name);


--
-- Name: unique_nationality; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nationalities
    ADD CONSTRAINT unique_nationality UNIQUE (nationality);


--
-- Name: address_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_addresses
    ADD CONSTRAINT address_id_reference FOREIGN KEY (address_id) REFERENCES addresses(address_id);


--
-- Name: address_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_places_of_birth
    ADD CONSTRAINT address_id_reference FOREIGN KEY (address_id) REFERENCES addresses(address_id);


--
-- Name: entry_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_addresses
    ADD CONSTRAINT entry_id_reference FOREIGN KEY (entry_id) REFERENCES entries(id);


--
-- Name: entry_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_places_of_birth
    ADD CONSTRAINT entry_id_reference FOREIGN KEY (entry_id) REFERENCES entries(id);


--
-- Name: entry_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_nationalities
    ADD CONSTRAINT entry_id_reference FOREIGN KEY (entry_id) REFERENCES entries(id);


--
-- Name: nationality_id_reference; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_nationalities
    ADD CONSTRAINT nationality_id_reference FOREIGN KEY (nationality_id) REFERENCES nationalities(nationality_id);


--
-- Name: referenced_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_names
    ADD CONSTRAINT referenced_id FOREIGN KEY (entry_id) REFERENCES entries(id);


--
-- Name: referenced_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_companies
    ADD CONSTRAINT referenced_id FOREIGN KEY (entry_id) REFERENCES entries(id);


--
-- Name: referenced_id_entry; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY entry_companies
    ADD CONSTRAINT referenced_id_entry FOREIGN KEY (referenced_id) REFERENCES entries(id);


--
-- PostgreSQL database dump complete
--

