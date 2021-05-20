CREATE OR REPLACE FUNCTION bit_count(setInt BIGINT) RETURNS INT AS 
$$ 
	DECLARE 
		i int;
		counting int;
		twoexpoi bigint;
	BEGIN
		i:= 0;
		counting:= 0;
		twoexpoi:=1;
		Loop
			IF setInt<twoexpoi THEN
				Exit;
			END IF;
			IF (setInt&twoexpoi) <> 0 THEN
				counting := counting + 1;
			END IF;
			i:= i + 1;
			twoexpoi:= 1<<i; 
		END Loop;	
		Return counting;
	END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION min_card_set_func(setIntEnd BIGINT, setIntNew BIGINT) RETURNS BIGINT AS 
$$ 
	DECLARE 
		i int;
		counting1 int;
		counting2 int;
		twoexpoi bigint;
		res bigint;
	BEGIN
		IF setIntNew < 0  THEN
			Return setIntEnd;
		END IF;
		IF setIntEnd < 0  THEN
			Return setIntNew;
		END IF;
		
		i:= 0;
		counting1:= 0;
		twoexpoi:=1;
		Loop
			IF setIntNew<twoexpoi AND setIntEnd<twoexpoi THEN
				Exit;
			END IF;
			IF (setIntNew&twoexpoi) <> 0 THEN
				counting1 := counting2 + 1;
			END IF;
			IF (setIntEnd&twoexpoi) <> 0 THEN
				counting2 := counting2 + 1;
			END IF;
			i:= i + 1;
			twoexpoi:= 1<<i; 
		END Loop;
		
		IF counting1>=counting2 THEN
			res := setIntEnd;
		ELSE
			res := setIntNew;
		END IF;
			
		Return res;
	END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE AGGREGATE min_card_set (bigint)(SFUNC = min_card_set_func, STYPE=bigint, initcond=-1);


CREATE OR REPLACE FUNCTION min_card_set_multi_func(setIntEnd text, setIntNew text) RETURNS Text AS 
$$ 
	DECLARE 
		t int;
		previous int;
		current int;
	BEGIN
		IF setIntEnd = '$'::text THEN
			Return setIntNew;
		END IF;
		
		current := 0;
		previous := 0;
		
		For t in SELECT bit_count(tab::bigint) FROM regexp_split_to_table(setIntNew, E'a') tab
		Loop
			current := current + t;
		END Loop;
		
		For t in SELECT bit_count(tab::bigint) FROM regexp_split_to_table(setIntEnd, E'a') tab
		Loop
			previous := previous + t;
		END Loop;
		
		IF previous <= current THEN
			Return setIntEnd;
		ELSE
			Return setIntNew;
		END IF;
	END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION min_card_set_multi_func_final(setInt text) RETURNS BIGINT AS 
$$ 
	DECLARE 
		t text;
		res bigint;
	BEGIN
		res := 0;
		FOREACH t in ARRAY regexp_split_to_array(setInt, E'a')
		Loop
			res := (res | (t::bigint));
		END Loop;
		Return res;
	END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE AGGREGATE min_card_set_multi (text)(SFUNC = min_card_set_multi_func, STYPE=text, initcond='$', finalfunc=min_card_set_multi_func_final);
























